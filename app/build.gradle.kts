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

        resConfigs("en", "de")
    }

    buildTypes {
        getByName("debug") {
            addManifestPlaceholders(mapOf("firebaseDisabled" to true))

            isCrunchPngs = false
        }
        getByName("release") {
            addManifestPlaceholders(mapOf("firebaseDisabled" to false))

            isMinifyEnabled = true
            isShrinkResources = true
            isZipAlignEnabled = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    bundle {
        language {
            enableSplit = false
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
    implementation("androidx.datastore:datastore-preferences:1.0.0-alpha01")

    implementation("com.google.firebase:firebase-analytics-ktx:17.6.0")
    implementation("com.google.firebase:firebase-config-ktx:19.2.0")
    implementation("com.google.firebase:firebase-messaging-ktx:20.3.0")

    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")
    implementation("androidx.tonyodev.fetch2:xfetch2:3.1.5")
}
