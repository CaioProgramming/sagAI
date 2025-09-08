import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.ilustris.sagai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ilustris.sagai"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val apikeyPropertiesFile = rootProject.file("app/config.properties")
    val apikeyProperties = Properties()
    apikeyProperties.load(FileInputStream(apikeyPropertiesFile))

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "AIKEY", apikeyProperties.getProperty("AI_KEY"))
            buildConfigField("String", "AIMODEL", apikeyProperties.getProperty("AI_MODEL"))
        }

        debug {
            buildConfigField("String", "AIKEY", apikeyProperties.getProperty("AI_KEY"))
            buildConfigField("String", "AIMODEL", apikeyProperties.getProperty("AI_MODEL"))
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
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.shapes)
    implementation(libs.androidx.animations)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.android)
    kapt(libs.androidx.hilt.compiler)

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.guava)
    implementation(libs.androidx.room.testing)
    kapt(libs.androidx.room.compiler)
    implementation(libs.material.colors)
    implementation(libs.accompanist.ui.controller)
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.coil.compose)
    implementation(libs.gson)
    implementation(libs.lottie.compose)
    implementation(libs.compose.cloudy)
    implementation(libs.skydoves.balloon)
    implementation(libs.chrisbanes.haze)
    implementation(libs.chrisbanes.haze.materials)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
