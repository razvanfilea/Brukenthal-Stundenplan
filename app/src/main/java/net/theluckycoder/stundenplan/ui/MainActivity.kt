package net.theluckycoder.stundenplan.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.navigator.Navigator
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.theluckycoder.stundenplan.BuildConfig
import net.theluckycoder.stundenplan.ui.screen.MainScreen
import net.theluckycoder.stundenplan.utils.Analytics
import net.theluckycoder.stundenplan.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        val view = ComposeView(this)
        view.setContent {
            val isDark by homeViewModel.darkThemeFlow.collectAsState(initial = true)

            AppTheme(isDark = isDark) {
                val systemUiController = rememberSystemUiController()

                val primaryVariantColor = MaterialTheme.colors.primaryVariant
                SideEffect {
                    systemUiController.setSystemBarsColor(primaryVariantColor, darkIcons = isDark)
                }

                Navigator(MainScreen)
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
