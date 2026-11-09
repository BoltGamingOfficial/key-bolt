import SwiftUI
import UniformTypeIdentifiers
import UIKit

struct ContentView: View {
    var body: some View {
        TabView {
            AppearanceView().tabItem { Label("Keyboard", systemImage: "keyboard") }
            ClipboardView().tabItem { Label("Clipboard", systemImage: "doc.on.clipboard") }
            LongTextEditorView().tabItem { Label("Editor", systemImage: "text.alignleft") }
            PreferencesView().tabItem { Label("Settings", systemImage: "gearshape") }
        }
        .tint(Color(hex: BoltShared.accentHex()))
        .background(Color.black)
    }
}

private struct ThemePreset: Identifiable {
    let name: String
    let value: String
    var id: String { name }
}

struct AppearanceView: View {
    @State private var layout = BoltShared.layout()
    @State private var mode = BoltShared.mode()
    @State private var keyboardScale = BoltShared.defaults.object(forKey: "bolt.scale") as? Double ?? 1.0
    @State private var red = 214.0
    @State private var green = 166.0
    @State private var blue = 66.0
    @State private var hex = BoltShared.accentHex()
    private let presets: [ThemePreset] = [
        ThemePreset(name: "Bolt Gold", value: "#D6A642"),
        ThemePreset(name: "Midnight", value: "#9CA3AF"),
        ThemePreset(name: "Electric Blue", value: "#42A5F5"),
        ThemePreset(name: "Emerald", value: "#4CAF79"),
        ThemePreset(name: "Rose", value: "#E58AA7"),
        ThemePreset(name: "Violet", value: "#AB8BEF")
    ]
    private let layouts = ["QWERTY", "AZERTY", "QWERTZ", "DVORAK", "COLEMAK", "ALPHABETICAL"]
    private let modes = ["FULL", "ONE_HAND_LEFT", "ONE_HAND_RIGHT", "THUMB", "COMPACT"]

    var body: some View {
        NavigationStack {
            Form {
                Section("Theme presets") {
                    ForEach(presets) { preset in
                        let name = preset.name
                        let value = preset.value
                        Button {
                            setColor(value)
                        } label: {
                            HStack {
                                Circle().fill(Color(hex: value)).frame(width: 24, height: 24)
                                Text(name).foregroundStyle(.primary)
                                Spacer()
                                if hex.uppercased() == value { Image(systemName: "checkmark").foregroundStyle(Color(hex: value)) }
                            }
                        }
                    }
                }
                Section("Custom RGB / HEX") {
                    colorSlider("Red", value: $red)
                    colorSlider("Green", value: $green)
                    colorSlider("Blue", value: $blue)
                    HStack {
                        Text("HEX")
                        TextField("#D6A642", text: $hex)
                            .textInputAutocapitalization(.characters)
                            .autocorrectionDisabled()
                            .multilineTextAlignment(.trailing)
                            .onChange(of: hex) { newValue in
                                if isValidHex(newValue) { BoltShared.setAccentHex(normalizedHex(newValue)) }
                            }
                    }
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color(hex: normalizedHex(hex)))
                        .frame(height: 34)
                }
                Section("Keyboard layout") {
                    Picker("Arrangement", selection: $layout) {
                        ForEach(layouts, id: \.self) { Text($0).tag($0) }
                    }
                    .onChange(of: layout) { BoltShared.setLayout($0) }
                }
                Section("Hand position and size") {
                    Picker("Mode", selection: $mode) {
                        ForEach(modes, id: \.self) { Text($0.replacingOccurrences(of: "_", with: " ")).tag($0) }
                    }
                    .onChange(of: mode) { BoltShared.setMode($0) }
                    HStack { Text("Keyboard size"); Spacer(); Text("\(Int(keyboardScale * 100))%").foregroundStyle(.secondary) }
                    Slider(value: $keyboardScale, in: 0.8...1.2, step: 0.05)
                        .onChange(of: keyboardScale) { BoltShared.defaults.set($0, forKey: "bolt.scale") }
                }
                Section("Enable the keyboard") {
                    Text("Open Settings → General → Keyboard → Keyboards → Add New Keyboard, then choose Key-Bolt. The system may display a warning for third-party keyboards; Key-Bolt does not request open access in this build.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
            .navigationTitle("Key-Bolt ⚡")
        }
    }

    @ViewBuilder
    private func colorSlider(_ title: String, value: Binding<Double>) -> some View {
        VStack(alignment: .leading, spacing: 3) {
            HStack {
                Text(title)
                Spacer()
                Text("\(Int(value.wrappedValue))").monospacedDigit().foregroundStyle(.secondary)
            }
            Slider(value: value, in: 0...255, step: 1) { _ in }
                .tint(Color(hex: normalizedHex(hex)))
                .onChange(of: value.wrappedValue) { _ in saveRGB() }
        }
    }

    private func saveRGB() {
        let newHex = String(format: "#%02X%02X%02X", Int(red), Int(green), Int(blue))
        hex = newHex
        BoltShared.setAccentHex(newHex)
    }

    private func setColor(_ value: String) {
        hex = value
        BoltShared.setAccentHex(value)
        let cleaned = value.trimmingCharacters(in: CharacterSet(charactersIn: "#"))
        let number = UInt64(cleaned, radix: 16) ?? 0xD6A642
        red = Double((number >> 16) & 0xFF)
        green = Double((number >> 8) & 0xFF)
        blue = Double(number & 0xFF)
    }
}

struct ClipboardView: View {
    @State private var folders = BoltShared.folders()
    @State private var clips = BoltShared.clips()
    @State private var selectedFolder = BoltShared.defaults.string(forKey: "bolt.activeFolder") ?? "personal"
    @State private var snippet = ""
    @State private var folderName = ""
    @State private var showingFolder = false
    @State private var showingImporter = false
    @State private var cardColor = "#282828"
    private let cardColors = ["#282828", "#47351B", "#203D32", "#20354A", "#462B3B", "#3B3154"]

    var body: some View {
        NavigationStack {
            List {
                Section("Collections") {
                    ForEach(folders) { folder in
                        HStack {
                            Circle().fill(Color(hex: folder.colorHex)).frame(width: 12, height: 12)
                            Text(folder.name)
                            Spacer()
                            if selectedFolder == folder.id { Image(systemName: "checkmark").foregroundStyle(.yellow) }
                        }
                        .contentShape(Rectangle())
                        .onTapGesture { selectedFolder = folder.id; BoltShared.defaults.set(folder.id, forKey: "bolt.activeFolder") }
                    }
                    Button { showingFolder = true } label: { Label("Add collection", systemImage: "plus") }
                }
                Section("Save a new clip") {
                    TextEditor(text: $snippet).frame(minHeight: 90)
                    Button {
                        let pasted = UIPasteboard.general.string ?? ""
                        if !pasted.isEmpty { snippet = pasted }
                    } label: { Label("Paste from clipboard", systemImage: "doc.on.clipboard") }
                    HStack {
                        ForEach(cardColors, id: \.self) { value in
                            Circle().fill(Color(hex: value)).frame(width: 28, height: 28)
                                .overlay(Circle().stroke(cardColor == value ? .white : .clear, lineWidth: 2))
                                .onTapGesture { cardColor = value }
                        }
                    }
                    Button {
                        guard !snippet.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
                        clips.append(BoltClip(id: UUID().uuidString, folderID: selectedFolder, text: snippet,
                            attachmentPath: "", displayName: "", colorHex: cardColor, createdAt: Date().timeIntervalSince1970))
                        BoltShared.saveClips(clips)
                        snippet = ""
                    } label: { Label("Save text snippet", systemImage: "square.and.arrow.down") }
                    Button { showingImporter = true } label: { Label("Import photo or document", systemImage: "paperclip") }
                }
                Section("Saved items") {
                    ForEach(clips.filter { $0.folderID == selectedFolder }) { clip in
                        VStack(alignment: .leading, spacing: 6) {
                            Text(clip.attachmentPath.isEmpty ? clip.text : "📎 \(clip.displayName)")
                                .lineLimit(4)
                            Text(clip.attachmentPath.isEmpty ? "Text snippet" : "Attachment saved in app storage")
                                .font(.caption).foregroundStyle(.secondary)
                        }
                        .padding(8)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color(hex: clip.colorHex), in: RoundedRectangle(cornerRadius: 10))
                        .swipeActions {
                            Button(role: .destructive) {
                                clips.removeAll { $0.id == clip.id }
                                BoltShared.saveClips(clips)
                            } label: { Label("Delete", systemImage: "trash") }
                        }
                    }
                }
            }
            .navigationTitle("Clipboard")
            .alert("New collection", isPresented: $showingFolder) {
                TextField("Collection name", text: $folderName)
                Button("Create") {
                    guard !folderName.isEmpty else { return }
                    let folder = BoltFolder(id: UUID().uuidString, name: folderName, colorHex: cardColor)
                    folders.append(folder)
                    BoltShared.saveFolders(folders)
                    selectedFolder = folder.id
                    BoltShared.defaults.set(folder.id, forKey: "bolt.activeFolder")
                    folderName = ""
                }
                Button("Cancel", role: .cancel) {}
            }
            .fileImporter(isPresented: $showingImporter, allowedContentTypes: [.image, .pdf, .plainText], allowsMultipleSelection: false) { result in
                guard case .success(let urls) = result, let url = urls.first else { return }
                saveAttachment(url)
            }
        }
    }

    private func saveAttachment(_ source: URL) {
        let granted = source.startAccessingSecurityScopedResource()
        defer { if granted { source.stopAccessingSecurityScopedResource() } }
        guard let container = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: BoltShared.groupID) else { return }
        let directory = container.appendingPathComponent("KeyBoltClips", isDirectory: true)
        do {
            try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
            let destination = directory.appendingPathComponent("\(UUID().uuidString)-\(source.lastPathComponent)")
            try FileManager.default.copyItem(at: source, to: destination)
            clips.append(BoltClip(id: UUID().uuidString, folderID: selectedFolder, text: "",
                attachmentPath: destination.path, displayName: source.lastPathComponent,
                colorHex: cardColor, createdAt: Date().timeIntervalSince1970))
            BoltShared.saveClips(clips)
        } catch {
            // File import errors are intentionally kept local; no file is sent to a service.
        }
    }
}

struct LongTextEditorView: View {
    @State private var draft = ""
    var body: some View {
        NavigationStack {
            VStack(spacing: 14) {
                TextEditor(text: $draft)
                    .font(.body)
                    .scrollContentBackground(.hidden)
                    .padding(8)
                    .background(Color.white.opacity(0.06), in: RoundedRectangle(cornerRadius: 14))
                HStack {
                    Button { UIPasteboard.general.string = draft } label: { Label("Copy all", systemImage: "doc.on.doc") }
                        .buttonStyle(.borderedProminent)
                    Button(role: .destructive) { draft = "" } label: { Label("Clear", systemImage: "trash") }
                    Spacer()
                    Text("\(draft.count) chars").font(.caption).foregroundStyle(.secondary)
                }
            }
            .padding()
            .navigationTitle("Long-text editor")
        }
    }
}

struct PreferencesView: View {
    @State private var autocorrection = BoltShared.defaults.object(forKey: "bolt.autocorrect") as? Bool ?? true
    @State private var haptics = BoltShared.defaults.object(forKey: "bolt.haptics") as? Bool ?? true
    @State private var incognito = BoltShared.incognitoDefault()
    var body: some View {
        NavigationStack {
            Form {
                Section("Typing") {
                    Toggle("Autocorrection preference", isOn: $autocorrection)
                        .onChange(of: autocorrection) { BoltShared.defaults.set($0, forKey: "bolt.autocorrect") }
                    Toggle("Key haptics preference", isOn: $haptics)
                        .onChange(of: haptics) { BoltShared.defaults.set($0, forKey: "bolt.haptics") }
                    Text("These preferences are saved for the next keyboard-engine milestone; native iOS dictation remains controlled by iOS.").font(.footnote)
                        .foregroundStyle(.secondary)
                }
                Section("Privacy") {
                    Toggle("Start sessions in incognito", isOn: $incognito)
                        .onChange(of: incognito) { BoltShared.defaults.set($0, forKey: "bolt.incognitoDefault") }
                    Text("Key-Bolt stores saved snippets locally in the app group. This extension does not request full access and does not read the system pasteboard.").font(.footnote)
                        .foregroundStyle(.secondary)
                }
                Section("About") {
                    LabeledContent("Version", value: "1.0.0 · Foundation")
                    LabeledContent("Keyboard", value: "Key-Bolt")
                }
            }
            .navigationTitle("Settings")
        }
    }
}

private func isValidHex(_ value: String) -> Bool {
    let cleaned = value.trimmingCharacters(in: CharacterSet(charactersIn: "#"))
    return cleaned.count == 6 && UInt64(cleaned, radix: 16) != nil
}

private func normalizedHex(_ value: String) -> String {
    let cleaned = value.trimmingCharacters(in: CharacterSet(charactersIn: "#"))
    return "#" + (cleaned.count == 6 && UInt64(cleaned, radix: 16) != nil ? cleaned.uppercased() : "D6A642")
}
