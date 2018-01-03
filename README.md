# MyHotFix
通过反射DexClassLoader/PathClassLoader实现热修复

# Application 启动完成补丁安装
加载指定应用目录下 cache/patch_dex.jar ，反射 DexClassLoader 和 PathClassLoader 拿到 DexElements 合并（注意补丁在前）。然后将合并结果设置回 PathClassLoader
```java
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

    Field pathListField = findField(this.getClassLoader(), "pathList");
    Object pathPathList = pathListField.get(this.getClassLoader());
    Field pathjlrField = findField(pathPathList, "dexElements");
    Object[] orginalDexElements = (Object[]) pathjlrField.get(pathPathList);

    Object[] combined = (Object[]) Array.newInstance(
            orginalDexElements.getClass().getComponentType(), orginalDexElements.length + PatchDexElements.length);
    System.arraycopy(PatchDexElements, 0, combined, 0, PatchDexElements.length);
    System.arraycopy(orginalDexElements, 0, combined, PatchDexElements.length, orginalDexElements.length);
    pathjlrField.set(pathPathList, combined);
    Log.d(TAG, "补丁加入成功");
} catch (NoSuchFieldException e) {
    e.printStackTrace();
} catch (IllegalAccessException e) {
    e.printStackTrace();
}
```
# python 脚本生成补丁放入 模拟器内
获取 Android 工程下的打包产出 class ，通过执行 dx 打包命令打成 patch_dex.jar<br>
注意：生成补丁 class 不要执行 run，执行 buildApk 就行了。
```python
#coding:utf-8
import os

sourcehost="/Users/ganchunyu/work/AndroidProject/MyHotFix"
classfullName="com.keyboard3.myhotfix.MainActivity";
index=classfullName.rfind(".");
nohandle=classfullName[:index];
handle=nohandle.replace(".","/");
fileName=classfullName[index+1:]+".class";
outputClassPath=sourcehost+"/app/build/intermediates/classes/debug/"+handle+"/"+fileName;
targetHost="/Users/ganchunyu/work/workspace/patch";
targetPatch=targetHost+handle;

int1="cp "+outputClassPath+" "+targetPatch;
int2="cd "+targetHost;
int3="jar cvf "+targetHost+"/path.jar "+targetHost;
int4="dx --dex --output="+targetHost+"/path_dex.jar "+targetHost+"/path.jar";
int5="adb push "+targetHost+"/path_dex.jar /sdcard/Android/data/"+nohandle+"/cache";
print int1
os.popen(int1);
print int2
os.popen(int2);
print int3
os.popen(int3);
print int4
os.popen(int4);
print int5
os.popen(int5);
```
# 步骤
- 运行带 bug 的Apk
<img src="images/bug.png" width="500">
- 生成修复的补丁安装
- 重启应用
<img src="images/ok.png" width="500">