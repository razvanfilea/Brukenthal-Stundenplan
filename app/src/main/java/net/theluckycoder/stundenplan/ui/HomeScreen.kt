package net.theluckycoder.stundenplan.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.theluckycoder.stundenplan.BuildConfig
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.utils.Analytics
import net.theluckycoder.stundenplan.viewmodel.HomeViewModel

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
        private const val APP_STORE_URL =
            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
    }
}

@Composable
private fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val timetableType = viewModel.timetableFlow.collectAsState()
    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(
                timetableType = timetableType.value,
                onClick = { newTimetableType ->
                    viewModel.switchTimetableType(newTimetableType)
                }
            )
        }
    ) {
        HomeContent(viewModel)
    }
}

@Preview
@Composable
private fun TopBar() {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.app_name))
        },
        actions = {
            IconButton(onClick = {
                /*TODO*/
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_switch_theme),
                    contentDescription = null
                )
            }
            IconButton(onClick = {
                /*TODO*/
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
    viewModel: HomeViewModel
) {

}

@Composable
private fun BottomBar(
    timetableType: TimetableType,
    onClick: (TimetableType) -> Unit
) {
    BottomAppBar {
        BottomNavigationItem(
            selected = timetableType == TimetableType.HIGH_SCHOOL,
            onClick = { onClick(TimetableType.HIGH_SCHOOL) },
            icon = {
                Text(text = stringResource(id = R.string.high_school))
            }
        )
        BottomNavigationItem(
            selected = timetableType == TimetableType.MIDDLE_SCHOOL,
            onClick = { onClick(TimetableType.MIDDLE_SCHOOL) },
            icon = {
                Text(text = stringResource(id = R.string.middle_school))
            }
        )
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    var timetableType by remember { mutableStateOf(TimetableType.MIDDLE_SCHOOL) }

    AppTheme(isDark = true) {
        BottomBar(timetableType = TimetableType.MIDDLE_SCHOOL) { newTimetableType: TimetableType ->
            timetableType = newTimetableType
        }
    }
}