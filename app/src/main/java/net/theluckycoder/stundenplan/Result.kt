package net.theluckycoder.stundenplan

import android.net.Uri

sealed class Result {
    class Success(val fileUri: Uri) : Result()
    class Loading(val indeterminate: Boolean, val progress: Int) : Result()
    class Failed : Result()
}
