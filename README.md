# Key-Bolt v1.0

**CUSTOM. FAST. BOLT.**

A native Android keyboard and companion workspace, with an iOS app and keyboard-extension project.

## Android v1.0

- Native Android IME with QWERTY, AZERTY, QWERTZ, Dvorak, Colemak and alphabetical arrangements.
- Adjustable keyboard size and one-hand/compact layouts.
- Gold/black default theme, color presets and custom RGB/HEX accent.
- Local clipboard tabs, named collections, colored text/file clips and optional automatic clipboard capture while the keyboard is visible.
- Attachment references through Android's Storage Access Framework. Rich-content insertion depends on the receiving editor advertising the MIME type and accepting the content.
- Long-text editor with copy and clear actions.
- Incognito session mode; password fields and editors requesting no personalized learning automatically suppress clip capture.
- Optional voice recognition handoff to the device's speech-recognition provider.

## Download

The GitHub Actions release workflow builds an installable Android debug-signed APK and publishes it to the **v1.0** GitHub Release after the Android build succeeds. This APK is suitable for direct installation/testing. It is not signed with a private production upload key and is not a Play Store release. A production-signed artifact requires the owner's private signing key, stored as GitHub Actions secrets.

## Build Android

Requirements: JDK 17, Android SDK 35, Gradle 8.9.

```bash
gradle --no-daemon testDebugUnitTest assembleDebug
```

Open the repository in Android Studio, install the generated APK, then enable **Key-Bolt** in Android Settings → System → Keyboard → On-screen keyboard and select it as the active input method.

## iOS

The iOS host app and custom keyboard extension are maintained in `ios/`. Generate the Xcode project with XcodeGen and build with Xcode. Apple limits third-party keyboards; secure fields and some apps force the system keyboard. The extension does not request Full Access and does not read the system pasteboard.

## Storage and platform limits

- Android attachment selection accepts provider-backed files; actual capacity is bounded by device storage, filesystem and provider limits. Rich-content insertion only works in apps that advertise compatible MIME types.
- iOS custom keyboard extensions have stricter access and host-app restrictions than Android.
- Large file handling is not the same as unlimited capacity. Do not assume arbitrary file sizes can be loaded into memory or inserted into every app.
- Android clip metadata/text is currently stored in app-private SharedPreferences. Very large text documents should be edited in Text Studio and exported rather than treated as unlimited clipboard payloads. A file-backed clip-store migration is a post-v1.0 engineering milestone.

## Privacy

Key-Bolt does not request Internet permission for Android keyboard operation and does not send typed text to a Key-Bolt server. Incognito disables Key-Bolt's own automatic clipboard capture and hides saved clips for the active session. It cannot prevent the operating system, destination app, accessibility services, backups or screen recording from observing content.

## Known v1.0 limitations

- Autocorrection and per-app correction controls are preferences only; a full language-aware suggestion engine is not included.
- Multilingual subtype declarations do not mean Pinyin, Kana/Kanji, Korean or Indic composition engines are bundled.
- iOS keyboard capabilities are not feature-equivalent to Android.
- Attachment previews, media playback, file-backed text clip storage and full editor formatting are future milestones.

See [Feature Matrix](docs/FEATURE_MATRIX.md), [Architecture](docs/ARCHITECTURE.md), and [Release Checklist](docs/RELEASE_CHECKLIST.md).
