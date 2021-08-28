package net.theluckycoder.stundenplan.utils

sealed class NetworkResult {
    class Success : NetworkResult() {
        override fun equals(other: Any?): Boolean = this === other

        override fun hashCode(): Int = System.identityHashCode(this)
    }

    class Loading : NetworkResult() {
        override fun equals(other: Any?): Boolean = this === other

        override fun hashCode(): Int = System.identityHashCode(this)
    }

    class Fail(val reason: Reason) : NetworkResult() {
        enum class Reason {
            MissingNetworkConnection,
            DownloadFailed,
        }
    }
}
