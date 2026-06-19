package io.shareit.transfer.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.shareit.transfer.location.LocationPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private val ShareBlueBar = Color(0xFF1565C0)
private const val PAGE_SIZE = 15
private const val TWENTY_FOUR_HOURS_MS = 24L * 60L * 60L * 1000L

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
    val context = LocalContext.current
    val latest = points.firstOrNull()
    val recentPoints = remember(points) {
        val now = System.currentTimeMillis()
        points.filter { now - it.capturedAt <= TWENTY_FOUR_HOURS_MS }
    }
    val olderPoints = remember(points) {
        val now = System.currentTimeMillis()
        points.filter { now - it.capturedAt > TWENTY_FOUR_HOURS_MS }
    }

    var recentPage by remember { mutableIntStateOf(0) }
    var olderPage by remember { mutableIntStateOf(0) }
    var selectedOlderPoint by remember { mutableStateOf<LocationPoint?>(null) }

    LaunchedEffect(recentPoints.size) {
        recentPage = recentPage.coerceIn(0, lastPageIndex(recentPoints.size))
    }
    LaunchedEffect(olderPoints.size, olderPage) {
        olderPage = olderPage.coerceIn(0, lastPageIndex(olderPoints.size))
        selectedOlderPoint = null
    }

    val recentPageItems = remember(recentPoints, recentPage) {
        paginate(recentPoints, recentPage, PAGE_SIZE)
    }
    val olderPageItems = remember(olderPoints, olderPage) {
        paginate(olderPoints, olderPage, PAGE_SIZE)
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
            if (!locationGranted) {
                item {
                    PermissionCard(
                        title = "Location permission needed",
                        message = "Allow location access so the app can save your position every 30 minutes.",
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
                    onCopy = { point -> copyLocation(context, point) },
                )
            }

            item {
                SectionHeader(
                    title = "Last 24 hours",
                    subtitle = "${recentPoints.size} saved · shown on map automatically",
                )
            }

            item {
                if (recentPoints.isEmpty()) {
                    EmptyMapPlaceholder(
                        message = "No locations in the last 24 hours.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                    )
                } else {
                    LocationHistoryMap(
                        points = recentPoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                    )
                }
            }

            if (recentPoints.isEmpty()) {
                item {
                    Text(
                        text = "Recent locations will appear on the map above. The app records every 30 minutes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(recentPageItems, key = { it.listKey() }) { point ->
                    LocationHistoryRow(
                        point = point,
                        isLatest = point == latest,
                        onCopy = { copyLocation(context, point) },
                    )
                }
            }

            if (olderPoints.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Older",
                        subtitle = "${olderPoints.size} saved · map loads only when you tap Load map",
                    )
                }

                if (selectedOlderPoint != null) {
                    item(key = "older_map_${selectedOlderPoint!!.listKey()}") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = selectedOlderPoint!!.displayTime(),
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                TextButton(onClick = { selectedOlderPoint = null }) {
                                    Text("Hide map")
                                }
                            }
                            LocationHistoryMap(
                                points = listOf(selectedOlderPoint!!),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                            )
                        }
                    }
                }

                items(olderPageItems, key = { it.listKey() }) { point ->
                    LocationHistoryRow(
                        point = point,
                        isLatest = false,
                        onCopy = { copyLocation(context, point) },
                        loadMapExpanded = selectedOlderPoint?.listKey() == point.listKey(),
                        onToggleMap = {
                            selectedOlderPoint = if (selectedOlderPoint?.listKey() == point.listKey()) {
                                null
                            } else {
                                point
                            }
                        },
                    )
                }
            }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                PaginationBar(
                    label = "Recent",
                    page = recentPage,
                    totalItems = recentPoints.size,
                    pageSize = PAGE_SIZE,
                    onPrevious = { recentPage -= 1 },
                    onNext = { recentPage += 1 },
                )
                if (olderPoints.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    PaginationBar(
                        label = "Older",
                        page = olderPage,
                        totalItems = olderPoints.size,
                        pageSize = PAGE_SIZE,
                        onPrevious = { olderPage -= 1 },
                        onNext = { olderPage += 1 },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PaginationBar(
    label: String? = null,
    page: Int,
    totalItems: Int,
    pageSize: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    if (totalItems <= pageSize) return

    val totalPages = pageCount(totalItems, pageSize)
    val start = page * pageSize + 1
    val end = minOf((page + 1) * pageSize, totalItems)

    Column(modifier = Modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
        OutlinedButton(onClick = onPrevious, enabled = page > 0) {
            Text("Previous")
        }
        Text(
            text = "$start–$end of $totalItems · page ${page + 1}/$totalPages",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedButton(onClick = onNext, enabled = page < totalPages - 1) {
            Text("Next")
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
    onCopy: (LocationPoint) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Current location",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (point != null) {
                    IconButton(onClick = { onCopy(point) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy location")
                    }
                }
            }
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
                        text = "Waiting for first save. Tap refresh or wait for the next 30-minute update.",
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
private fun LocationHistoryRow(
    point: LocationPoint,
    isLatest: Boolean,
    onCopy: () -> Unit,
    loadMapExpanded: Boolean = false,
    onToggleMap: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLatest) Color(0xFFBBDEFB) else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy location")
                }
            }
            if (onToggleMap != null) {
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onToggleMap) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (loadMapExpanded) "Hide map" else "Load map")
                }
            }
        }
    }
}

@Composable
private fun EmptyMapPlaceholder(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
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
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
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

private fun copyLocation(context: Context, point: LocationPoint) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("location", point.copyText()))
    Toast.makeText(context, "Location copied", Toast.LENGTH_SHORT).show()
}

private fun paginate(items: List<LocationPoint>, page: Int, pageSize: Int): List<LocationPoint> {
    if (items.isEmpty()) return emptyList()
    val safePage = page.coerceIn(0, lastPageIndex(items.size, pageSize))
    val start = safePage * pageSize
    val end = minOf(start + pageSize, items.size)
    return items.subList(start, end)
}

private fun pageCount(totalItems: Int, pageSize: Int): Int =
    maxOf(1, (totalItems + pageSize - 1) / pageSize)

private fun lastPageIndex(totalItems: Int, pageSize: Int = PAGE_SIZE): Int =
    maxOf(0, pageCount(totalItems, pageSize) - 1)
