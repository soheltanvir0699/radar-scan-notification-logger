package io.shareit.transfer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.shareit.transfer.notifications.CapturedNotification
import io.shareit.transfer.ui.theme.Champagne
import io.shareit.transfer.ui.theme.Cream
import io.shareit.transfer.ui.theme.Gold
import io.shareit.transfer.ui.theme.LovePink
import io.shareit.transfer.ui.theme.MidnightDeep
import io.shareit.transfer.ui.theme.MidnightPurple
import io.shareit.transfer.ui.theme.Plum
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val NOTIF_PAGE_SIZE = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturedNotificationsScreen(
    notifications: List<CapturedNotification>,
    accessGranted: Boolean,
    adminActive: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onClearAll: () -> Unit,
    onClearApp: (String) -> Unit,
    onRequestAccess: () -> Unit,
    onDisableUninstallProtection: () -> Unit,
) {
    var search by remember { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<String?>(null) }
    var filterMenuOpen by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var confirmDisableAdmin by remember { mutableStateOf(false) }
    var confirmClearApp by remember { mutableStateOf<Pair<String, String>?>(null) }
    var selectedTitle by remember { mutableStateOf<String?>(null) }
    var listPage by remember { mutableIntStateOf(0) }
    var groupPage by remember { mutableIntStateOf(0) }
    var detailPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedApp) {
        selectedTitle = null
    }

    val apps = remember(notifications) {
        notifications.map { it.appLabel.ifBlank { it.packageName } to it.packageName }
            .distinct()
            .sortedBy { it.first.lowercase() }
    }

    val filtered = remember(notifications, search, selectedApp) {
        val q = search.trim().lowercase()
        notifications.filter { n ->
            val matchApp = selectedApp?.let { it == n.packageName } ?: true
            val matchSearch = if (q.isEmpty()) true else {
                n.title.lowercase().contains(q) ||
                    n.text.lowercase().contains(q) ||
                    n.subText.lowercase().contains(q) ||
                    n.bigText.lowercase().contains(q) ||
                    n.appLabel.lowercase().contains(q) ||
                    n.packageName.lowercase().contains(q)
            }
            matchApp && matchSearch
        }
    }

    val titleGroups = remember(filtered, selectedApp) {
        if (selectedApp == null) emptyList()
        else filtered
            .groupBy { it.titleGroupKey() }
            .map { (title, items) ->
                title to items.sortedByDescending { it.postedAt }
            }
            .sortedByDescending { (_, items) -> items.firstOrNull()?.postedAt ?: 0L }
    }

    val titleDetailItems = remember(filtered, selectedApp, selectedTitle) {
        if (selectedApp == null || selectedTitle == null) emptyList()
        else filtered.filter { it.titleGroupKey() == selectedTitle }
            .sortedByDescending { it.postedAt }
    }

    val showingTitleDetail = selectedApp != null && selectedTitle != null
    val showingTitleGroups = selectedApp != null && selectedTitle == null

    LaunchedEffect(filtered.size, search, selectedApp) {
        listPage = listPage.coerceIn(0, notifLastPageIndex(filtered.size))
    }
    LaunchedEffect(titleGroups.size, selectedApp) {
        groupPage = groupPage.coerceIn(0, notifLastPageIndex(titleGroups.size))
    }
    LaunchedEffect(titleDetailItems.size, selectedTitle) {
        detailPage = detailPage.coerceIn(0, notifLastPageIndex(titleDetailItems.size))
    }

    val pagedFiltered = remember(filtered, listPage) {
        notifPaginate(filtered, listPage, NOTIF_PAGE_SIZE)
    }
    val pagedTitleGroups = remember(titleGroups, groupPage) {
        notifPaginate(titleGroups, groupPage, NOTIF_PAGE_SIZE)
    }
    val pagedTitleDetailItems = remember(titleDetailItems, detailPage) {
        notifPaginate(titleDetailItems, detailPage, NOTIF_PAGE_SIZE)
    }

    fun handleBack() {
        when {
            showingTitleDetail -> selectedTitle = null
            else -> onBack()
        }
    }

    LaunchedEffect(Unit) { onRefresh() }

    BackHandler(enabled = showingTitleDetail) {
        selectedTitle = null
    }

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
        ) {
            TopBar(
                count = when {
                    showingTitleDetail -> titleDetailItems.size
                    showingTitleGroups -> titleGroups.size
                    else -> filtered.size
                },
                total = notifications.size,
                adminActive = adminActive,
                subtitle = when {
                    showingTitleDetail -> selectedTitle
                    showingTitleGroups -> apps.firstOrNull { it.second == selectedApp }?.first
                    else -> null
                },
                onBack = { handleBack() },
                onRefresh = onRefresh,
                onClear = { confirmClear = true },
                onDisableAdmin = { confirmDisableAdmin = true },
                onOpenFilter = { filterMenuOpen = true },
                filterMenuOpen = filterMenuOpen,
                onCloseFilter = { filterMenuOpen = false },
                apps = apps,
                selectedApp = selectedApp,
                onAppSelected = { pkg ->
                    selectedApp = pkg
                    filterMenuOpen = false
                }
            )

            if (!accessGranted) {
                AccessNotice(onRequestAccess = onRequestAccess)
            }

            SearchBar(
                value = search,
                onValueChange = { search = it },
                onClear = { search = "" }
            )

            AppCategoryRow(
                apps = apps,
                selectedApp = selectedApp,
                onSelectApp = { selectedApp = it },
                onClearApp = { pkg, label ->
                    confirmClearApp = pkg to label
                }
            )

            if (filtered.isEmpty()) {
                EmptyState(
                    hasAny = notifications.isNotEmpty(),
                    accessGranted = accessGranted
                )
            } else when {
                showingTitleDetail -> {
                    if (titleDetailItems.isEmpty()) {
                        EmptyState(
                            hasAny = notifications.isNotEmpty(),
                            accessGranted = accessGranted
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = pagedTitleDetailItems,
                                key = { n -> n.listKey() }
                            ) { n ->
                                NotificationCard(
                                    notification = n,
                                    showAppName = false,
                                    onAppClick = {},
                                )
                            }
                            item {
                                NotificationPaginationBar(
                                    page = detailPage,
                                    totalItems = titleDetailItems.size,
                                    pageSize = NOTIF_PAGE_SIZE,
                                    onPrevious = { detailPage -= 1 },
                                    onNext = { detailPage += 1 },
                                )
                            }
                        }
                    }
                }
                showingTitleGroups -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = pagedTitleGroups,
                            key = { (title, _) -> "${selectedApp}_$title" }
                        ) { (title, items) ->
                            TitleGroupCard(
                                title = title,
                                items = items,
                                onClick = { selectedTitle = title }
                            )
                        }
                        item {
                            NotificationPaginationBar(
                                page = groupPage,
                                totalItems = titleGroups.size,
                                pageSize = NOTIF_PAGE_SIZE,
                                onPrevious = { groupPage -= 1 },
                                onNext = { groupPage += 1 },
                            )
                        }
                    }
                }
                else -> {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = pagedFiltered,
                            key = { n -> n.listKey() }
                        ) { n ->
                            NotificationCard(
                                notification = n,
                                onAppClick = { selectedApp = n.packageName }
                            )
                        }
                        item {
                            NotificationPaginationBar(
                                page = listPage,
                                totalItems = filtered.size,
                                pageSize = NOTIF_PAGE_SIZE,
                                onPrevious = { listPage -= 1 },
                                onNext = { listPage += 1 },
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        ConfirmClearDialog(
            onConfirm = {
                confirmClear = false
                onClearAll()
            },
            onDismiss = { confirmClear = false }
        )
    }

    if (confirmDisableAdmin) {
        AlertDialog(
            onDismissRequest = { confirmDisableAdmin = false },
            title = { Text("Allow uninstall?") },
            text = {
                Text(
                    "This turns off uninstall protection so you can remove the app from " +
                        "system settings. The app will no longer be protected."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDisableAdmin = false
                    onDisableUninstallProtection()
                }) { Text("Allow uninstall", color = LovePink) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDisableAdmin = false }) { Text("Cancel") }
            }
        )
    }

    confirmClearApp?.let { (pkg, label) ->
        AlertDialog(
            onDismissRequest = { confirmClearApp = null },
            title = { Text("Clear $label notifications?") },
            text = {
                Text("This deletes all saved notifications for this app only.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmClearApp = null
                    onClearApp(pkg)
                    if (selectedApp == pkg) selectedApp = null
                }) { Text("Clear", color = LovePink) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearApp = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppCategoryRow(
    apps: List<Pair<String, String>>,
    selectedApp: String?,
    onSelectApp: (String?) -> Unit,
    onClearApp: (pkg: String, label: String) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedApp == null,
                onClick = { onSelectApp(null) },
                label = { Text("All", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LovePink.copy(alpha = 0.22f),
                    selectedLabelColor = Cream,
                    containerColor = Color.White.copy(alpha = 0.08f),
                    labelColor = Cream
                )
            )
        }
        items(apps, key = { it.second }) { (label, pkg) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = selectedApp == pkg,
                    onClick = { onSelectApp(pkg) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LovePink.copy(alpha = 0.22f),
                        selectedLabelColor = Cream,
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Cream
                    )
                )
                IconButton(
                    onClick = { onClearApp(pkg, label) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear $label",
                        tint = Gold,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(2.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    count: Int,
    total: Int,
    adminActive: Boolean,
    subtitle: String? = null,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onClear: () -> Unit,
    onDisableAdmin: () -> Unit,
    onOpenFilter: () -> Unit,
    filterMenuOpen: Boolean,
    onCloseFilter: () -> Unit,
    apps: List<Pair<String, String>>,
    selectedApp: String?,
    onAppSelected: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cream)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Notifications",
                color = Cream,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (total == 0) "no captures yet" else "showing $count of $total",
                color = Champagne.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Gold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
        Box {
            IconButton(onClick = onOpenFilter) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Cream)
            }
            DropdownMenu(
                expanded = filterMenuOpen,
                onDismissRequest = onCloseFilter,
            ) {
                DropdownMenuItem(
                    text = { Text("All apps") },
                    onClick = { onAppSelected(null) }
                )
                apps.forEach { (label, pkg) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = { onAppSelected(pkg) },
                        trailingIcon = if (selectedApp == pkg) {
                            { Icon(Icons.Default.FilterList, contentDescription = null) }
                        } else null
                    )
                }
            }
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Cream)
        }
        IconButton(onClick = onClear) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Clear", tint = Cream)
        }
        if (adminActive) {
            IconButton(onClick = onDisableAdmin) {
                Icon(
                    Icons.Default.GppMaybe,
                    contentDescription = "Allow uninstall",
                    tint = Gold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        placeholder = { Text("Search title, text, or app", color = Cream.copy(alpha = 0.5f)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Cream.copy(alpha = 0.7f))
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Cream)
                }
            }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Cream,
            unfocusedTextColor = Cream,
            cursorColor = LovePink,
            focusedBorderColor = LovePink,
            unfocusedBorderColor = Cream.copy(alpha = 0.25f),
        ),
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun AccessNotice(onRequestAccess: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LovePink.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Notification access required",
                color = Cream,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Grant \"Notification access\" so the app can read and save all notifications shown on this device.",
                color = Cream.copy(alpha = 0.85f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onRequestAccess,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LovePink,
                    contentColor = Cream
                ),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text("Open settings")
            }
        }
    }
}

@Composable
private fun EmptyState(hasAny: Boolean, accessGranted: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = Cream.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = when {
                    !accessGranted -> "Waiting for access"
                    !hasAny -> "No notifications captured yet"
                    else -> "No matches for the current filter"
                },
                color = Cream,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = when {
                    !accessGranted -> "Enable notification access above to start saving."
                    !hasAny -> "New incoming notifications will appear here automatically."
                    else -> "Try clearing the search or app filter."
                },
                color = Cream.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TitleGroupCard(
    title: String,
    items: List<CapturedNotification>,
    onClick: () -> Unit,
) {
    val latest = items.firstOrNull() ?: return
    val preview = latest.displayMessage().takeIf { it != "(No message)" }.orEmpty()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Cream,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${items.size} notification${if (items.size == 1) "" else "s"}",
                    color = Gold,
                    fontSize = 12.sp
                )
                if (preview.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = preview,
                        color = Cream.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Latest: ${formatTime(latest.postedAt)}",
                    color = Cream.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Cream.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: CapturedNotification,
    onAppClick: () -> Unit,
    showAppName: Boolean = true,
) {
    val titleText = notification.displayTitle()
    val messageText = notification.displayMessage()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (showAppName) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Gold.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (notification.appLabel.ifBlank { notification.packageName })
                                .firstOrNull()?.uppercase() ?: "?",
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.size(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        TextButton(
                            onClick = onAppClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = notification.appLabel.ifBlank { notification.packageName },
                                color = Gold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = formatTime(notification.postedAt),
                        color = Cream.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = titleText,
                        color = Cream,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = formatTime(notification.postedAt),
                        color = Cream.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            if (showAppName) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = titleText,
                    color = Cream,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(3.dp))
            Text(
                text = messageText,
                color = if (messageText == "(No message)") {
                    Cream.copy(alpha = 0.5f)
                } else {
                    Cream.copy(alpha = 0.85f)
                },
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ConfirmClearDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear all captured notifications?") },
        text = { Text("This permanently deletes everything saved locally.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Clear", color = LovePink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private val TODAY_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())
private val DATE_FORMAT = SimpleDateFormat("MMM d • HH:mm", Locale.getDefault())

private fun formatTime(millis: Long): String {
    val now = System.currentTimeMillis()
    val sameDay = (now / 86_400_000L) == (millis / 86_400_000L) &&
        java.util.TimeZone.getDefault().getOffset(now) == java.util.TimeZone.getDefault().getOffset(millis)
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = millis
    val day = cal.get(java.util.Calendar.DAY_OF_YEAR)
    val year = cal.get(java.util.Calendar.YEAR)
    cal.timeInMillis = now
    val today = cal.get(java.util.Calendar.DAY_OF_YEAR)
    val thisYear = cal.get(java.util.Calendar.YEAR)
    return if (day == today && year == thisYear) {
        TODAY_FORMAT.format(Date(millis))
    } else {
        DATE_FORMAT.format(Date(millis))
    }
}

@Composable
private fun NotificationPaginationBar(
    page: Int,
    totalItems: Int,
    pageSize: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    if (totalItems <= pageSize) return

    val totalPages = notifPageCount(totalItems, pageSize)
    val start = page * pageSize + 1
    val end = minOf((page + 1) * pageSize, totalItems)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = onPrevious, enabled = page > 0) {
            Text("Previous", color = Cream)
        }
        Text(
            text = "$start–$end of $totalItems",
            color = Cream.copy(alpha = 0.8f),
            fontSize = 12.sp,
        )
        OutlinedButton(onClick = onNext, enabled = page < totalPages - 1) {
            Text("Next", color = Cream)
        }
    }
}

private fun <T> notifPaginate(items: List<T>, page: Int, pageSize: Int): List<T> {
    if (items.isEmpty()) return emptyList()
    val safePage = page.coerceIn(0, notifLastPageIndex(items.size, pageSize))
    val start = safePage * pageSize
    return items.subList(start, minOf(start + pageSize, items.size))
}

private fun notifPageCount(totalItems: Int, pageSize: Int): Int =
    maxOf(1, (totalItems + pageSize - 1) / pageSize)

private fun notifLastPageIndex(totalItems: Int, pageSize: Int = NOTIF_PAGE_SIZE): Int =
    maxOf(0, notifPageCount(totalItems, pageSize) - 1)
