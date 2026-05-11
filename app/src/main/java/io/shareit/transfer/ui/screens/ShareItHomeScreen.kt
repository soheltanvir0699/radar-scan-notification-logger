package io.shareit.transfer.ui.screens

import android.os.Environment
import android.os.StatFs
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.shareit.transfer.R

private val ShareBlue = Color(0xFF1976D2)
private val ShareBlueDark = Color(0xFF0D47A1)
private val ShareBlueBar = Color(0xFF1565C0)
private val SurfaceBg = Color(0xFFF5F7FA)
private val TextDark = Color(0xFF263238)
private val TextMuted = Color(0xFF607D8B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareItHomeScreen(
    showSecretButton: Boolean,
    onTitleDoubleTap: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onFiles: () -> Unit,
) {
    val context = LocalContext.current
    val storage = remember {
        runCatching {
            val statFs = StatFs(Environment.getDataDirectory().path)
            statFs.availableBytes to statFs.totalBytes
        }.getOrDefault(0L to 0L)
    }
    val available = storage.first
    val total = storage.second
    val used = (total - available).coerceAtLeast(0L)
    val pct = if (total > 0) used.toFloat() / total.toFloat() else 0f

    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SHAREit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = { onTitleDoubleTap() })
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShareBlueBar)
            )
        },
        floatingActionButton = {
            if (showSecretButton) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Double tap title to hide",
                        color = ShareBlueDark.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp, end = 6.dp)
                    )
                    ExtendedFloatingActionButton(
                        onClick = onOpenNotifications,
                        containerColor = ShareBlue,
                        contentColor = Color.White,
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Notifications", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Text(
                text = "Faster and simpler",
                fontSize = 14.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(34.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ImageActionTile("Send", R.drawable.shareit_send, onSend)
                ImageActionTile("Receive", R.drawable.shareit_receive, onReceive)
                ActionTile("Files", Icons.Default.Folder, onFiles)
            }

            Spacer(Modifier.weight(1f))

            StorageCard(
                available = available,
                total = total,
                pct = pct,
                onClean = {
                    Toast.makeText(context, "Cleaning…", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ActionTile(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(ShareBlue)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(label, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ImageActionTile(label: String, drawableRes: Int, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(ShareBlue)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(drawableRes),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .padding(2.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(label, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StorageCard(available: Long, total: Long, pct: Float, onClean: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(58.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { pct },
                    modifier = Modifier.size(58.dp),
                    color = ShareBlue,
                    trackColor = Color(0xFFE0E0E0),
                    strokeWidth = 5.dp
                )
                Text(
                    text = "${(pct * 100).toInt()}%",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = ShareBlueDark
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Available ${formatGB(available)}",
                    fontWeight = FontWeight.SemiBold,
                    color = ShareBlueDark
                )
                Text(
                    text = "Total ${formatGB(total)}",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            Button(
                onClick = onClean,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShareBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("CLEAN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

private fun formatGB(bytes: Long): String {
    val gb = bytes / 1_073_741_824.0
    return "%.2fGB".format(gb)
}
