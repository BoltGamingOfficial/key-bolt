# Feature status matrix

This matrix distinguishes implemented behavior from saved preferences and roadmap work.

| Feature | Android | iOS | Notes |
|---|---|---|---|
| Native system keyboard | InputMethodService baseline | Keyboard extension baseline | Needs physical-device QA and store signing |
| Gold / black lightning look | Implemented | Implemented | Presets and RGB / HEX controls in companion apps |
| QWERTY / AZERTY / QWERTZ | Basic arrangements | Basic arrangements | ASCII-oriented layouts |
| Dvorak / Colemak / alphabetical | Basic arrangements | Basic arrangements | Requires locale-specific validation |
| One-hand / compact / thumb modes | Basic width modes | Basic width modes | Thumb uses a narrower centered footprint |
| Keyboard size | Implemented | Implemented | User-adjustable scale |
| Clipboard collections | App + keyboard filter | App + keyboard filter | Named collections |
| Colored clipboard cards | Implemented | Implemented | Color selected before saving |
| Text clips | Keyboard inserts saved text | Keyboard inserts saved text | Local-only app preferences / App Group |
| Photo / document references | Imported; rich insertion attempted | Imported to shared App Group | iOS keyboard currently inserts text only |
| Long-text editor | Implemented | Implemented | Explicit copy action |
| Voice dictation | Android recognizer handoff | OS dictation remains available | Android requires a compatible recognizer app/service |
| Incognito | Session toggle + password/no-learning default | Session toggle + default preference | Not protection against OS/host recording |
| Autocorrection | Preference only | Preference only | Dictionaries, suggestions and replacement logic not complete |
| Per-app autocorrection | Preference stored by package name | Not implemented | Android control is not yet connected to correction logic |
| Multilingual composition | Language subtypes only | System keyboard switching | Chinese/Japanese/Indic engines not bundled |
| Emoji | Unicode emoji | Unicode emoji | No custom proprietary emoji artwork |
| Dynamic text | Scaled labels + Compose UI | Dynamic Type-aware labels | Needs screen-reader and large-text device QA |

## Release quality gates

A settings control alone does not mark a feature complete. Completion requires successful behavior through the actual system keyboard, accessibility checks, privacy review and test evidence on target devices.
