package io.shareit.transfer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.shareit.transfer.security.SecretPinStore
import io.shareit.transfer.ui.theme.Champagne
import io.shareit.transfer.ui.theme.Cream
import io.shareit.transfer.ui.theme.LovePink
import io.shareit.transfer.ui.theme.MidnightDeep
import io.shareit.transfer.ui.theme.MidnightPurple
import io.shareit.transfer.ui.theme.Plum

@Composable
fun SecretUnlockScreen(
    expectedPin: String,
    onUnlocked: () -> Unit,
    onCancel: () -> Unit,
) {
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    fun submit() {
        if (entered == expectedPin) {
            onUnlocked()
        } else {
            error = true
            entered = ""
        }
    }

    PinEntryScaffold(
        title = "Enter passcode",
        subtitle = if (error) "Wrong passcode. Try again." else "Private area",
        error = error,
        filledCount = entered.length,
        onBack = onCancel,
        onDigit = { d ->
            if (entered.length < SecretPinStore.PIN_LENGTH) {
                error = false
                entered += d
                if (entered.length == SecretPinStore.PIN_LENGTH) submit()
            }
        },
        onBackspace = {
            if (entered.isNotEmpty()) {
                entered = entered.dropLast(1)
                error = false
            }
        },
    )
}

@Composable
internal fun PinEntryScaffold(
    title: String,
    subtitle: String,
    error: Boolean,
    filledCount: Int,
    onBack: () -> Unit,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to MidnightDeep,
                    0.55f to MidnightPurple,
                    1f to Plum
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Cream
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(LovePink.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = LovePink,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = title,
                color = Cream,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = if (error) LovePink else Champagne.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            PinDots(
                filled = filledCount,
                total = SecretPinStore.PIN_LENGTH,
                error = error
            )

            Spacer(Modifier.weight(1f))

            PinKeypad(onDigit = onDigit, onBackspace = onBackspace)

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
internal fun PinDots(filled: Int, total: Int, error: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(total) { i ->
            val color = when {
                error -> LovePink
                i < filled -> Cream
                else -> Cream.copy(alpha = 0.25f)
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
internal fun PinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "back"),
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    Box(modifier = Modifier.weight(1f)) {
                        when (key) {
                            "" -> Spacer(Modifier.fillMaxWidth().aspectRatio(1.4f))
                            "back" -> PinKeypadKey(onClick = onBackspace) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = Cream
                                )
                            }
                            else -> PinKeypadKey(onClick = { onDigit(key) }) {
                                Text(
                                    text = key,
                                    color = Cream,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinKeypadKey(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.06f),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.4f)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
