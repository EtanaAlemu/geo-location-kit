plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

apply(from = rootProject.file("gradle/publish-library.gradle"))

android {
    namespace = "com.etanaalemu.geo.database.common"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(project(":geo-core"))
    implementation(libs.gson)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.kotlinx.coroutines.android)
}
