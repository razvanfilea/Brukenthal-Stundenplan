plugins {
    id("com.android.application")

    kotlin("android")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
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

        addManifestPlaceholders(mapOf("firebaseDisabled" to true, "crashlyticsEnabled" to false))
    }

    buildTypes {
        getByName("debug") {
            isCrunchPngs = false
            extra.set("enableCrashlytics", false)
            extra.set("alwaysUpdateBuildId", false)
        }
        create("staging") {
            initWith(buildTypes.getByName("release"))
            versionNameSuffix("-staging")

            debuggable(true)
            proguardFiles("proguard-rules.pro")

            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            addManifestPlaceholders(mapOf("firebaseDisabled" to false, "crashlyticsEnabled" to true))

            minifyEnabled(true)
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    bundle {
        language.enableSplit = false
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
    kotlin("stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.kotlinCoroutines}")

    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.activity:activity-ktx:1.2.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0-alpha08")

    implementation(platform("com.google.firebase:firebase-bom:26.8.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")
    implementation("androidx.tonyodev.fetch2:xfetch2:3.1.6")
}
