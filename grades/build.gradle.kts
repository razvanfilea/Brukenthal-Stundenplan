plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
}

android {
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        create("staging") {
            
        }
    }
}

dependencies {
    kotlin("stdlib-jdk8")

    implementation("androidx.room:room-ktx:2.4.3")
    ksp("androidx.room:room-compiler:2.4.3")
}