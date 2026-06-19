package io.shareit.transfer.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.shareit.transfer.location.LocationAccess
import io.shareit.transfer.location.LocationPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private val ShareBlueBar = Color(0xFF1565C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapScreen(
    points: List<LocationPoint>,
    locationGranted: Boolean,
    backgroundGranted: Boolean,
    isRefreshing: Boolean,
    onBack: () -> Unit,
    onRefreshNow: () -> Unit,
    onClearHistory: () -> Unit,
    onRequestLocation: () -> Unit,
    onRequestBackground: () -> Unit,
) {
    val latest = points.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshNow, enabled = !isRefreshing && locationGranted) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh now")
                    }
                    IconButton(onClick = onClearHistory, enabled = points.isNotEmpty()) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear history")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShareBlueBar,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!locationGranted) {
                item {
                    PermissionCard(
                        title = "Location permission needed",
                        message = "Allow location access so the app can save your position every 15 minutes.",
                        actionLabel = "Grant location",
                        onAction = onRequestLocation,
                    )
                }
            } else if (!backgroundGranted) {
                item {
                    PermissionCard(
                        title = "Background location recommended",
                        message = "Choose \"Allow all the time\" so saves continue when the app is closed.",
                        actionLabel = "Allow all the time",
                        onAction = onRequestBackground,
                    )
                }
            }

            item {
                CurrentLocationCard(
                    point = latest,
                    isRefreshing = isRefreshing,
                    locationGranted = locationGranted,
                )
            }

            item {
                LocationHistoryMap(
                    points = points,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                )
            }

            if (points.isEmpty()) {
                item {
                    Text(
                        text = "No saved locations yet. The app records every 15 minutes once permission is granted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                item {
                    Text(
                        text = "History (${points.size})",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                items(points, key = { "${it.capturedAt}_${it.latitude}_${it.longitude}" }) { point ->
                    LocationHistoryRow(point = point, isLatest = point == latest)
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun CurrentLocationCard(
    point: LocationPoint?,
    isRefreshing: Boolean,
    locationGranted: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current location",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            when {
                isRefreshing -> {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Getting location…", style = MaterialTheme.typography.bodyMedium)
                }

                point != null -> {
                    Text(point.displayAddress(), fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = point.displayCoordinates(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Saved ${point.displayTime()} · ±${point.accuracyMeters.toInt()} m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                locationGranted -> {
                    Text(
                        text = "Waiting for first save. Tap refresh or wait for the next 15-minute update.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                else -> {
                    Text(
                        text = "Grant location permission to start tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationHistoryRow(point: LocationPoint, isLatest: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLatest) Color(0xFFBBDEFB) else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (isLatest) "Latest · ${point.displayTime()}" else point.displayTime(),
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(point.displayAddress(), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = point.displayCoordinates(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LocationHistoryMap(
    points: List<LocationPoint>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDetach()
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).also { map ->
                    mapView = map
                    map.setTileSource(TileSourceFactory.MAPNIK)
                    map.setMultiTouchControls(true)
                    map.controller.setZoom(14.0)
                }
            },
            update = { map ->
                map.overlays.clear()
                if (points.isEmpty()) {
                    map.controller.setCenter(GeoPoint(0.0, 0.0))
                    map.invalidate()
                    return@AndroidView
                }

                val geoPoints = points.asReversed().map { GeoPoint(it.latitude, it.longitude) }

                if (geoPoints.size >= 2) {
                    val line = Polyline().apply {
                        setPoints(geoPoints)
                        outlinePaint.color = AndroidColor.parseColor("#1565C0")
                        outlinePaint.strokeWidth = 8f
                    }
                    map.overlays.add(line)
                }

                points.forEachIndexed { index, point ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(point.latitude, point.longitude)
                        title = if (index == 0) "Latest · ${point.displayTime()}" else point.displayTime()
                        snippet = point.displayAddress()
                    }
                    map.overlays.add(marker)
                }

                val latest = geoPoints.last()
                map.controller.setCenter(latest)
                val zoom = when {
                    points.size == 1 -> 16.0
                    points.size < 5 -> 14.0
                    else -> 12.0
                }
                map.controller.setZoom(zoom)
                map.invalidate()
            },
        )
    }
}
