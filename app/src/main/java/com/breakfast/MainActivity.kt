package com.breakfast

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.breakfast.ui.navigation.BreakfastNavGraph
import com.breakfast.ui.tabbar.notifications.NotificationHandler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var notificationHandler: NotificationHandler

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            lifecycleScope.launch {
                notificationHandler.fetchAndStoreFcmToken()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationHandler = NotificationHandler(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                lifecycleScope.launch {
                    notificationHandler.fetchAndStoreFcmToken()
                }
            }
        } else {
            lifecycleScope.launch {
                notificationHandler.fetchAndStoreFcmToken()
            }
        }

        setContent {
            com.breakfast.ui.theme.BreakfastTheme {
                Surface {
                    BreakfastNavGraph()
                }
            }
        }
    }
}