package com.bolt.keybolt

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import java.util.Locale

class VoiceInputActivity : Activity() {
    companion object { private const val REQUEST_SPEECH = 7104 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to type with Key-Bolt")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        try {
            startActivityForResult(intent, REQUEST_SPEECH)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No speech recognition service is installed", Toast.LENGTH_LONG).show()
            finish()
        } catch (_: SecurityException) {
            Toast.makeText(this, "Speech recognition is blocked by device permissions", Toast.LENGTH_LONG).show()
            finish()
        } catch (_: Exception) {
            Toast.makeText(this, "Could not start voice typing. Try again.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @Deprecated("Recognizer intent returns results through the activity result contract.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_SPEECH) return
        if (resultCode == RESULT_OK) {
            val recognizedText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull { it.isNotBlank() }
            if (recognizedText.isNullOrBlank()) {
                Toast.makeText(this, "No speech was recognized", Toast.LENGTH_SHORT).show()
            } else if (!KeyboardService.commitVoiceResult(recognizedText)) {
                Toast.makeText(this, "Voice text could not be returned to the keyboard. Tap the microphone and try again.", Toast.LENGTH_LONG).show()
            }
        }
        finish()
    }
}
