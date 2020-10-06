plugins {
    id("com.android.application")

    kotlin("android")

    id("com.google.gms.google-services")
}

android {
    compileSdkVersion(Versions.Sdk.compile)

    defaultConfig {
        applicationId("net.theluckycoder.stundenplan")
        minSdkVersion(Versions.Sdk.min)
        targetSdkVersion(Versions.Sdk.target)
        versionCode(Versions.App.versionCode)
        versionName(Versions.App.versionName)
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders(mapOf("firebaseDisabled" to true))

            extra.set("enableCrashlytics", false)
            extra.set("alwaysUpdateBuildId", false)
            isCrunchPngs = false
        }
        getByName("release") {
            manifestPlaceholders(mapOf("firebaseDisabled" to false))

            isMinifyEnabled = true
            isShrinkResources = true
            isZipAlignEnabled = true

            ndk {
                abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.kotlinCoroutines}")

    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.2.1")
    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.androidxLifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.androidxLifecycle}")

    implementation("com.google.firebase:firebase-analytics-ktx:17.5.0")
    implementation("com.google.firebase:firebase-config-ktx:19.2.0")
    implementation("com.google.firebase:firebase-messaging-ktx:20.3.0")

    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")
    implementation("androidx.tonyodev.fetch2:xfetch2:3.1.5")
}
