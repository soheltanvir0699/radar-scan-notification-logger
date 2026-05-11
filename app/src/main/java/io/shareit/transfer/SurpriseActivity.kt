package io.shareit.transfer

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import io.shareit.transfer.audio.HappyBirthdayPlayer
import io.shareit.transfer.ui.screens.SurpriseScreen
import io.shareit.transfer.ui.theme.SurpriseTheme
import io.shareit.transfer.util.BirthdayConfig

class SurpriseActivity : ComponentActivity() {

    private val player = HappyBirthdayPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wakeAndUnlock()
        enableEdgeToEdge()
        setContent {
            SurpriseTheme {
                Box(Modifier.fillMaxSize()) {
                    SurpriseScreen(
                        wifeName = BirthdayConfig.WIFE_NAME,
                        wifeShort = BirthdayConfig.WIFE_SHORT,
                        husbandName = BirthdayConfig.HUSBAND_NAME,
                        message = LOVE_MESSAGE,
                        onClose = { finish() }
                    )
                }
                LaunchedEffect(Unit) {
                    player.start(loop = true)
                    vibrateCelebration()
                }
                DisposableEffect(Unit) {
                    onDispose { player.stop() }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }

    private fun wakeAndUnlock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun vibrateCelebration() {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 220, 120, 220, 120, 380)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 220, 120, 220, 120, 380), -1)
            }
        } catch (_: Exception) {
        }
    }

    companion object {
        private val LOVE_MESSAGE = """
            My dearest Supti,

            From the very first second of this day,
            my heart wants to be the first to whisper —
            Happy Birthday, my love.

            You are my sunrise, my song, my forever.
            Every breath I take is sweeter because of you.

            Thank you for being mine.

            — Always yours,
                Sohel
        """.trimIndent()
    }
}
