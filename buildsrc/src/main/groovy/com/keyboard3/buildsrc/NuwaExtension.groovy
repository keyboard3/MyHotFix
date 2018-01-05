package com.keyboard3.buildsrc

import org.gradle.api.Project

class NuwaExtension {
    HashSet<String> includePackage = []
    HashSet<String> excludeClass = []
    HashSet<String> excludePackage = []
    boolean debugOn = false
    String patchPath
    String patchFilePrefixName = ""

    NuwaExtension(Project project) {
    }
}
