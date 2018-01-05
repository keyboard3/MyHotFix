package com.keyboard3.buildsrc

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.api.transform.Format;
import com.android.build.gradle.AppPlugin
import com.keyboard3.buildsrc.util.NuwaAndroidUtils;
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

/**
 * @author keyboard3 on 2018/1/5
 */

public class HotfixPlugin extends Transform implements Plugin<Project> {
    def debugOn = true
    private static final String RELEASE = "Release"

    void apply(Project project) {
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            def android = project.extensions.getByType(AppExtension)
            android.registerTransform(this)
        }
        project.afterEvaluate {
            project.android.applicationVariants.each { variant ->
                println("variant:" + variant.name)
                println("variant:" + variant.getDirName())

                if (variant.name.endsWith(RELEASE) || debugOn) {
                    def prepareTaskName = "check${variant.name.capitalize()}Manifest";
                    def prepareTask = project.tasks.findByName(prepareTaskName)
                    if (prepareTask) {
                        prepareTask.doFirst({
                            prepareBuild(project, variant);
                        })
                    } else {
                        println("not found task ${prepareTaskName}")
                    }
                }
            }
        }
    }

    void prepareBuild(def project, def variant) {
        //proguard map
        println("prepareBuild")

        //sign hack.apk
        def projectDir = project.projectDir.absolutePath
        projectDir = projectDir.subSequence(0, projectDir.lastIndexOf('/'));
        def hackApkFile = new File(projectDir + '/extras/hack.apk');
        System.out.print(projectDir + '/extras/hack.apk')
        def hackAssestApkFile = new File(projectDir + '/app/src/main/assets/hack.apk');

        FileUtils.copyFile(hackApkFile, hackAssestApkFile)
        NuwaAndroidUtils.signedApk(variant, hackAssestApkFile)
    }

    @Override
    String getName() {
        return "HotfixPlugin"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if (directoryInput.file.isDirectory()) {
                    directoryInput.file.eachFileRecurse { File file ->
                        def name = file.name
                        if (name.endsWith(".class") && !name.startsWith("R\$") &&
                                !"R.class".equals(name) && !"BuildConfig.class".equals(name) && !name.endsWith("Application.class") && !name.equals("AssetUtils.class")) {

                            println name + ' is changing...'

                            ClassReader cr = new ClassReader(file.bytes)
                            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
                            ClassVisitor cv = new InjectClassVisitor(cw)

                            cr.accept(cv, EXPAND_FRAMES)

                            byte[] code = cw.toByteArray()

                            FileOutputStream fos = new FileOutputStream(
                                    file.parentFile.absolutePath + File.separator + name)
                            fos.write(code)
                            fos.close()
                        }
                    }
                }
                //处理完输入文件之后，要把输出给下一个任务
                def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->
                println "------=== jarInput.file === " + jarInput.file.getAbsolutePath()
                File tempFile = null
                if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
                    // ...对jar进行插入字节码
                }
                /**
                 * 重名输出文件,因为可能同名,会覆盖
                 */
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //处理jar进行字节码注入处理
                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}