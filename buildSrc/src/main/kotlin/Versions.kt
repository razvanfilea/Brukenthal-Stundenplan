import org.gradle.api.JavaVersion

object Versions {
    object App {
        private const val major = 2
        private const val minor = 7

        const val versionCode: Int = major * 10 + minor
        const val versionName: String = "$major.$minor"
    }

    object Sdk {
        const val min = 23
        const val compile = 37
        const val target = 37
    }

    val javaVersion = JavaVersion.VERSION_21
}
