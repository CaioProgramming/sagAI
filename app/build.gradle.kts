import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.firebase.crashlytics) // <-- ADDED CRASHLYTICS PLUGIN
}

android {
    namespace = "com.ilustris.sagai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ilustris.sagai"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.1.2"

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
            isMinifyEnabled = false // Consider enabling for release builds along with Crashlytics
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "APIKEY", apikeyProperties.getProperty("CLOUDFLARE_KEY"))
            buildConfigField("String", "ACCOUNTID", apikeyProperties.getProperty("CLOUDFLARE_ID"))
        }

        debug {
            buildConfigField("String", "APIKEY", apikeyProperties.getProperty("CLOUDFLARE_KEY"))
            buildConfigField("String", "ACCOUNTID", apikeyProperties.getProperty("CLOUDFLARE_ID"))
            // It'''s good practice to disable Crashlytics for debug builds to avoid polluting your dashboard
            // You can do this via a manifest placeholder or by checking BuildConfig.DEBUG in your Application class
            // For now, we'''ll keep it simple and add the dependency for all build types.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.android)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.androidx.core.splashscreen) // Added Splash Screen dependency

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.guava)
    implementation(libs.androidx.room.testing)
    kapt(libs.androidx.room.compiler)

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
    implementation(libs.compose.cloudy)
    implementation(libs.skydoves.balloon)
    implementation(libs.chrisbanes.haze)
    implementation(libs.chrisbanes.haze.materials)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.installations.ktx)
    implementation(libs.hypnoticcanvas)
    implementation(libs.hypnoticcanvas.shaders)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
