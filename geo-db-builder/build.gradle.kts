import org.gradle.process.CommandLineArgumentProvider

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

application {
    mainClass.set("com.etanaalemu.geo.dbbuilder.DatabaseBuilderMainKt")
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(libs.gson)
    implementation(libs.sqlite.jdbc)
    testImplementation(libs.junit)
}

tasks.register<JavaExec>("generateLocationDatabase") {
    group = "geo"
    description =
        "Build location.db into geo-database assets. Optional: -Pdr5hnReleaseTag=vX.Y-export.Z, -Pdr5hnSkipDownload"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(application.mainClass)
    dependsOn(tasks.classes)
    argumentProviders.add(
        object : CommandLineArgumentProvider {
            override fun asArguments(): Iterable<String> {
                val out = mutableListOf<String>()
                val tag = project.findProperty("dr5hnReleaseTag") as String?
                if (!tag.isNullOrBlank()) out.add(tag.trim())
                if (project.hasProperty("dr5hnSkipDownload")) out.add("--skip-download")
                return out
            }
        },
    )
}

tasks.register<JavaExec>("generateLocationLiteDatabase") {
    group = "geo"
    description =
        "Build location-lite.db into geo-database-lite assets. Optional: -Pdr5hnReleaseTag=…, -Pdr5hnSkipDownload"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.etanaalemu.geo.dbbuilder.DatabaseLiteBuilderMainKt")
    dependsOn(tasks.classes)
    argumentProviders.add(
        object : CommandLineArgumentProvider {
            override fun asArguments(): Iterable<String> {
                val out = mutableListOf<String>()
                val tag = project.findProperty("dr5hnReleaseTag") as String?
                if (!tag.isNullOrBlank()) out.add(tag.trim())
                if (project.hasProperty("dr5hnSkipDownload")) out.add("--skip-download")
                return out
            }
        },
    )
}
