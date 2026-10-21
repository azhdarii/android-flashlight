package com.azhdar.app10

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.azhdar.app10.ui.navigation.AppNavHost
import com.azhdar.app10.ui.FlashlightViewModel
import com.azhdar.app10.ui.theme.App10Theme

/**
 * Single activity host.
 *
 * The original class was ~980 lines and contained the navigation graph, every screen, the
 * ad integration and the window manipulation. It is now reduced to hosting the Compose
 * content; all behaviour lives in the dedicated UI classes.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Applies the launch theme and, on Android 12+, hands over to the platform splash
        // screen. The splash is dismissed as soon as the first frame is drawn.
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = (application as App10Application).container

        setContent {
            val viewModel: FlashlightViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        FlashlightViewModel(
                            application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                                as App10Application,
                            container = container,
                        )
                    }
                },
            )
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()

            // The screen must not time out while the user is using the torch.
            DisposableEffect(keepScreenOn) {
                if (keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            }

            App10Theme(themeMode = themeMode) {
                AppNavHost(
                    viewModel = viewModel,
                    adsManager = container.adsManager,
                )
            }
        }

        container.adsManager.initialize()
    }
}
