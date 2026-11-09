import Foundation
struct BoltFolder: Identifiable, Codable {
    var id: String
    var name: String
    var colorHex: String
}
struct BoltClip: Identifiable, Codable {
    var id: String
    var folderID: String
    var text: String
    var attachmentPath: String
    var displayName: String
    var colorHex: String
    var createdAt: Double
}
enum BoltShared {
    static let defaults = UserDefaults(suiteName: "group.com.bolt.keybolt") ?? .standard
    static func clips() -> [BoltClip] {
        guard let data = defaults.data(forKey: "bolt.clips"),
              let value = try? JSONDecoder().decode([BoltClip].self, from: data) else { return [] }
        return value.sorted { $0.createdAt > $1.createdAt }
    }
    static func accentHex() -> String { defaults.string(forKey: "bolt.accent") ?? "#D6A642" }
    static func layout() -> String { defaults.string(forKey: "bolt.layout") ?? "QWERTY" }
    static func mode() -> String { defaults.string(forKey: "bolt.mode") ?? "FULL" }
}
