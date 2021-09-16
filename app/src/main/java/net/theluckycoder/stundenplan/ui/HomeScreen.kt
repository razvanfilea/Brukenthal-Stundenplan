package net.theluckycoder.stundenplan.ui

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.theluckycoder.stundenplan.BuildConfig
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.extensions.browseUrl
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.notifications.NetworkResult
import net.theluckycoder.stundenplan.utils.Analytics
import net.theluckycoder.stundenplan.utils.UpdateChecker
import net.theluckycoder.stundenplan.viewmodel.HomeViewModel
import kotlin.math.ceil

class HomeActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = ComposeView(this)
        view.setContent {
            val isDark by viewModel.darkThemeFlow.collectAsState(initial = true)

            AppTheme(isDark = isDark) {
                val systemUiController = rememberSystemUiController()

                val primaryVariantColor = MaterialTheme.colors.primaryVariant
                SideEffect {
                    systemUiController.setSystemBarsColor(primaryVariantColor, darkIcons = true)
                }

                HomeScreen(viewModel = viewModel)
            }
        }

        setContentView(view)

        // For Firebase Analytics
        if (intent.getBooleanExtra(ARG_OPENED_FROM_NOTIFICATION, false))
            Analytics.openNotificationEvent()
    }

    companion object {
        const val ARG_OPENED_FROM_NOTIFICATION = "opened_from_notification"
        const val APP_STORE_URL =
            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val timetableType = viewModel.timetableFlow.collectAsState()
    val scaffoldState = rememberScaffoldState()

    val isUpdateNeeded = remember { UpdateChecker.isUpdateNeeded() }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AnimatedVisibility(
                visible = viewModel.showAppBar.value,
                enter = expandVertically(Alignment.Top),
                exit = shrinkVertically(Alignment.Top)
            ) {
                TopBar(viewModel)
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = viewModel.showAppBar.value,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                BottomBar(
                    timetableType = timetableType.value,
                    onTimetableChange = { newTimetableType ->
                        viewModel.switchTimetableType(newTimetableType)
                    }
                )
            }
        }
    ) {
        HomeContent(viewModel, scaffoldState.snackbarHostState)
    }

    if (isUpdateNeeded && !viewModel.hasSeenUpdateDialog.value) {
        val ctx = LocalContext.current

        UpdateDialog(
            onDismiss = {
                viewModel.hasSeenUpdateDialog.value = true
            },
            onConfirm = {
                viewModel.hasSeenUpdateDialog.value = true
                ctx.browseUrl(HomeActivity.APP_STORE_URL)
            }
        )
    }
}

@Composable
private fun TopBar(
    viewModel: HomeViewModel
) {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.primary,
        title = {
            Text(text = stringResource(id = R.string.activity_title))
        },
        actions = {
            val darkThemeFlow = viewModel.darkThemeFlow.collectAsState(initial = true)
            IconButton(onClick = {
                viewModel.switchTheme(!darkThemeFlow.value)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_switch_theme),
                    contentDescription = null
                )
            }
            IconButton(onClick = {
                viewModel.refresh()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = null,
                )
            }
        }
    )
}

@Composable
private fun HomeContent(
    viewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState
) = BoxWithConstraints(Modifier.fillMaxSize()) {
    val width = with(LocalDensity.current) { maxWidth.toPx() }

    val networkResult by viewModel.networkFlow.collectAsState()
    val swipeState =
        rememberSwipeRefreshState(isRefreshing = networkResult is NetworkResult.Loading)

    val missingNetworkError = stringResource(id = R.string.error_network_connection)
    val downloadFailed = stringResource(id = R.string.error_download_failed)

    LaunchedEffect(networkResult) {
        @Suppress("UnnecessaryVariable") val result = networkResult
        if (result is NetworkResult.Fail) {
            val message = when (result.reason) {
                NetworkResult.Fail.Reason.MissingNetworkConnection -> missingNetworkError
                NetworkResult.Fail.Reason.DownloadFailed -> downloadFailed
            }

            snackbarHostState.showSnackbar(message)
        }
    }

    SwipeRefresh(
        modifier = Modifier.fillMaxWidth(),
        state = swipeState,
        onRefresh = { },
        swipeEnabled = false,
    ) {
        val renderingError = stringResource(id = R.string.error_rendering_failed)

        val timetableType by viewModel.timetableFlow.collectAsState()
        val darkMode by viewModel.darkThemeFlow.collectAsState(true)

        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(width, timetableType, networkResult, ceil(scale).toInt(), darkMode) {
            try {
                bitmap = viewModel.renderPdf(width.toInt(), ceil(scale), darkMode)
            } catch (_: OutOfMemoryError) {
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(renderingError)
                e.printStackTrace()
            }
        }

        LaunchedEffect(timetableType) {
            scale = 1f
            offset = Offset.Zero
        }

        if (bitmap != null) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4.5f)
                            offset += (pan * scale)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                viewModel.showAppBar.value = !viewModel.showAppBar.value
                            },
                            onDoubleTap = {
                                scale = when {
                                    scale > 1f -> 1f
                                    else -> 2.5f
                                }
                            }
                        )
                    },
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null
            )
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

@Preview
@Composable
fun BottomBarPreview() {
    var timetableType by remember { mutableStateOf(TimetableType.MIDDLE_SCHOOL) }

    AppTheme(isDark = true) {
        BottomBar(timetableType = timetableType) { newTimetableType: TimetableType ->
            timetableType = newTimetableType
        }
    }
}

@Composable
private fun UpdateDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.update_available)) },
        text = { Text(text = stringResource(id = R.string.update_available_desc)) },
        buttons = {
            Row(Modifier.fillMaxWidth().padding(16.dp)) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss,
                ) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onConfirm,
                ) {
                    Text(text = stringResource(id = R.string.action_update))
                }
            }
        }
    )
}
