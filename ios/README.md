# iOS build

The iOS app and custom keyboard extension are generated from project.yml with XcodeGen.

```sh
brew install xcodegen
cd ios
xcodegen generate
open KeyBolt.xcodeproj
```

Select the KeyBolt scheme, configure signing for both the app and keyboard extension, then build on macOS with Xcode. The extension uses an App Group to read clips saved explicitly in the containing app.

The iOS keyboard intentionally does not request Full Access. Consequently, it cannot silently read the system clipboard, call remote speech services, or access arbitrary files. iOS dictation and multilingual input methods remain system-controlled.
