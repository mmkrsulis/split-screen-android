# Contributing

By contributing you agree that your work is licensed under AGPL-3.0-only.

1. Open an issue for substantial behavior or platform-policy changes.
2. Use public Android APIs; do not introduce reflection, hidden APIs, analytics, Internet access, or broad package visibility.
3. Add a failing behavioral test first, implement, then run `./gradlew test lint assembleDebug assembleRelease`.
4. Keep the app free of Accessibility services unless a reviewed, explicit native-first integration is implemented.
5. Use Kotlin formatting and SPDX headers on source files. Never commit signing keys or local SDK paths.
