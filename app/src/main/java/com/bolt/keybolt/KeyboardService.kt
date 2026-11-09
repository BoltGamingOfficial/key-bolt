package com.bolt.keybolt

import android.content.ClipDescription
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputContentInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import java.lang.ref.WeakReference

class KeyboardService : InputMethodService() {
    private var keyboard: BoltKeyboardView? = null
    var incognito: Boolean = false
        private set
    private var secureField: Boolean = false

    override fun onCreate() {
        super.onCreate()
        current = WeakReference(this)
    }

    override fun onCreateInputView(): View {
        keyboard = BoltKeyboardView(this, this)
        return keyboard!!
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        val type = attribute?.inputType ?: 0
        val variation = type and android.text.InputType.TYPE_MASK_VARIATION
        val isPassword = variation == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        val noPersonalizedLearning = ((attribute?.imeOptions ?: 0) and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0
        secureField = isPassword || noPersonalizedLearning
        incognito = secureField
        keyboard?.refresh()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        keyboard?.refresh()
    }

    fun toggleIncognito() {
        if (secureField) return
        incognito = !incognito
        Toast.makeText(this, if (incognito) "Incognito keyboard on" else "Incognito keyboard off", Toast.LENGTH_SHORT).show()
        keyboard?.refresh()
    }

    fun insertText(text: String) { currentInputConnection?.commitText(text, 1) }

    fun handleKey(key: String) {
        val input = currentInputConnection ?: return
        when (key) {
            "⌫" -> {
                val selected = input.getSelectedText(0)
                if (!selected.isNullOrEmpty()) input.commitText("", 1) else input.deleteSurroundingText(1, 0)
            }
            "↵" -> {
                val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
                if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) input.performEditorAction(action)
                else input.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            }
            "⇧" -> keyboard?.toggleShift()
            "space" -> input.commitText(" ", 1)
            "123" -> keyboard?.toggleSymbols()
            "ABC" -> keyboard?.toggleSymbols()
            else -> {
                val value = if (keyboard?.consumeShift() == true) key.uppercase() else key.lowercase()
                input.commitText(value, 1)
            }
        }
    }

    fun launchVoiceInput() {
        if (!KeyBoltPrefs.voiceEnabled(this) || incognito) return
        runCatching {
            startActivity(android.content.Intent(this, VoiceInputActivity::class.java).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { Toast.makeText(this, "Speech recognition is unavailable", Toast.LENGTH_SHORT).show() }
    }

    fun commitImageOrDocument(uri: Uri, displayName: String) {
        if (incognito || Build.VERSION.SDK_INT < 25) return
        val mime = contentResolver.getType(uri) ?: "application/octet-stream"
        val editor = currentInputEditorInfo
        val supported = editor?.contentMimeTypes ?: emptyArray()
        val mimeAccepted = supported.any { it == "*/*" || it == mime || (it.endsWith("/*") && mime.startsWith(it.removeSuffix("*"))) }
        if (!mimeAccepted) {
            Toast.makeText(this, "The current app does not accept this attachment type", Toast.LENGTH_SHORT).show()
            return
        }
        val info = InputContentInfo(uri, ClipDescription(displayName, arrayOf(mime)), null)
        val connection = currentInputConnection ?: return
        val accepted = runCatching {
            connection.commitContent(info, InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION, Bundle())
        }.getOrDefault(false)
        if (!accepted) Toast.makeText(this, "The current app declined this attachment", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private var current: WeakReference<KeyboardService>? = null
        fun commitVoiceResult(text: String) { current?.get()?.insertText(text) }
    }
}
