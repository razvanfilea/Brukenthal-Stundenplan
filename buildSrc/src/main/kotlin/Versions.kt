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

    const val kotlin = "1.7.10"
    const val kotlinCoroutines = "1.6.4"
    const val compose = "1.2.1"
    const val accompanist = "0.25.1"
    const val voyager = "1.0.0-rc02"
}
