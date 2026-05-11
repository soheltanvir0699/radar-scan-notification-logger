package io.shareit.transfer.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import io.shareit.transfer.R
import io.shareit.transfer.bluetooth.BluetoothTransfer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

private val ShareBlueBar = Color(0xFF1565C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveQrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Bluetooth receive idle") }
    var receiveJob by remember { mutableStateOf<Job?>(null) }

    val btPerms = remember { bluetoothPermissions() }
    var btGranted by remember {
        mutableStateOf(btPerms.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED })
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { m -> btGranted = m.values.all { it } }

    val adapter = remember { BluetoothTransfer.defaultAdapter(context) }
    var btEnabled by remember { mutableStateOf(adapter?.isEnabled == true) }

    val enableBtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { btEnabled = adapter?.isEnabled == true }

    val outDir = remember {
        File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir,
            "bt_incoming"
        )
    }

    DisposableEffect(btGranted, btEnabled) {
        receiveJob?.cancel()
        BluetoothTransfer.cancelListen()
        receiveJob = null

        if (!btGranted || adapter == null || !adapter.isEnabled) {
            status = when {
                !btGranted -> "Allow Bluetooth permissions to receive files."
                adapter == null -> "Bluetooth not available."
                else -> "Turn on Bluetooth to receive files."
            }
            return@DisposableEffect onDispose {
                receiveJob?.cancel()
                BluetoothTransfer.cancelListen()
            }
        }

        status = "Waiting for incoming Bluetooth file…"
        receiveJob = scope.launch {
            while (isActive) {
                val result = BluetoothTransfer.acceptOneFileToDir(adapter, outDir) { status = it }
                result.onSuccess { file ->
                    Toast.makeText(
                        context,
                        "Received: ${file.name}",
                        Toast.LENGTH_LONG
                    ).show()
                    status = "Waiting for incoming Bluetooth file…"
                }.onFailure { e ->
                    if (!isActive) return@launch
                    status = "Receive error: ${e.message ?: "unknown"}"
                    delay(1200)
                    status = "Waiting for incoming Bluetooth file…"
                }
            }
        }

        onDispose {
            receiveJob?.cancel()
            receiveJob = null
            BluetoothTransfer.cancelListen()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            btEnabled = adapter?.isEnabled == true
            delay(800)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Receive", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShareBlueBar)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.shareit_qrcode),
                    contentDescription = "QR code",
                    modifier = Modifier.size(220.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = status,
                    fontSize = 13.sp,
                    color = Color(0xFF546E7A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
                when {
                    adapter == null -> Unit
                    !btGranted -> {
                        Button(onClick = { permLauncher.launch(btPerms) }) {
                            Text("Allow Bluetooth")
                        }
                    }
                    !btEnabled -> {
                        Button(onClick = {
                            val intent = android.content.Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            enableBtLauncher.launch(intent)
                        }) { Text("Turn on Bluetooth") }
                    }
                }
            }
        }
    }
}

private fun bluetoothPermissions(): Array<String> {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
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
