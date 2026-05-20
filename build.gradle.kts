// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.maven.publish) apply false
}

val locationDbOutput = rootProject.layout.projectDirectory.file(
    "geo-database/src/main/assets/databases/location.db",
)

val locationLiteDbOutput = rootProject.layout.projectDirectory.file(
    "geo-database-lite/src/main/assets/databases/location-lite.db",
)

val generateLocationDatabase = tasks.register("generateLocationDatabase") {
    group = "geo"
    description =
        "Downloads dr5hn JSON and builds location.db. Use -PforceDbGeneration to rebuild; -Pdr5hnReleaseTag=vX.Y for a specific release (see README)."
    dependsOn(":geo-db-builder:generateLocationDatabase")
    onlyIf {
        !locationDbOutput.asFile.exists() || project.hasProperty("forceDbGeneration")
    }
}

val generateLocationLiteDatabase = tasks.register("generateLocationLiteDatabase") {
    group = "geo"
    description =
        "Downloads dr5hn JSON and builds location-lite.db. Use -PforceDbGeneration; optional -Pdr5hnReleaseTag (see README)."
    dependsOn(":geo-db-builder:generateLocationLiteDatabase")
    onlyIf {
        !locationLiteDbOutput.asFile.exists() || project.hasProperty("forceDbGeneration")
    }
}

project(":geo-database").tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(generateLocationDatabase)
}

project(":geo-database-lite").tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(generateLocationLiteDatabase)
}
