buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.google.gms:google-services:4.3.13")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    // Taken from:
    // https://github.com/chrisbanes/tivi/blob/main/build.gradle
    configurations.configureEach {
        // We forcefully exclude AppCompat + MDC from any transitive dependencies.
        // This is a Compose app, so there's no need for these.
        exclude(group = "androidx.appcompat", module = "appcompat")
        exclude(group = "com.google.android.material", module = "material")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.42.0"
}

tasks.named("dependencyUpdates", com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class.java).configure {
    // Only accept stable versions
    rejectVersionIf {
        (candidate.version.contains("alpha") && !currentVersion.contains("alpha"))
                || (candidate.version.contains("beta") && !currentVersion.contains("beta"))
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
