package com.etanaalemu.geo.dbbuilder

import java.io.File

fun main(args: Array<String>) {
    val rootDir = resolveProjectRoot()
    val dataDir = File(rootDir, "geo-db-builder/build/dr5hn")
    val outputDb = File(rootDir, "geo-database/src/main/assets/databases/location.db")
    val releaseTag = args.firstOrNull { !it.startsWith("--") && it.isNotBlank() }
        ?: LocationDatabaseBuilder.DEFAULT_RELEASE_TAG
    val skipDownload = args.contains("--skip-download")

    val schemaFile = File(rootDir, "geo-database/schemas/com.etanaalemu.geo.database.LocationDatabase/4.json")
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
        ),
    )

    println(
        "Built ${result.outputPath}: " +
            "${result.countryCount} countries, " +
            "${result.stateCount} states, " +
            "${result.cityCount} cities, " +
            "${result.postcodeCount} postcodes",
    )
}
