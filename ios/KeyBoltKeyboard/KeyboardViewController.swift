import UIKit

final class KeyboardViewController: UIInputViewController {
    private let mainStack = UIStackView()
    private var shifted = true
    private var symbols = false
    private var showingClips = false
    private var showingEmoji = false
    private var incognito = BoltShared.defaults.bool(forKey: "bolt.incognitoDefault")
    private var keyRows: [String] {
        if symbols {
            return ["1234567890", "@#$%&-+()", "*/:;!?'"]
        }
        switch BoltShared.layout() {
        case "AZERTY": return ["azertyuiop", "qsdfghjklm", "wxcvbn"]
        case "QWERTZ": return ["qwertzuiop", "asdfghjkl", "yxcvbnm"]
        case "DVORAK": return ["pyfgcrlaoe", "uidhtns", "qjkxbmwvz"]
        case "COLEMAK": return ["qwfpgjluy;", "arstdhneio", "zxcvbkm"]
        case "ALPHABETICAL": return ["abcdefghij", "klmnopqrst", "uvwxyz"]
        default: return ["qwertyuiop", "asdfghjkl", "zxcvbnm"]
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(white: 0.055, alpha: 1)
        let storedScale = BoltShared.defaults.object(forKey: "bolt.scale") as? Double ?? 1.0
        let scale = CGFloat(min(max(storedScale, 0.8), 1.2))
        view.heightAnchor.constraint(equalToConstant: 264 * scale).isActive = true
        buildKeyboard()
    }

    override func textWillChange(_ textInput: UITextInput?) {
        super.textWillChange(textInput)
        if textDocumentProxy.keyboardAppearance == .dark { view.backgroundColor = UIColor(white: 0.055, alpha: 1) }
    }

    private var accent: UIColor { UIColor(hex: BoltShared.accentHex()) }

    private func buildKeyboard() {
        view.subviews.forEach { $0.removeFromSuperview() }
        mainStack.removeFromSuperview()
        mainStack.axis = .vertical
        mainStack.spacing = 7
        mainStack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(mainStack)
        let mode = BoltShared.mode()
        let widthFactor: CGFloat
        switch mode {
        case "ONE_HAND_LEFT", "ONE_HAND_RIGHT": widthFactor = 0.82
        case "THUMB": widthFactor = 0.84
        case "COMPACT": widthFactor = 0.92
        default: widthFactor = 1.0
        }
        NSLayoutConstraint.activate([
            mainStack.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: widthFactor),
            mainStack.topAnchor.constraint(equalTo: view.topAnchor, constant: 5),
            mainStack.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -5)
        ])
        if mode == "ONE_HAND_LEFT" {
            mainStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 4).isActive = true
        } else if mode == "ONE_HAND_RIGHT" {
            mainStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -4).isActive = true
        } else {
            mainStack.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        }
        mainStack.addArrangedSubview(makeToolbar())
        if showingClips { buildClipPanel() }
        else if showingEmoji { buildEmojiPanel() }
        else { buildKeyRows() }
    }

    private func makeToolbar() -> UIStackView {
        let toolbar = UIStackView()
        toolbar.axis = .horizontal
        toolbar.distribution = .fillEqually
        toolbar.spacing = 4
        let items: [(String, () -> Void)] = [
            (symbols ? "ABC" : "123", { [weak self] in self?.symbols.toggle(); self?.showingClips = false; self?.showingEmoji = false; self?.buildKeyboard() }),
            ("🌐", { [weak self] in self?.advanceToNextInputMode() }),
            ("📋", { [weak self] in self?.showingClips.toggle(); self?.showingEmoji = false; self?.buildKeyboard() }),
            ("☺", { [weak self] in self?.showingEmoji.toggle(); self?.showingClips = false; self?.buildKeyboard() }),
            (incognito ? "◉" : "○", { [weak self] in self?.incognito.toggle(); self?.showingClips = false; self?.buildKeyboard() })
        ]
        items.forEach { title, action in
            let button = UIButton(type: .system)
            button.setTitle(title, for: .normal)
            button.titleLabel?.font = .systemFont(ofSize: 16, weight: .semibold)
            button.tintColor = accent
            button.backgroundColor = UIColor(white: 0.12, alpha: 1)
            button.layer.cornerRadius = 7
            button.addAction(UIAction { _ in action() }, for: .touchUpInside)
            toolbar.addArrangedSubview(button)
        }
        return toolbar
    }

    private func buildKeyRows() {
        keyRows.enumerated().forEach { index, letters in
            let row = UIStackView()
            row.axis = .horizontal
            row.distribution = .fillEqually
            row.spacing = 4
            if index == 2 {
                row.addArrangedSubview(makeKey("⇧", special: true) { [weak self] in self?.shifted.toggle(); self?.buildKeyboard() })
            }
            for character in letters {
                let displayed = shifted ? String(character).uppercased() : String(character)
                row.addArrangedSubview(makeKey(displayed) { [weak self] in
                    guard let self else { return }
                    self.textDocumentProxy.insertText(displayed)
                    if self.shifted { self.shifted = false; self.buildKeyboard() }
                })
            }
            if index == 2 {
                row.addArrangedSubview(makeKey("⌫", special: true) { [weak self] in self?.textDocumentProxy.deleteBackward() })
            }
            mainStack.addArrangedSubview(row)
        }

        let bottom = UIStackView()
        bottom.axis = .horizontal
        bottom.spacing = 4
        bottom.distribution = .fillProportionally
        bottom.addArrangedSubview(makeKey(symbols ? "ABC" : "123", special: true) { [weak self] in
            self?.symbols.toggle(); self?.buildKeyboard()
        })
        bottom.addArrangedSubview(makeKey("☺", special: true) { [weak self] in
            self?.showingEmoji = true; self?.buildKeyboard()
        })
        bottom.addArrangedSubview(makeKey(",", special: true) { [weak self] in self?.textDocumentProxy.insertText(",") })
        bottom.addArrangedSubview(makeKey("English", special: false) { [weak self] in self?.textDocumentProxy.insertText(" ") })
        bottom.addArrangedSubview(makeKey(".", special: true) { [weak self] in self?.textDocumentProxy.insertText(".") })
        bottom.addArrangedSubview(makeKey("↵", special: true) { [weak self] in self?.textDocumentProxy.insertText("\n") })
        mainStack.addArrangedSubview(bottom)
    }

    private func buildClipPanel() {
        guard !incognito else {
            let label = UILabel()
            label.text = "Clipboard hidden in incognito"
            label.textColor = .white
            label.textAlignment = .center
            mainStack.addArrangedSubview(label)
            return
        }
        let activeFolder = BoltShared.defaults.string(forKey: "bolt.activeFolder") ?? "personal"
        let clips = BoltShared.clips().filter { $0.folderID == activeFolder && !$0.text.isEmpty }.prefix(5)
        if clips.isEmpty {
            let label = UILabel()
            label.text = "Save text clips in the Key-Bolt app"
            label.textColor = .lightGray
            label.textAlignment = .center
            mainStack.addArrangedSubview(label)
        }
        clips.forEach { clip in
            let button = UIButton(type: .system)
            button.setTitle(String(clip.text.prefix(90)), for: .normal)
            button.setTitleColor(.white, for: .normal)
            button.titleLabel?.font = .systemFont(ofSize: 13)
            button.contentHorizontalAlignment = .leading
            button.titleLabel?.numberOfLines = 2
            button.backgroundColor = UIColor(hex: clip.colorHex)
            button.layer.cornerRadius = 7
            button.contentEdgeInsets = UIEdgeInsets(top: 5, left: 9, bottom: 5, right: 9)
            button.addAction(UIAction { [weak self] _ in
                self?.textDocumentProxy.insertText(clip.text)
            }, for: .touchUpInside)
            mainStack.addArrangedSubview(button)
        }
    }

    private func buildEmojiPanel() {
        let emojis = ["😀","😂","🥹","😍","😎","🤔","😭","🔥","✨","❤️","🫶","👏","👍","🙏","🎉","⚡","💛","🖤","🌍","🚀"]
        for start in stride(from: 0, to: emojis.count, by: 5) {
            let row = UIStackView()
            row.axis = .horizontal
            row.distribution = .fillEqually
            row.spacing = 4
            for emoji in emojis[start..<min(start + 5, emojis.count)] {
                row.addArrangedSubview(makeKey(emoji, special: false) { [weak self] in self?.textDocumentProxy.insertText(emoji) })
            }
            mainStack.addArrangedSubview(row)
        }
    }

    private func makeKey(_ title: String, special: Bool, action: @escaping () -> Void) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.setTitleColor(UIColor(white: 0.08, alpha: 1), for: .normal)
        button.titleLabel?.font = UIFontMetrics.default.scaledFont(for: .systemFont(ofSize: title == "English" ? 10 : 17, weight: .medium))
        button.titleLabel?.adjustsFontForContentSizeCategory = true
        button.backgroundColor = special ? accent.withAlphaComponent(0.76) : accent
        button.layer.cornerRadius = 6
        button.titleLabel?.numberOfLines = 2
        if !special && title.count == 1 {
            button.setAttributedTitle(NSAttributedString(string: "\(title)\n⚡", attributes: [
                .font: UIFont.systemFont(ofSize: 17, weight: .medium),
                .foregroundColor: UIColor(white: 0.08, alpha: 1)
            ]), for: .normal)
        }
        button.addAction(UIAction { _ in action() }, for: .touchUpInside)
        return button
    }
}

private extension UIColor {
    convenience init(hex: String) {
        let cleaned = hex.trimmingCharacters(in: CharacterSet(charactersIn: "#"))
        let value = UInt64(cleaned, radix: 16) ?? 0xD6A642
        self.init(
            red: CGFloat((value >> 16) & 0xFF) / 255,
            green: CGFloat((value >> 8) & 0xFF) / 255,
            blue: CGFloat(value & 0xFF) / 255,
            alpha: 1
        )
    }
}
