package com.etanaalemu.geo.dbbuilder

import java.io.File

internal fun resolveProjectRoot(): File {
    var dir = File(System.getProperty("user.dir"))
    while (dir != null) {
        if (File(dir, "settings.gradle.kts").exists()) return dir
        dir = dir.parentFile
    }
    error("Could not find project root (settings.gradle.kts)")
}

internal fun readRoomIdentityHash(schemaJson: File): String? {
    val text = schemaJson.readText()
    val regex = """"identityHash"\s*:\s*"([^"]+)"""".toRegex()
    return regex.find(text)?.groupValues?.get(1)
}
