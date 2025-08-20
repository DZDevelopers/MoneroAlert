
# MoneroAlert Android (Kotlin)

A simple Android app that periodically checks a Monero RPC node for chain reorganizations and sends a local push notification if a reorg of configurable depth is detected.

Features
- Configure node URL and port
- Runs periodic checks in the background using WorkManager (default 15 minutes)
- Alerts when reorg depth >= threshold (default 4)
- Saves recent alerts inside the app for review
- Clean, minimal, easy-to-understand Kotlin source code

Important notes
- This project provides the full Android source code. **I cannot compile an APK in this environment**, so you'll need to build the APK locally using Android Studio or the Gradle wrapper. Instructions below.
- WorkManager enforces a minimum period of 15 minutes for periodic work; intervals lower than 15 will be clamped to 15 minutes by the system.

How it works (short)
1. App saves settings (node + port + interval + threshold).
2. Periodic WorkManager job runs every `interval` minutes (minimum 15).
3. Worker calls Monero RPC `get_info` to get current height, then requests `get_block_header_by_height` for the top `threshold+1` blocks.
4. It compares the top block hashes to the previous saved window. If `reorgDepth >= threshold`, it sends a notification and logs the event.
5. The saved window of block hashes is updated for the next comparison.

Build & install (Android Studio)
1. Open Android Studio and select "Open an existing project" -> choose this repository folder.
2. Let Gradle sync. If prompted, install recommended SDKs/Gradle plugin.
3. Build -> Build Bundle(s) / APK(s) -> Build APK(s).
4. Install the generated APK on your device.

Build via command-line (Linux/macOS/Windows with proper SDK)
1. Install Android SDK / JDK 17 and set `ANDROID_HOME` / `JAVA_HOME`.
2. From the project root run: `./gradlew assembleDebug` (or `gradlew.bat assembleDebug` on Windows).
3. APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

Security & privacy
- This app sends RPC calls to whatever node you configure. Be careful not to point it to untrusted endpoints that could log or modify your requests. The app stores the node URL in plaintext in SharedPreferences.
- The app only uses local notifications (no external push services).

Optional improvements you can implement
- Use TLS/HTTPS verification for RPC hosts and allow self-signed certs options.
- Use Room DB for logs instead of SharedPreferences.
- Add a setting to only run checks on Wi‑Fi or when charging.
- Add authentication to RPC requests if you use an authenticated node.
- Allow exporting logs and alerts.

Project layout
- `app/src/main/java/com/example/moneroalert` — Kotlin source
- `app/src/main/res` — layouts and resources
- `build.gradle` and `app/build.gradle` — Gradle configuration

If you'd like, I can:
- Add a GitHub Actions workflow file that builds a debug APK automatically on push and attaches it to release artifacts.
- Convert logs to Room DB and add export/share buttons.
- Add unit tests for the RPC client using mocked responses.

