package net.theluckycoder.stundenplan.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.model.NetworkResult
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.ui.LocalSnackbarHostState
import net.theluckycoder.stundenplan.ui.pdf_rendering.PDFDecoder
import net.theluckycoder.stundenplan.ui.pdf_rendering.PDFRegionDecoder
import net.theluckycoder.stundenplan.viewmodel.HomeViewModel


class TimetableScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = viewModel<HomeViewModel>()
        HomeContent(viewModel)
    }
}


@Composable
private fun HomeContent(
    viewModel: HomeViewModel,
) {
    val snackbarHostState = LocalSnackbarHostState.current

    val networkResult by viewModel.networkStateFlow.collectAsState()
    val isRefreshing = networkResult is NetworkResult.Loading
    val refreshState = rememberPullToRefreshState()

    val actionRetry = stringResource(id = R.string.action_retry)
    val missingNetworkError = stringResource(id = R.string.error_network_connection)
    val downloadFailed = stringResource(id = R.string.error_download_failed)

    LaunchedEffect(networkResult) {
        val result = networkResult
        if (result is NetworkResult.Fail) {
            val message = when (result.reason) {
                NetworkResult.Fail.Reason.MissingNetworkConnection -> missingNetworkError
                NetworkResult.Fail.Reason.DownloadFailed -> downloadFailed
            }

            val snackbarResult = snackbarHostState.showSnackbar(message, actionRetry)
            if (snackbarResult == SnackbarResult.ActionPerformed) {
                viewModel.refresh()
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        state = refreshState,
        modifier = Modifier.fillMaxSize(),
    ) {
        val timetableState by viewModel.timetableFile.collectAsState(null to true)
        val (file, darkMode) = timetableState

        if (file != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    SubsamplingScaleImageView(context).apply {
                        setMinimumTileDpi(160)
                    }
                },
                update = { view ->
                    val (lastFile, lastDarkMode) = view.tag as? Pair<*, *> ?: (null to null)

                    if (lastFile != file || lastDarkMode != darkMode) {
                        view.tag = file to darkMode

                        view.setBitmapDecoderFactory { PDFDecoder(0, file, MAX_ZOOM_FACTOR, darkMode) }
                        view.setRegionDecoderFactory {
                            PDFRegionDecoder(
                                0,
                                file,
                                MAX_ZOOM_FACTOR,
                                darkMode
                            )
                        }
                        view.setImage(ImageSource.uri(file.absolutePath), view.state)
                    }
                }
            )
        }
    }
}

private const val MAX_ZOOM_FACTOR = 5f
