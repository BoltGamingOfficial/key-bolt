# Key-Bolt delivery roadmap

## Foundation source baseline

- [x] Android native input-method service and custom keyboard view
- [x] iOS app and native keyboard extension
- [x] Gold / black lightning identity, presets, RGB and HEX controls
- [x] QWERTY, AZERTY, QWERTZ, Dvorak, Colemak and alphabetical layouts
- [x] Size and hand-position modes on Android and iOS
- [x] Named local clipboard collections and colored cards
- [x] Android attachment URI references and iOS shared-container file import
- [x] Long-text editor with explicit copy action
- [x] Android recognizer handoff
- [x] Incognito session behavior and password/no-personalized-learning handling
- [x] Main-only development and release checklist

## Next engineering milestones

1. **Build and device validation:** build Android with Gradle, generate the iOS project with XcodeGen, fix compiler/lint errors, and test the IME and extension on physical devices.
2. **Multilingual composition:** integrate properly licensed engines for Chinese Pinyin, Japanese Kana/Kanji, Korean and Indic scripts; add candidate selection, locale-specific layouts and right-to-left support. Enumerate installed system locales rather than assuming a fixed list.
3. **Text intelligence:** implement autocapitalization, suggestion candidates and language-aware autocorrection dictionaries. Connect per-app profiles to the correction engine.
4. **Clipboard completeness:** add attachment previews, quotas, URI lifecycle handling, secure deletion, folder rename/delete/reorder and rich-content compatibility tests.
5. **Accessibility:** test screen readers, larger text, contrast, touch targets, haptics and reduced motion across orientations and device sizes.
6. **Privacy and security:** threat-model local clipboard persistence, define retention/clear-all controls, test secure fields, review permissions and publish a privacy policy before store submission.
7. **Release quality:** add automated tests, CI verification, signing configuration, reproducible release notes and signed distribution artifacts.

## Definition of done

A settings toggle is not proof that a feature works. Test each feature through the actual system keyboard, on its target OS, and document known limitations in the feature matrix.

## Release gate

A version is eligible for GitHub Releases only after Android APK/AAB and signed iOS distribution artifacts are produced from the tagged source, install-tested and checked against the release checklist. Keep all development on main; never publish placeholder or unverified binaries.
