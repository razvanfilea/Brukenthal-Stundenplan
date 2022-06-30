object Versions {
    object App {
        private const val major = 1
        private const val minor = 5
        private const val patch = 0

        const val versionCode: Int = major * 100 + minor * 10 + patch
        const val versionName: String = "$major.$minor.$patch"
    }

    object Sdk {
        const val min = 21
        const val compile = 32
        const val target = 31
    }

    const val kotlin = "1.7.0"
    const val kotlinCoroutines = "1.6.3"
    const val compose = "1.2.0"
    const val accompanist = "0.24.13-rc"
    const val voyager = "1.0.0-rc02"
}
