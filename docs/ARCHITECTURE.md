# Key-Bolt architecture

## Native keyboard targets

A regular application screen cannot replace the system keyboard by itself. Android exposes an InputMethodService and InputConnection; Apple distributes custom keyboards as app extensions with a more restrictive lifecycle and host-app boundary. Key-Bolt therefore has native keyboard targets and separate companion-app interfaces.

## Android

- KeyboardService is the installed IME and writes through the focused editor's InputConnection.
- BoltKeyboardView draws the keyboard with a lightweight Canvas view, separate from the Compose settings app.
- KeyBoltPrefs stores appearance, layout, hand mode, folders and saved clip metadata in app-private preferences.
- VoiceInputActivity delegates speech recognition to the device's configured RecognizerIntent handler.
- Attachment insertion uses InputConnection.commitContent only when the target editor advertises a compatible MIME type and accepts the content.
- Password-like input variations and editors setting IME_FLAG_NO_PERSONALIZED_LEARNING begin in incognito mode. The mode cannot be toggled off for those fields.

## iOS

- The containing SwiftUI app manages appearance, layout, collections, imported attachment files and the long-text editor.
- The extension reads required settings and saved text snippets from the configured App Group.
- The extension requests no Full Access, does not read the system pasteboard and does not make network requests.
- Host apps can restrict or replace custom keyboards; secure text fields do not accept third-party keyboards.

## Privacy defaults

Typed text is sent only to the focused editor through the operating-system keyboard API. Key-Bolt does not transmit typed text. Clips are saved only through explicit user action in the companion app. Incognito suppresses the saved-clip surface for the active session. It is not a guarantee against host-app, operating-system or third-party recording.

## Shared behavior and platform-specific behavior

The shared contract covers the gold/black visual system, layout names, hand modes, sizing, collections and editor workflow. Text composition, spell checking, dictation, rich-content insertion, locale switching and secure-field behavior are platform-specific and must be tested independently.
