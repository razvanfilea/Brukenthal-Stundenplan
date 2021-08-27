package net.theluckycoder.stundenplan.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.viewmodel.HomeViewModel

class HomeActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = ComposeView(this)
        view.setContent {
            val isDark by viewModel.darkThemeFlow.collectAsState(initial = true)
            AppTheme(isDark = isDark) {
                HomeScreen(viewModel = viewModel)
            }
        }
        setContentView(view)
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
        HomeContent()
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
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_switch_theme),
                    contentDescription = null
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = null,
                )
            }

        }
    )
}

@Preview
@Composable
private fun HomeContent() {

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
            onClick = { onClick(TimetableType.MIDDLE_SCHOOL ) },
            icon = {
                Text(text = stringResource(id = R.string.middle_school))
            }
        )
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    AppTheme(isDark = true) {
        BottomBar(timetableType = TimetableType.MIDDLE_SCHOOL) { newTimetableType: TimetableType ->

        }
    }
}