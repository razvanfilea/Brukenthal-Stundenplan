import org.gradle.api.JavaVersion

object Versions {
    object App {
        private const val major = 2
        private const val minor = 0

        const val versionCode: Int = major * 10 + minor * 1
        const val versionName: String = "$major.$minor"
    }

    object Sdk {
        const val min = 21
        const val compile = 33
        const val target = 33
    }

    val javaVersion = JavaVersion.VERSION_17
}
