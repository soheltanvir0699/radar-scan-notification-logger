package io.shareit.transfer.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import io.shareit.transfer.bluetooth.BluetoothTransfer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private val ShareBlueBar = Color(0xFF1565C0)
private val Bg = Color(0xFF0B1C34)
private val TextDark = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFFB0C7E8)

private fun bluetoothPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
    }
}

private fun bluetoothAdapter(context: Context): BluetoothAdapter? = BluetoothTransfer.defaultAdapter(context)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSearchScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie_radar.json"))

    val transition = rememberInfiniteTransition(label = "dots")
    val dotPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dot-phase"
    )
    val dots = ".".repeat(dotPhase.toInt().coerceIn(0, 3))

    val btPerms = remember { bluetoothPermissions() }
    var btGranted by remember {
        mutableStateOf(btPerms.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED })
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { m -> btGranted = m.values.all { it } }

    val adapter = remember { bluetoothAdapter(context) }
    var btEnabled by remember { mutableStateOf(adapter?.isEnabled == true) }
    val enableBtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { btEnabled = adapter?.isEnabled == true }

    val discovered = remember { mutableStateListOf<BluetoothDevice>() }
    var scanning by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<BluetoothDevice?>(null) }

    val pickFile = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        val dev = selected ?: return@rememberLauncherForActivityResult
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val name = queryDisplayName(context, uri) ?: "shared_file"
            val bytes = readUriBytesCapped(context, uri, maxBytes = 16L * 1024 * 1024)
            if (bytes == null) {
                Toast.makeText(context, "Could not read file (too large or denied).", Toast.LENGTH_LONG).show()
                return@launch
            }
            Toast.makeText(context, "Sending to ${dev.address}…", Toast.LENGTH_SHORT).show()
            @Suppress("MissingPermission")
            val result = BluetoothTransfer.sendFileBytes(dev, name, bytes)
            result.onSuccess {
                Toast.makeText(context, "Sent.", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                Toast.makeText(context, "Send failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    DisposableEffect(btGranted, btEnabled) {
        val ad = adapter
        if (!btGranted || ad == null || !ad.isEnabled) {
            return@DisposableEffect onDispose { }
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val dev: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        if (dev != null && discovered.none { it.address == dev.address }) {
                            discovered.add(dev)
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        scanning = false
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        discovered.clear()
        ad.bondedDevices?.forEach { bonded ->
            if (discovered.none { it.address == bonded.address }) discovered.add(bonded)
        }
        if (ad.isDiscovering) {
            ad.cancelDiscovery()
        }
        scanning = true
        @Suppress("MissingPermission")
        ad.startDiscovery()

        onDispose {
            runCatching {
                if (ad.isDiscovering) ad.cancelDiscovery()
            }
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    LaunchedEffect(adapter) {
        while (true) {
            btEnabled = adapter?.isEnabled == true
            kotlinx.coroutines.delay(800)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        "Search devices",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShareBlueBar)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = "Scanning Bluetooth$dots",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Other phone must open Receive and stay on this screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(12.dp))

                when {
                    adapter == null -> {
                        Text("Bluetooth not available on this device.", color = TextMuted)
                    }
                    !btGranted -> {
                        Button(onClick = { permLauncher.launch(btPerms) }) {
                            Text("Allow Bluetooth & location (scan)")
                        }
                    }
                    !btEnabled -> {
                        Button(onClick = {
                            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            enableBtLauncher.launch(intent)
                        }) { Text("Turn on Bluetooth") }
                    }
                    else -> {
                        Text(
                            if (scanning) "Discovery running…" else "Discovery finished",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(discovered, key = { it.address }) { dev ->
                        val name = try {
                            @Suppress("MissingPermission")
                            dev.name ?: dev.address
                        } catch (_: SecurityException) {
                            dev.address
                        }
                        val bonded = dev.bondState == BluetoothDevice.BOND_BONDED
                        DeviceRow(
                            title = name,
                            subtitle = dev.address,
                            bonded = bonded,
                            selected = selected?.address == dev.address,
                            onClick = { selected = dev },
                            onSend = {
                                selected = dev
                                pickFile.launch("*/*")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(
    title: String,
    subtitle: String,
    bonded: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onSend: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF1E3A5F) else Color(0xFF132743)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Medium)
                Text(subtitle, color = TextMuted, fontSize = 12.sp)
                if (bonded) {
                    Spacer(Modifier.height(4.dp))
                    Text("Paired", color = Color(0xFF8BC34A), fontSize = 11.sp)
                }
            }
            Button(onClick = onSend) { Text("Send file") }
        }
    }
}

private fun queryDisplayName(context: Context, uri: android.net.Uri): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { c ->
        if (c.moveToFirst()) {
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) return c.getString(idx)
        }
    }
    return null
}

private suspend fun readUriBytesCapped(context: Context, uri: android.net.Uri, maxBytes: Long): ByteArray? =
    withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val out = ByteArrayOutputStream()
                val buf = ByteArray(8192)
                var total = 0L
                while (true) {
                    val r = input.read(buf)
                    if (r <= 0) break
                    total += r
                    if (total > maxBytes) return@runCatching null
                    out.write(buf, 0, r)
                }
                out.toByteArray()
            }
        }.getOrNull()
    }
