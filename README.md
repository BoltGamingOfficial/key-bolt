# Key-Bolt ⚡

A privacy-first custom keyboard foundation for Android and iOS, using the Bolt Gold visual identity (gold keys, dark base, lightning accents).

## Project status

Key-Bolt is in development. There is currently no installable app or published build.

The Android source includes a Compose settings shell, a custom input-method service, configurable layouts, local clip collections, a long-text editor, theme controls, and privacy-oriented defaults. The iOS source includes a SwiftUI companion app and a custom keyboard extension with saved-snippet insertion.

## Current foundation

- Android input method and iOS keyboard extension.
- Gold default theme, preset accents, RGB and HEX customization.
- QWERTY, AZERTY, QWERTZ, Dvorak, Colemak and alphabetical layouts.
- Full, compact, one-hand and thumb-mode preference scaffolding.
- Local named clipboard collections, colored snippet cards, and attachment references/import.
- Long-text editor with copy action.
- Incognito session toggle and password-field privacy behavior.
- Android language subtype declarations; iOS uses system keyboard language switching.

## Important limitations

- **Language input engines:** declaring a language or layout is not equivalent to a complete IME. Chinese Pinyin/handwriting, Japanese Kana/Kanji, Korean composition, Indic transliteration, RTL shaping and language-aware prediction need dedicated input-engine work and native-speaker QA. The iOS extension currently provides basic Latin layouts and a small emoji palette; native iOS dictation and system language keyboards remain available through iOS.
- **Voice:** native speech-to-text integration is not complete. Android voice requires runtime permission, speech-recognition availability and clear privacy disclosures. iOS dictation is system-owned.
- **Autocorrection:** settings are currently preferences/scaffolding; a multilingual dictionary, suggestion strip, learning model and per-app policy engine are not complete.
- **Clipboard attachments:** Android stores persisted content URIs and iOS imports selected files into the shared app-group container. A keyboard cannot insert arbitrary files into most text fields; it can insert text and supported rich content only where the target app and OS permit it.
- **Apple constraints:** custom keyboards cannot operate in secure text fields, some apps disable third-party keyboards, and clipboard access is permission/privacy constrained. This source does not request iOS Full Access.
- **Dynamic text:** the keyboard respects scalable text in its own UI. It cannot force other apps or Android/iOS system UI to adopt a global dynamic-text style.
