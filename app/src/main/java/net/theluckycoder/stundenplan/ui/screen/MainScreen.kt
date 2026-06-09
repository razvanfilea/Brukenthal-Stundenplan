package net.theluckycoder.stundenplan.ui.screen

import android.content.ActivityNotFoundException
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.extensions.browseUrl
import net.theluckycoder.stundenplan.ui.LocalSnackbarHostState
import net.theluckycoder.stundenplan.viewmodel.HomeViewModel

object MainScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = viewModel<HomeViewModel>()
        val snackbarHostState = remember { SnackbarHostState() }
        val rootNavigator = LocalNavigator.currentOrThrow

        Navigator(TimetableScreen()) { navigator ->
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { TopBar(viewModel, rootNavigator, navigator) },
                bottomBar = { BottomBar(navigator) },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                    Box(Modifier.padding(padding)) {
                        CurrentScreen()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TopBar(
        viewModel: HomeViewModel,
        rootNavigator: Navigator,
        contentNavigator: Navigator
    ) {
        val containerColor = MaterialTheme.colorScheme.primary
        val timetableType by viewModel.timetableStateFlow.collectAsState()

        Column {
            TopAppBar(
                title = { Text(stringResource(R.string.activity_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    var showMenu by remember { mutableStateOf(false) }

                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = stringResource(id = R.string.action_refresh),
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_more_vertical),
                                contentDescription = stringResource(id = R.string.menu_more)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            val darkThemeFlow = viewModel.darkThemeFlow.collectAsState(initial = true)
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.action_switch_theme)) },
                                onClick = {
                                    viewModel.switchTheme(!darkThemeFlow.value)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_switch_theme),
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.menu_about)) },
                                onClick = {
                                    rootNavigator.push(AboutScreen())
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_about),
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            )

            if (contentNavigator.lastItem is TimetableScreen) {
                val selectedIndex = if (timetableType == net.theluckycoder.stundenplan.model.TimetableType.HIGH_SCHOOL) 0 else 1

                Surface(tonalElevation = 3.dp) {
                    PrimaryTabRow(
                        selectedTabIndex = selectedIndex,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.Transparent, // Use Surface's color
                    ) {
                        Tab(
                            selected = selectedIndex == 0,
                            onClick = { viewModel.switchTimetableType(net.theluckycoder.stundenplan.model.TimetableType.HIGH_SCHOOL) },
                            text = {
                                Text(
                                    text = stringResource(id = R.string.high_school),
                                    fontWeight = FontWeight.Bold.takeIf { selectedIndex == 0 },
                                )
                            }
                        )
                        Tab(
                            selected = selectedIndex == 1,
                            onClick = { viewModel.switchTimetableType(net.theluckycoder.stundenplan.model.TimetableType.MIDDLE_SCHOOL) },
                            text = {
                                Text(
                                    text = stringResource(id = R.string.middle_school),
                                    fontWeight = FontWeight.Bold.takeIf { selectedIndex == 1 },
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomBar(navigator: Navigator) {
        val context = LocalContext.current
        NavigationBar {
            val currentScreen = navigator.lastItem

            NavigationBarItem(
                selected = currentScreen is TimetableScreen,
                onClick = {
                    if (currentScreen !is TimetableScreen) {
                        navigator.replaceAll(TimetableScreen())
                    }
                },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_timetable),
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(R.string.menu_timetable)) }
            )

            NavigationBarItem(
                selected = false,
                onClick = {
                    try {
                        CustomTabsIntent.Builder()
                            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                            .build()
                            .launchUrl(context, NEWS_URL.toUri())
                    } catch (e: ActivityNotFoundException) {
                        context.browseUrl(NEWS_URL)
                    }
                },
                icon = { Icon(painterResource(R.drawable.ic_news), contentDescription = null) },
                label = { Text(stringResource(R.string.menu_news)) }
            )

            NavigationBarItem(
                selected = currentScreen is GradesScreen,
                onClick = {
                    if (currentScreen !is GradesScreen) {
                        navigator.replaceAll(GradesScreen())
                    }
                },
                icon = { Icon(painterResource(R.drawable.ic_grades), contentDescription = null) },
                label = { Text(stringResource(R.string.menu_grades)) }
            )
        }
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
    confirmButton = {
        Button(onClick = onConfirm) {
            Text(text = stringResource(id = R.string.action_update))
        }
    },
    dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(text = stringResource(id = R.string.action_ignore))
        }
    }
)
