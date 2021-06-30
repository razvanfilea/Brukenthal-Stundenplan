plugins {
    id("com.android.application")

    kotlin("android")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        applicationId = "net.theluckycoder.stundenplan"
        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target
        versionCode = Versions.App.versionCode
        versionName = Versions.App.versionName

        resourceConfigurations += listOf("en", "de", "ro")

        addManifestPlaceholders(mapOf("firebaseDisabled" to true, "crashlyticsEnabled" to false))
    }

    buildTypes {
        getByName("debug") {
            extra.set("enableCrashlytics", false)
            extra.set("alwaysUpdateBuildId", false)
        }
        create("staging") {
            initWith(buildTypes.getByName("debug"))
            versionNameSuffix("-staging")

            debuggable(true)

            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            addManifestPlaceholders(mapOf("firebaseDisabled" to false, "crashlyticsEnabled" to true))

            isMinifyEnabled = true

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

    kotlinOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures.viewBinding = true
}

dependencies {
    kotlin("stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.kotlinCoroutines}")

    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.activity:activity-ktx:1.2.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0-rc01")

    implementation(platform("com.google.firebase:firebase-bom:28.2.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")
    implementation("androidx.tonyodev.fetch2:xfetch2:3.1.6")
}
