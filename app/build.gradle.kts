import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.ilustris.sagai"
    compileSdk = 36

    val versionPropsFile = rootProject.file("version.properties")
    val versionProps = Properties()
    if (versionPropsFile.exists()) {
        versionProps.load(FileInputStream(versionPropsFile))
    } else {
        logger.warn("version.properties not found at root. Falling back to default version 0.0.0")
    }
    val major = (versionProps.getProperty("MAJOR") ?: "0").toInt()
    val minor = (versionProps.getProperty("MINOR") ?: "0").toInt()
    val patch = (versionProps.getProperty("PATCH") ?: "0").toInt()
    val computedVersionName = "$major.$minor.$patch"
    val computedVersionCode = major * 10000 + minor * 100 + patch

    defaultConfig {
        applicationId = "com.ilustris.sagai"
        minSdk = 27
        targetSdk = 36
        versionCode = computedVersionCode
        versionName = computedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val apikeyPropertiesFile = rootProject.file("app/config.properties")
    val apikeyProperties = Properties()
    apikeyProperties.load(FileInputStream(apikeyPropertiesFile))

    room {
        schemaDirectory("$projectDir/schemas")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "APIKEY", apikeyProperties.getProperty("GEMINI_KEY"))
        }

        debug {
            buildConfigField("String", "APIKEY", apikeyProperties.getProperty("GEMINI_KEY"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.kotlin.reflection)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.shapes)
    implementation(libs.androidx.animations)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.media)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.android)
    implementation(libs.androidx.lifecycle.process)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.guava)
    implementation(libs.androidx.room.testing)
    ksp(libs.androidx.room.compiler)

    implementation(libs.compose.charts)

    // Retrofit & OkHttp
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.material.colors)
    implementation(libs.accompanist.ui.controller)
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    implementation(libs.gson)
    implementation(libs.lottie.compose)
    implementation(libs.face.detection)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.installations.ktx)
    implementation(libs.hypnoticcanvas)
    implementation(libs.hypnoticcanvas.shaders)
    implementation(libs.google.generativeai)
    implementation(libs.face.detection)
    implementation(libs.billing.ktx)

    implementation(libs.kotlinx.coroutines.play.services)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
