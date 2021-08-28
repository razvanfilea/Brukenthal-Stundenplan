object Versions {
    object App {
        private const val major = 1
        private const val minor = 3
        private const val patch = 0

        const val versionCode: Int = major * 100 + minor * 10 + patch
        const val versionName: String = "$major.$minor.$patch"
    }

    object Sdk {
        const val min = 21
        const val compile = 31
        const val target = 31
    }

    const val kotlin = "1.5.21"
    const val kotlinCoroutines = "1.5.1"
    const val compose = "1.0.1"
    const val accompanist = "0.17.0"
}
