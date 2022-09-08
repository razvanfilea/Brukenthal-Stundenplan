package net.theluckycoder.stundenplan.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.navigator.Navigator
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.ui.screen.MainScreen
import net.theluckycoder.stundenplan.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // TODO Inform user of missing notification permission
        }
    }

    private val homeViewModel: HomeViewModel by viewModels()
    private var showNotificationPermissionDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        val view = ComposeView(this)
        view.setContent {
            val isDark by homeViewModel.darkThemeFlow.collectAsState(true)

            AppTheme(isDark = isDark) {
                val systemUiController = rememberSystemUiController()

                val primaryVariantColor = MaterialTheme.colors.primaryVariant
                LaunchedEffect(primaryVariantColor, isDark) {
                    systemUiController.setStatusBarColor(primaryVariantColor, darkIcons = isDark)
                    systemUiController.setNavigationBarColor(Color.Black, isDark)
                }

                Navigator(MainScreen)

                if (showNotificationPermissionDialog && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text(stringResource(R.string.notification_permission_required)) },
                        text = { Text(stringResource(R.string.notification_permission_desc)) },
                        dismissButton = {
                            TextButton(
                                onClick = { showNotificationPermissionDialog = false },
                            ) {
                                Text(text = stringResource(id = R.string.action_ignore))
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    showNotificationPermissionDialog = false
                                },
                            ) {
                                Text(text = stringResource(id = android.R.string.ok))
                            }
                        }
                    )
                }
            }
        }

        setContentView(view)

        askNotificationPermission()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showNotificationPermissionDialog = true
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
