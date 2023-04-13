package net.theluckycoder.stundenplan.ui.screen

import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import com.smarttoolfactory.image.zoom.EnhancedZoomableImage
import com.smarttoolfactory.image.zoom.rememberEnhancedZoomState
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.model.NetworkResult
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.ui.LocalSnackbarHostState
import net.theluckycoder.stundenplan.viewmodel.HomeViewModel
import kotlin.math.roundToInt

class HomeScreen : Screen {

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
    val screenWidth = with(LocalDensity.current) { maxWidth.roundToPx() }
    val screenHeight = with(LocalDensity.current) { maxHeight.roundToPx() }
    val renderWidth = minOf(screenWidth, screenHeight)

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

        val renderingError = stringResource(id = R.string.error_rendering_failed)

        val timetableType by viewModel.timetableStateFlow.collectAsState()
        val darkMode by viewModel.darkThemeFlow.collectAsState(true)

        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        val zoomableState = rememberEnhancedZoomState(
            imageSize = IntSize(bitmap?.width ?: 0, bitmap?.height ?: 0),
            maxZoom = 4.5f,
            moveToBounds = true,
            fling = false,
            key1 = timetableType,
        )

        val roundedScale = zoomableState.zoom.roundToInt()
        LaunchedEffect(renderWidth, timetableType, networkResult, roundedScale, darkMode) {
            try {
                bitmap = viewModel.renderPdf(renderWidth, roundedScale.coerceAtMost(2), darkMode)
            } catch (_: OutOfMemoryError) {
            } catch (e: Exception) {
                if (networkResult !is NetworkResult.Loading) {
                    snackbarHostState.showSnackbar(renderingError)
                    e.printStackTrace()
                }
            }
        }

        if (bitmap != null) {
            EnhancedZoomableImage(
                modifier = Modifier.fillMaxSize(),
                imageBitmap = bitmap!!.asImageBitmap(),
                moveToBounds = true,
                fling = false,
                enhancedZoomState =zoomableState
            )
        }
    }
}
