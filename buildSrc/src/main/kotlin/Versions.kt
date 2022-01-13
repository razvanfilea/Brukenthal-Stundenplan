object Versions {
    object App {
        private const val major = 1
        private const val minor = 4
        private const val patch = 0

        const val versionCode: Int = major * 100 + minor * 10 + patch
        const val versionName: String = "$major.$minor.$patch"
    }

    object Sdk {
        const val min = 21
        const val compile = 31
        const val target = 31
    }

    const val kotlin = "1.6.10"
    const val kotlinCoroutines = "1.6.0"
    const val compose = "1.1.0-rc01"
    const val composeCompiler = "1.1.0-rc02"
    const val accompanist = "0.22.0-rc"
    const val voyager = "1.0.0-beta14"
}
