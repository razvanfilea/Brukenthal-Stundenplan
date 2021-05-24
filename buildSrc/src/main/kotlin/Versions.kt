object Versions {
    object App {
        private const val major = 1
        private const val minor = 2
        private const val patch = 1

        const val versionCode: Int = major * 100 + minor * 10 + patch
        const val versionName: String = "$major.$minor.$patch"
    }

    object Sdk {
        const val min = 21
        const val compile = 30
        const val target = 30
    }

    const val kotlin = "1.5.10"
    const val kotlinCoroutines = "1.5.0"
}
