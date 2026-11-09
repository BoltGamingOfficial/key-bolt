# Key-Bolt release checklist

Use a version tag and GitHub Release for each distributable version. Source-only commits are not releases.

## Before tagging

- [ ] Confirm the exact version and increment Android versionCode.
- [ ] Keep the version consistent in Android and both iOS Info.plist files.
- [ ] Build Android debug/release variants; resolve lint and test failures.
- [ ] Generate and build the iOS app and keyboard extension with XcodeGen/Xcode.
- [ ] Configure Apple signing and App Group group.com.bolt.keybolt for both targets.
- [ ] Test Android IME enablement, editor types, password fields, no-personalized-learning flags, rotation and multiple screen sizes.
- [ ] Test iOS installation, extension activation, secure fields, host-app compatibility and App Group clip sharing.
- [ ] Test typing, delete/return, layouts, display modes, theme settings, RGB/HEX values and large text.
- [ ] Exercise clipboard text, collection switching, attachment failure/fallback and delete behavior.
- [ ] Verify incognito, offline behavior and privacy messaging.
- [ ] Ensure every incomplete feature is described accurately in release notes.

## Publish

1. Create a version tag from the verified commit on main, e.g. v1.0.0.
2. Create a GitHub Release for that tag with title Key-Bolt v1.0.0 (or the product-facing style Key-Bolt v1).
3. Attach the tested Android APK and/or AAB.
4. Attach the signed iOS distribution artifact appropriate for TestFlight/App Store distribution. Do not substitute a simulator build.
5. Include checksums, installation notes, supported OS versions and known limitations.
6. Verify the release and binary assets appear in the repository's Releases tab.

## Current baseline

The source has not been built or device-tested in this environment. Do not publish it as a binary release until every release gate above passes.
