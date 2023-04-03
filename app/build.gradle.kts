plugins {
    id("com.android.application")

    kotlin("android")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        applicationId = "ro.brukenthal.stundenplenapp"
        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target
        versionCode = Versions.App.versionCode
        versionName = Versions.App.versionName

        resourceConfigurations += listOf("en", "de", "ro")

        addManifestPlaceholders(mapOf("firebaseDisabled" to true, "crashlyticsEnabled" to false))
    }

    buildTypes {
        debug {
            extra.set("enableCrashlytics", false)
            extra.set("alwaysUpdateBuildId", false)
        }
        create("staging") {
            versionNameSuffix = "-staging"

            isDebuggable = true

            signingConfig = signingConfigs.getByName("debug")

            extra.set("enableCrashlytics", false)
            extra.set("alwaysUpdateBuildId", false)
        }
        release {
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.4"
    }
}

dependencies {
    implementation(project(":grades"))

    // Kotlin
    kotlin("stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.kotlinCoroutines}")

    // AndroidX
    implementation("androidx.activity:activity-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.core:core-splashscreen:1.0.0")

    // Compose
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("androidx.compose.foundation:foundation:1.4.0")
    implementation("androidx.compose.material:material:1.4.0")
    implementation("androidx.compose.animation:animation-graphics:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0")
    debugImplementation(kotlin("reflect"))

    // Compose Accompanist
    implementation("com.google.accompanist:accompanist-swiperefresh:${Versions.accompanist}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${Versions.accompanist}")

    // Voyager
    implementation("cafe.adriel.voyager:voyager-navigator:${Versions.voyager}")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:30.5.0"))
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    implementation("androidx.browser:browser:1.5.0")

    // Other
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.github.SmartToolFactory:Compose-Image:+")
}
