package net.theluckycoder.stundenplan.utils

import android.net.Uri

sealed class NetworkResult {
    class Success(val fileUri: Uri) : NetworkResult()
    class Loading(val indeterminate: Boolean, val progress: Int) : NetworkResult()
    class Failed(val stringRes: Int) : NetworkResult()
}
