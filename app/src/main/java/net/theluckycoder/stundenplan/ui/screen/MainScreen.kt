package net.theluckycoder.stundenplan.ui.screen

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.extensions.browseUrl
import net.theluckycoder.stundenplan.ui.LocalSnackbarHostState
import net.theluckycoder.stundenplan.utils.UpdateChecker
import net.theluckycoder.stundenplan.viewmodel.HomeViewModel

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalAnimationGraphicsApi::class
)
object MainScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = viewModel<HomeViewModel>()
        val scope = rememberCoroutineScope()

        Navigator(HomeScreen()) { navigator ->
            val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)

            BackdropScaffold(
                modifier = Modifier.safeDrawingPadding(),
                scaffoldState = scaffoldState,
                appBar = { TopBar(viewModel, scaffoldState) },
                backLayerContent = {
                    fun replaceScreen(screen: Screen) {
                        navigator.replace(screen)
                        scope.launch { scaffoldState.conceal() }
                    }

                    Column(Modifier.padding(8.dp)) {
                        BackdropButton(
                            icon = painterResource(R.drawable.ic_timetable),
                            text = stringResource(R.string.menu_timetable)
                        ) {
                            replaceScreen(HomeScreen())
                        }

                        val ctx = LocalContext.current
                        BackdropButton(
                            icon = painterResource(R.drawable.ic_news),
                            text = stringResource(R.string.menu_news)
                        ) {
                            try {
                                CustomTabsIntent.Builder()
                                    .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                                    .build()
                                    .launchUrl(ctx, Uri.parse(NEWS_URL))
                            } catch (e: ActivityNotFoundException) {
                                ctx.browseUrl(NEWS_URL)
                            }
                        }

                        BackdropButton(
                            icon = painterResource(R.drawable.ic_grades),
                            text = stringResource(R.string.menu_grades)
                        ) {
                            replaceScreen(GradesScreen())
                        }

                        BackdropButton(
                            icon = painterResource(R.drawable.ic_about),
                            text = stringResource(R.string.menu_about)
                        ) {
                            replaceScreen(AboutScreen())
                        }
                    }
                },
                frontLayerContent = {
                    CompositionLocalProvider(LocalSnackbarHostState provides scaffoldState.snackbarHostState) {
                        CurrentScreen()
                    }
                },
                gesturesEnabled = !(navigator.lastItem is HomeScreen && scaffoldState.isRevealed)
            )

            val scaffoldTutorial by viewModel.hasFinishedScaffoldTutorialFlow.collectAsState(true)
            if (!scaffoldTutorial) {
                LaunchedEffect(Unit) {
                    delay(800)
                    scaffoldState.reveal()
                    viewModel.finishedScaffoldTutorial()
                }
            }
        }

        val isUpdateNeeded = remember { UpdateChecker.isUpdateNeeded() }

        if (isUpdateNeeded && !viewModel.hasSeenUpdateDialogState.value) {
            val ctx = LocalContext.current

            UpdateDialog(
                onDismiss = {
                    viewModel.hasSeenUpdateDialogState.value = true
                },
                onConfirm = {
                    val versionName = UpdateChecker.getNewVersionUrl()
                    viewModel.hasSeenUpdateDialogState.value = true
                    ctx.browseUrl(versionName)
                }
            )
        }
    }

    @Composable
    private fun BackdropButton(
        icon: Painter,
        text: String,
        onClick: () -> Unit
    ) {
        val buttonModifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        val colors = ButtonDefaults.textButtonColors(
            MaterialTheme.colors.primary,
            MaterialTheme.colors.onPrimary
        )

        TextButton(
            modifier = buttonModifier,
            colors = colors,
            onClick = onClick
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(text = text)
            }
        }
    }

    @Composable
    private fun TopBar(
        viewModel: HomeViewModel,
        scaffoldState: BackdropScaffoldState
    ) {
        val scope = rememberCoroutineScope()

        TopAppBar(
            backgroundColor = MaterialTheme.colors.primary,
            navigationIcon = {
                val vector = AnimatedImageVector.animatedVectorResource(R.drawable.ic_menu_close_anim)

                IconButton(onClick = {
                    scope.launch {
                        if (scaffoldState.isConcealed)
                            scaffoldState.reveal()
                        else if (scaffoldState.isRevealed)
                            scaffoldState.conceal()
                    }
                }) {
                    val atEnd = if (scaffoldState.isAnimationRunning)
                        scaffoldState.isConcealed else scaffoldState.isRevealed

                    Icon(
                        rememberAnimatedVectorPainter(vector, atEnd),
                        contentDescription = null
                    )
                }
            },
            title = { Text(stringResource(R.string.activity_title)) },
            actions = {
                val darkThemeFlow = viewModel.darkThemeFlow.collectAsState(initial = true)
                IconButton(onClick = {
                    viewModel.switchTheme(!darkThemeFlow.value)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_switch_theme),
                        contentDescription = stringResource(id = R.string.action_switch_theme),
                    )
                }
                IconButton(onClick = {
                    viewModel.refresh()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh),
                        contentDescription = stringResource(id = R.string.action_refresh),
                    )
                }
            },
            elevation = 0.dp,
        )
    }

    private const val NEWS_URL = "https://brukenthal.ro/noutati/"
}

@Composable
fun UpdateDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = stringResource(id = R.string.update_available)) },
    text = { Text(text = stringResource(id = R.string.update_available_desc)) },
    buttons = {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = onDismiss,
            ) {
                Text(text = stringResource(id = R.string.action_ignore))
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
