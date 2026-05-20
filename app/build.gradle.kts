import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

fun localProperty(name: String): String? {
    val file = rootProject.file("local.properties")
    if (!file.exists()) return null
    return Properties().apply { file.inputStream().use { load(it) } }.getProperty(name)
}

val usePublishedGeo =
    (findProperty("geo.usePublished") as String? ?: localProperty("geo.usePublished")) == "true"
val publishedGeoVersion =
    findProperty("geo.version") as String? ?: localProperty("geo.version") ?: "1.0.0"
// 1.0.0 on GitHub Packages uses com.etanaalemu.geo; use io.github.etanaalemu after Maven Central publish
val publishedGeoGroup =
    findProperty("geo.publishedGroup") as String?
        ?: localProperty("geo.publishedGroup")
        ?: "com.etanaalemu.geo"

android {
    namespace = "com.etanaalemu.demo"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.etanaalemu.demo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    if (usePublishedGeo) {
        implementation("$publishedGeoGroup:geo-compose:$publishedGeoVersion")
    } else {
        implementation(project(":geo-compose"))
    }
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

if (usePublishedGeo) {
    val hasGpr = !localProperty("gpr.user").isNullOrBlank() && !localProperty("gpr.key").isNullOrBlank()
    val source = when {
        publishedGeoGroup == "io.github.etanaalemu" && hasGpr ->
            "Maven Central and/or GitHub Packages"
        publishedGeoGroup == "io.github.etanaalemu" ->
            "Maven Central (not published yet? use geo.publishedGroup=com.etanaalemu.geo + gpr for GitHub Packages 1.0.0)"
        hasGpr -> "GitHub Packages"
        else -> "UNCONFIGURED — add gpr.user and gpr.key to local.properties for GitHub Packages"
    }
    logger.lifecycle(
        "Demo app uses published $publishedGeoGroup:geo-compose:$publishedGeoVersion ($source).",
    )
}
