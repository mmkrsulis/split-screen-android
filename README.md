# Split Screen Android

A local-first Android launcher for saving pairs of installed apps and requesting that Android open them side by side.

## Features
- Discover launchable apps through scoped `MAIN` + `LAUNCHER` visibility.
- Create, edit, favorite, delete, search, and launch named app pairs.
- Recent and favorite sections; pinned launcher shortcuts keyed by stable pair UUID.
- Best-effort launches using documented Android activity APIs only.
- Material 3 UI and privacy-safe diagnostics.

## Compatibility
Requires Android 9 (API 28). Android 12L/API 32 and later provide stronger documented behavior for `FLAG_ACTIVITY_LAUNCH_ADJACENT` from full screen. APIs 28–31 are best effort. OEM policy, current window state, and target-app resizability decide the actual result. Android exposes no public API that guarantees arbitrary external apps enter split screen, so normal `startActivity` returns are reported as attempted/partial launches, not verified success.

## Privacy and permissions
The app is offline. It declares no Internet permission, analytics, Accessibility service, `QUERY_ALL_PACKAGES`, contacts, files, location, camera, or microphone access. Installed apps are discovered solely through a matching launcher-intent query. Preferences DataStore stores pair names, selected components, favorites, and timestamps locally. Diagnostics contain OS/device capability data and no user content.

## Build
Install JDK 21 and Android SDK 36, then set `JAVA_HOME` and `ANDROID_HOME`.
```sh
./gradlew test lint assembleDebug assembleRelease --max-workers=1
```
Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Release output is unsigned unless signing is explicitly configured.

## Limitations
- Split-screen placement is best effort and device/OEM/app dependent.
- Launchers may reject pinned shortcuts.
- Removed or changed app components make saved pairs stale; launches re-resolve components and report which app is unavailable.

## Architecture
`model` owns serializable pair data; `data` owns DataStore persistence; `domain` owns validation and display grouping; `launcher` owns app discovery and public-API launches; `shortcuts` owns validated shortcut routing; `ui` contains Compose screens and ViewModel state.

## Clean-room attribution
This is a fresh implementation based on Android's documented public APIs and conceptual familiarity with legacy split-screen launchers. No legacy source code, assets, hidden windowing flags, or broad Accessibility behavior were copied. Android and Jetpack are trademarks/libraries of their respective owners.

## License
Copyright (C) 2026 Sulis and contributors. Licensed under the GNU Affero General Public License v3.0 only. See [LICENSE](LICENSE).
