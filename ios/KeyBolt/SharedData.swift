import Foundation
import SwiftUI

struct BoltFolder: Identifiable, Codable, Equatable {
    var id: String
    var name: String
    var colorHex: String
}
struct BoltClip: Identifiable, Codable, Equatable {
    var id: String
    var folderID: String
    var text: String
    var attachmentPath: String
    var displayName: String
    var colorHex: String
    var createdAt: Double
}
enum BoltShared {
    static let groupID = "group.com.bolt.keybolt"
    static let defaults = UserDefaults(suiteName: groupID) ?? .standard
    static let gold = "#D6A642"
    static let folderKey = "bolt.folders"
    static let clipKey = "bolt.clips"
    static func folders() -> [BoltFolder] {
        guard let data = defaults.data(forKey: folderKey),
              let value = try? JSONDecoder().decode([BoltFolder].self, from: data), !value.isEmpty else {
            return [BoltFolder(id: "personal", name: "Personal", colorHex: gold)]
        }
        return value
    }
    static func saveFolders(_ value: [BoltFolder]) {
        if let data = try? JSONEncoder().encode(value) { defaults.set(data, forKey: folderKey) }
    }
    static func clips() -> [BoltClip] {
        guard let data = defaults.data(forKey: clipKey),
              let value = try? JSONDecoder().decode([BoltClip].self, from: data) else { return [] }
        return value.sorted { $0.createdAt > $1.createdAt }
    }
    static func saveClips(_ value: [BoltClip]) {
        if let data = try? JSONEncoder().encode(value) { defaults.set(data, forKey: clipKey) }
    }
    static func accentHex() -> String { defaults.string(forKey: "bolt.accent") ?? gold }
    static func setAccentHex(_ value: String) { defaults.set(value, forKey: "bolt.accent") }
    static func layout() -> String { defaults.string(forKey: "bolt.layout") ?? "QWERTY" }
    static func setLayout(_ value: String) { defaults.set(value, forKey: "bolt.layout") }
    static func mode() -> String { defaults.string(forKey: "bolt.mode") ?? "FULL" }
    static func setMode(_ value: String) { defaults.set(value, forKey: "bolt.mode") }
    static func incognitoDefault() -> Bool { defaults.bool(forKey: "bolt.incognitoDefault") }
}
extension Color {
    init(hex: String) {
        let value = hex.trimmingCharacters(in: CharacterSet(charactersIn: "#"))
        let number = UInt64(value, radix: 16) ?? 0xD6A642
        self.init(.sRGB, red: Double((number >> 16) & 0xFF) / 255,
                  green: Double((number >> 8) & 0xFF) / 255,
                  blue: Double(number & 0xFF) / 255, opacity: 1)
    }
}
