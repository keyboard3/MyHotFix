package com.keyboard3.myhotfix;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

/**
 * @author keyboard3 on 2018/1/3
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        //创建路径文件夹
        File externalCacheDir = getApplicationContext().getExternalCacheDir();
        if (!externalCacheDir.exists()) {
            externalCacheDir.mkdirs();
        }
        initial(externalCacheDir);
        installPatch(getApplicationContext().getExternalCacheDir() + "/" + "path_dex.jar");
    }

    public void initial(File externalCacheDir) {
        String dexPath = null;
        try {
            dexPath = copyAsset(this, "hack.apk", externalCacheDir);
        } catch (IOException e) {
            Log.e(TAG, "copy hack.apk failed");
        }
        installPatch(dexPath);
    }

    private void installPatch(String patchFile) {
        //拿补丁
        File file = new File(patchFile);
        if (file.exists()) {
            Log.d(TAG, "file.exists()");
            //存在就加载，并通过反射插入到pathClassLoader的DexElements
            File dexOpt = this.getDir("dexOpt", MODE_PRIVATE);
            final DexClassLoader dexClassloader = new DexClassLoader(
                    patchFile,
                    dexOpt.getAbsolutePath(),
                    null,
                    this.getClassLoader());
            try {
                //拿到dexElements
                Field dexListField = findField(dexClassloader, "pathList");
                Object dexPathList = dexListField.get(dexClassloader);
                Field dexjlrField = findField(dexPathList, "dexElements");
                Object[] PatchDexElements = (Object[]) dexjlrField.get(dexPathList);

                //原生修复
                Field pathListField = findField(this.getClassLoader(), "pathList");
                Object pathPathList = pathListField.get(this.getClassLoader());
                Field pathjlrField = findField(pathPathList, "dexElements");
                Object[] orginalDexElements = (Object[]) pathjlrField.get(pathPathList);

                Object[] combined = (Object[]) Array.newInstance(
                        orginalDexElements.getClass().getComponentType(), orginalDexElements.length + PatchDexElements.length);
                System.arraycopy(PatchDexElements, 0, combined, 0, PatchDexElements.length);
                System.arraycopy(orginalDexElements, 0, combined, PatchDexElements.length, orginalDexElements.length);
                pathjlrField.set(pathPathList, combined);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);


                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    public static void moveAsset(Context context, String fileName) throws IOException {
        File externalCacheDir = context.getExternalCacheDir();
        if (!externalCacheDir.exists()) {
            externalCacheDir.mkdirs();
        }
        copyAsset(context, fileName, externalCacheDir);
    }

    public static void deltePatch(Context context, String fileName) {
        File file = new File(context.getExternalCacheDir() + "/" + "path_dex.jar");
        file.delete();
    }

    private static String copyAsset(Context context, String assetName, File dir) throws IOException {
        File outFile = new File(dir, assetName);
        if (!outFile.exists()) {
            AssetManager assetManager = context.getAssets();
            InputStream in = assetManager.open(assetName);
            OutputStream out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            out.close();
        }
        return outFile.getAbsolutePath();
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
