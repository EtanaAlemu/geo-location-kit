import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

fun loadLocalProperties(rootDir: java.io.File): Properties {
    val props = Properties()
    val file = java.io.File(rootDir, "local.properties")
    if (file.exists()) {
        file.inputStream().use { props.load(it) }
    }
    return props
}

fun resolveProperty(
    name: String,
    localProps: Properties,
    providers: org.gradle.api.provider.ProviderFactory,
): String? =
    providers.gradleProperty(name).orNull
        ?: localProps.getProperty(name)
        ?: when (name) {
            "gpr.user" -> System.getenv("GITHUB_ACTOR")
            "gpr.key" -> System.getenv("GITHUB_TOKEN")
            else -> null
        }

val localProps = loadLocalProperties(settingsDir)
val gprUser = resolveProperty("gpr.user", localProps, providers)
val gprKey = resolveProperty("gpr.key", localProps, providers)

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        if (!gprUser.isNullOrBlank() && !gprKey.isNullOrBlank()) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/EtanaAlemu/geo-location-kit")
                credentials {
                    username = gprUser
                    password = gprKey
                }
            }
        }
    }
}

rootProject.name = "Geo Location Kit"
include(":app")
include(":geo-core")
include(":geo-database-common")
include(":geo-database")
include(":geo-database-lite")
include(":geo-compose")
include(":geo-db-builder")
