package net.theluckycoder.stundenplan.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.math.roundToInt


class TimetableScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = viewModel<HomeViewModel>()

        Scaffold(bottomBar = {
            val timetableType by viewModel.timetableStateFlow.collectAsState()

            AnimatedVisibility(
                visible = viewModel.showAppBarState.value,
                enter = expandVertically(expandFrom = Alignment.Bottom),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                BottomBar(
                    timetableType = timetableType,
                    onTimetableChange = { newTimetableType ->
                        viewModel.switchTimetableType(newTimetableType)
                    }
                )
            }
        }) { padding ->
            Box(Modifier.padding(padding)) {
                HomeContent(viewModel)
            }
        }
    }

    @Composable
    private fun BottomBar(
        timetableType: TimetableType,
        onTimetableChange: (newTimetable: TimetableType) -> Unit
    ) {
        BottomAppBar(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
        ) {
            val highSchoolSelected = timetableType == TimetableType.HIGH_SCHOOL
            val middleSchoolSelected = highSchoolSelected.not()

            BottomNavigationItem(
                selected = highSchoolSelected,
                onClick = { onTimetableChange(TimetableType.HIGH_SCHOOL) },
                icon = {
                    Text(
                        text = stringResource(id = R.string.high_school),
                        color = if (highSchoolSelected) MaterialTheme.colors.secondaryVariant else Color.Unspecified,
                        fontWeight = FontWeight.Bold.takeIf { highSchoolSelected },
                    )
                }
            )
            BottomNavigationItem(
                selected = middleSchoolSelected,
                onClick = { onTimetableChange(TimetableType.MIDDLE_SCHOOL) },
                icon = {
                    Text(
                        text = stringResource(id = R.string.middle_school),
                        color = if (middleSchoolSelected) MaterialTheme.colors.secondaryVariant else Color.Unspecified,
                        fontWeight = FontWeight.Bold.takeIf { middleSchoolSelected },
                    )
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HomeContent(
    viewModel: HomeViewModel,
) = BoxWithConstraints(Modifier.fillMaxSize()) {
    val snackbarHostState = LocalSnackbarHostState.current
    val renderWidth = with(LocalDensity.current) { minOf(maxWidth, maxHeight).roundToPx() }

    val networkResult by viewModel.networkStateFlow.collectAsState()
    val isRefreshing = networkResult is NetworkResult.Loading
    val refreshState = rememberPullRefreshState(isRefreshing, onRefresh = {})

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(refreshState),
    ) {
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = refreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            contentColor = MaterialTheme.colors.secondary
        )

        val darkMode by viewModel.darkThemeFlow.collectAsState(true)
        val fileState = viewModel.timetableFile.collectAsState(null)

        val file = fileState.value
        if (file != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    SubsamplingScaleImageView(context).apply {

                        setMinimumTileDpi(160)

                        setBitmapDecoderFactory { PDFDecoder(0, file, MAX_ZOOM_FACTOR, darkMode) }
                        setRegionDecoderFactory { PDFRegionDecoder(0, file, MAX_ZOOM_FACTOR, darkMode) }
                        setImage(ImageSource.uri(file.absolutePath))
                    }
                },
                update = { view ->
                    view.setBitmapDecoderFactory { PDFDecoder(0, file, MAX_ZOOM_FACTOR, darkMode) }
                    view.setRegionDecoderFactory { PDFRegionDecoder(0, file, MAX_ZOOM_FACTOR, darkMode) }
                    view.setImage(ImageSource.uri(file.absolutePath))
                }
            )
        }
    }
}

private const val MAX_ZOOM_FACTOR = 5f
