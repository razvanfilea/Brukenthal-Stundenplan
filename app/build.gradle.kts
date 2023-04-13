plugins {
    id("com.android.application")

    kotlin("android")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "net.theluckycoder.stundenplan"
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        applicationId = "ro.brukenthal.stundenplanapp"
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

    compileOptions {
        sourceCompatibility = Versions.javaVersion
        targetCompatibility = Versions.javaVersion
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    bundle.language.enableSplit = false

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":grades"))

    // Kotlin
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlin.coroutines.playServices)

    // AndroidX
    implementation(libs.androidX.activity)
    implementation(libs.androidX.dataStore)
    implementation(libs.androidX.splash)
    implementation(libs.androidX.browser)

    // Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.animationGraphics)
    implementation(libs.compose.toolingPreview)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    debugImplementation(libs.compose.tooling)
    debugImplementation(libs.kotlin.reflect)

    // Voyager
    implementation(libs.voyager.navigator)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:31.4.0"))
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Other
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.github.SmartToolFactory:Compose-Image:+")
}
