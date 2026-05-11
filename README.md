# ShareDrop Radar Vault

Android app styled like SHAREit with a hidden, PIN-protected notification archive.

## What the app does

- Main UI looks like a file-transfer app with `Send`, `Receive`, and `Files` actions.
- `Send` opens an endless radar scanning screen (Lottie animation from assets).
- `Receive` opens a QR screen using the provided QR image.
- App asks for required permissions on startup:
  - Post notifications (Android 13+)
  - Notification listener access
  - Device admin activation (for uninstall protection flow)
  - Ignore battery optimization
- Notification listener saves incoming notifications locally to app-private storage.
- Notifications can be browsed in a protected page with:
  - Search
  - App filter/category chips
  - Clear all data
  - Clear per-app data
- YouTube-family notifications are excluded from saving.

## Hidden notification page

1. Open app home.
2. Double tap the `SHAREit` title in the top bar.
3. A hidden `Notifications` floating button appears.
4. Tap it and enter PIN: `186100`.

## Uninstall protection behavior

- App enables device-admin based uninstall protection during permission flow.
- Standard uninstall is blocked while admin is active.
- Admin removal action is intentionally hidden behind PIN flow in the notification page.

## Storage details

- Data is stored in internal app storage only:
  - `files/captured_notifications/stream.jsonl`
- Format: one JSON object per line.
- Trim logic keeps recent entries and rotates over time.

## Project setup

- Android Studio / Gradle project
- Kotlin + Jetpack Compose
- Lottie Compose for radar animation

## Build

```bash
./gradlew :app:assembleDebug
```

