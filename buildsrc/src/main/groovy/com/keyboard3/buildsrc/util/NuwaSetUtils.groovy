package com.keyboard3.buildsrc.util

class NuwaSetUtils {
    public
    static boolean isExcluded(String path, Set<String> excludePackage, Set<String> excludeClass) {
        for (String exclude:excludeClass){
            if(path.equals(exclude)) {
                return  true;
            }
        }
        for (String exclude:excludePackage){
            if(path.startsWith(exclude)) {
                return  true;
            }
        }

        return false;
    }

    public static boolean isIncluded(String path, Set<String> includePackage) {
        if (includePackage.size() == 0) {
            return true
        }

        for (String include:includePackage){
            if(path.startsWith(include)) {
                return  true;
            }
        }

        return false;
    }
}
