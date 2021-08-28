buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
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
    id("com.github.ben-manes.versions") version "0.39.0"
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
