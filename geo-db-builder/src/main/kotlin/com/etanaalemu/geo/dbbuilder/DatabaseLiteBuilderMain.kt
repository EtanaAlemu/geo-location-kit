package com.etanaalemu.geo.dbbuilder

import java.io.File

fun main(args: Array<String>) {
    val rootDir = resolveProjectRoot()
    val dataDir = File(rootDir, "geo-db-builder/build/dr5hn")
    val outputDb = File(rootDir, "geo-database-lite/src/main/assets/databases/location-lite.db")
    val releaseTag = args.firstOrNull { !it.startsWith("--") && it.isNotBlank() }
        ?: LocationDatabaseBuilder.DEFAULT_RELEASE_TAG
    val skipDownload = args.contains("--skip-download")

    val schemaFile = File(
        rootDir,
        "geo-database-lite/schemas/com.etanaalemu.geo.database.lite.LocationLiteDatabase/3.json",
    )
    val identityHash = if (schemaFile.exists()) {
        readRoomIdentityHash(schemaFile)
    } else {
        null
    }

    val result = LocationDatabaseBuilder().build(
        LocationDatabaseBuilder.Config(
            releaseTag = releaseTag,
            dataDir = dataDir,
            outputDb = outputDb,
            skipDownload = skipDownload,
            roomIdentityHash = identityHash,
            useNestedCombinedJson = true,
            includeCities = false,
        ),
    )

    println(
        "Built lite ${result.outputPath}: " +
            "${result.countryCount} countries, " +
            "${result.stateCount} states " +
            "(no cities)",
    )
}
