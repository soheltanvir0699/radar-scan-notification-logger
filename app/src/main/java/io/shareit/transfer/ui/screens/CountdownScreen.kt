package io.shareit.transfer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.shareit.transfer.ui.animations.FloatingHearts
import io.shareit.transfer.ui.animations.TwinklingStars
import io.shareit.transfer.ui.theme.Champagne
import io.shareit.transfer.ui.theme.Cream
import io.shareit.transfer.ui.theme.Gold
import io.shareit.transfer.ui.theme.LovePink
import io.shareit.transfer.ui.theme.MidnightDeep
import io.shareit.transfer.ui.theme.MidnightPurple
import io.shareit.transfer.ui.theme.Plum
import io.shareit.transfer.ui.theme.RoseGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CountdownScreen(
    wifeName: String,
    husbandName: String,
    targetMillis: Long,
    nowMillis: Long,
    scheduledAtMillis: Long?,
    notificationsGranted: Boolean,
    canScheduleExact: Boolean,
    batteryUnrestricted: Boolean,
    fullScreenAllowed: Boolean,
    onRequestNotifications: () -> Unit,
    onRequestExactAlarms: () -> Unit,
    onRequestBatteryUnrestricted: () -> Unit,
    onRequestFullScreenIntent: () -> Unit,
    onSchedule: () -> Unit,
    onPreview: () -> Unit,
    onSecretLongPress: () -> Unit = {},
) {
    val fullyArmed = notificationsGranted && canScheduleExact && batteryUnrestricted && fullScreenAllowed
    val remaining = (targetMillis - nowMillis).coerceAtLeast(0L)
    val totalSeconds = remaining / 1000L
    val days = totalSeconds / 86400L
    val hours = (totalSeconds % 86400L) / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    val isBirthdayNow = remaining <= 0L

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
        TwinklingStars(modifier = Modifier.fillMaxSize())
        FloatingHearts(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeartHeader(onSecretLongPress = onSecretLongPress)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "For my dearest",
                style = MaterialTheme.typography.titleLarge,
                color = Champagne,
            )
            Text(
                text = wifeName,
                style = MaterialTheme.typography.displayMedium,
                color = LovePink,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "with all my love — $husbandName",
                style = MaterialTheme.typography.bodyMedium,
                color = RoseGold,
            )

            Spacer(Modifier.height(28.dp))

            AnimatedVisibility(
                visible = !isBirthdayNow,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CountdownGrid(days, hours, minutes, seconds)
            }
            AnimatedVisibility(
                visible = isBirthdayNow,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BirthdayNowBanner()
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Surprise unlocks at",
                style = MaterialTheme.typography.bodyLarge,
                color = Cream.copy(alpha = 0.85f),
            )
            Text(
                text = formatTarget(targetMillis),
                style = MaterialTheme.typography.titleLarge,
                color = Gold,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.weight(1f))

            PermissionsRow(
                notificationsGranted = notificationsGranted,
                canScheduleExact = canScheduleExact,
                batteryUnrestricted = batteryUnrestricted,
                fullScreenAllowed = fullScreenAllowed,
                onRequestNotifications = onRequestNotifications,
                onRequestExactAlarms = onRequestExactAlarms,
                onRequestBatteryUnrestricted = onRequestBatteryUnrestricted,
                onRequestFullScreenIntent = onRequestFullScreenIntent,
            )

            Spacer(Modifier.height(14.dp))

            ArmedStatus(fullyArmed = fullyArmed, scheduledAtMillis = scheduledAtMillis)

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onSchedule,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LovePink,
                    contentColor = Cream
                )
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Schedule the Surprise",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onPreview,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Champagne)
                Spacer(Modifier.width(10.dp))
                Text("Preview the Surprise", color = Champagne, fontSize = 15.sp)
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun HeartHeader(onSecretLongPress: () -> Unit = {}) {
    val transition = rememberInfiniteTransition(label = "header")
    val scale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "header-scale"
    )
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .size(72.dp)
            .scale(scale)
            .background(
                Brush.radialGradient(
                    colors = listOf(LovePink, LovePink.copy(alpha = 0f)),
                    radius = 110f
                ),
                shape = CircleShape
            )
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = {},
                onLongClick = onSecretLongPress,
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Cream,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun CountdownGrid(days: Long, hours: Long, minutes: Long, seconds: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TimeChip("Days", days, Modifier.weight(1f))
        TimeChip("Hours", hours, Modifier.weight(1f))
        TimeChip("Minutes", minutes, Modifier.weight(1f))
        TimeChip("Seconds", seconds, Modifier.weight(1f), highlight = true)
    }
}

@Composable
private fun TimeChip(
    label: String,
    value: Long,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    val container = if (highlight) LovePink.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%02d".format(value),
                color = if (highlight) LovePink else Cream,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = Cream.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BirthdayNowBanner() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LovePink),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "It's TIME!",
                color = Cream,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tap Preview to celebrate now",
                color = Cream.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun PermissionsRow(
    notificationsGranted: Boolean,
    canScheduleExact: Boolean,
    batteryUnrestricted: Boolean,
    fullScreenAllowed: Boolean,
    onRequestNotifications: () -> Unit,
    onRequestExactAlarms: () -> Unit,
    onRequestBatteryUnrestricted: () -> Unit,
    onRequestFullScreenIntent: () -> Unit,
) {
    val items = buildList {
        if (!notificationsGranted) add(PermissionItem("Allow notifications", Icons.Default.NotificationsActive, onRequestNotifications))
        if (!canScheduleExact) add(PermissionItem("Allow exact alarms", Icons.Default.Lock, onRequestExactAlarms))
        if (!batteryUnrestricted) add(PermissionItem("Allow battery use", Icons.Default.BatteryFull, onRequestBatteryUnrestricted))
        if (!fullScreenAllowed) add(PermissionItem("Allow full-screen", Icons.Default.Fullscreen, onRequestFullScreenIntent))
    }
    if (items.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { item ->
                    PermissionPill(
                        text = item.label,
                        onClick = item.onClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(item.icon, contentDescription = null, tint = Gold)
                    }
                }
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

private data class PermissionItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit,
)

@Composable
private fun PermissionPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        icon()
        Spacer(Modifier.width(6.dp))
        Text(text, color = Cream, fontSize = 12.sp)
    }
}

@Composable
private fun ArmedStatus(fullyArmed: Boolean, scheduledAtMillis: Long?) {
    val color = if (fullyArmed) Gold else LovePink
    val icon = if (fullyArmed) Icons.Default.CheckCircle else Icons.Default.Bolt
    val label = when {
        scheduledAtMillis == null -> "Tap Schedule to arm the surprise"
        fullyArmed -> "Surprise FULLY ARMED for\n${formatTarget(scheduledAtMillis)}"
        else -> "Almost ready — grant the missing permissions above\nfor a guaranteed midnight launch"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            color = color,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatTarget(millis: Long): String {
    val sdf = SimpleDateFormat("EEE, d MMM yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
