package com.bolt.keybolt

import android.content.Context
import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ClipFolder(val id: String, val name: String, val color: Int)
data class SavedClip(
    val id: String, val folderId: String, val text: String, val uri: String,
    val displayName: String, val color: Int, val createdAt: Long
)

object KeyBoltPrefs {
    private const val FILE = "key_bolt_preferences"
    const val DEFAULT_GOLD = 0xFFD6A642.toInt()
    val presets = listOf(
        "Bolt Gold" to 0xFFD6A642.toInt(), "Midnight" to 0xFF9CA3AF.toInt(),
        "Electric Blue" to 0xFF42A5F5.toInt(), "Emerald" to 0xFF4CAF79.toInt(),
        "Rose" to 0xFFE58AA7.toInt(), "Violet" to 0xFFAB8BEF.toInt()
    )
    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    fun accent(context: Context) = prefs(context).getInt("accent", DEFAULT_GOLD)
    fun setAccent(context: Context, color: Int) { prefs(context).edit().putInt("accent", color).apply() }
    fun layout(context: Context) = prefs(context).getString("layout", "QWERTY") ?: "QWERTY"
    fun setLayout(context: Context, value: String) { prefs(context).edit().putString("layout", value).apply() }
    fun activeFolder(context: Context) = prefs(context).getString("active_clip_folder", "personal") ?: "personal"
    fun setActiveFolder(context: Context, value: String) { prefs(context).edit().putString("active_clip_folder", value).apply() }
    fun mode(context: Context) = prefs(context).getString("mode", "FULL") ?: "FULL"
    fun setMode(context: Context, value: String) { prefs(context).edit().putString("mode", value).apply() }
    fun keyScale(context: Context) = prefs(context).getFloat("key_scale", 1f)
    fun setKeyScale(context: Context, value: Float) { prefs(context).edit().putFloat("key_scale", value.coerceIn(.8f, 1.2f)).apply() }
    fun autoCorrect(context: Context) = prefs(context).getBoolean("autocorrect", true)
    fun setAutoCorrect(context: Context, value: Boolean) { prefs(context).edit().putBoolean("autocorrect", value).apply() }
    fun voiceEnabled(context: Context) = prefs(context).getBoolean("voice", true)
    fun setVoiceEnabled(context: Context, value: Boolean) { prefs(context).edit().putBoolean("voice", value).apply() }
    fun appAutoCorrect(context: Context, packageName: String) =
        prefs(context).getBoolean("app_autocorrect_$packageName", autoCorrect(context))
    fun setAppAutoCorrect(context: Context, packageName: String, value: Boolean) {
        prefs(context).edit().putBoolean("app_autocorrect_$packageName", value).apply()
    }
    fun folders(context: Context): List<ClipFolder> {
        val raw = prefs(context).getString("clip_folders", null)
        if (raw.isNullOrBlank()) return listOf(ClipFolder("personal", "Personal", DEFAULT_GOLD))
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                ClipFolder(o.getString("id"), o.getString("name"), o.optInt("color", DEFAULT_GOLD))
            }.ifEmpty { listOf(ClipFolder("personal", "Personal", DEFAULT_GOLD)) }
        }.getOrDefault(listOf(ClipFolder("personal", "Personal", DEFAULT_GOLD)))
    }
    fun saveFolders(context: Context, folders: List<ClipFolder>) {
        val array = JSONArray()
        folders.forEach { array.put(JSONObject().put("id", it.id).put("name", it.name).put("color", it.color)) }
        prefs(context).edit().putString("clip_folders", array.toString()).apply()
    }
    fun clips(context: Context): List<SavedClip> {
        val raw = prefs(context).getString("saved_clips", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                SavedClip(o.getString("id"), o.optString("folderId", "personal"),
                    o.optString("text"), o.optString("uri"), o.optString("displayName"),
                    o.optInt("color", 0xFF252525.toInt()), o.optLong("createdAt", 0L))
            }.sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }
    fun saveClips(context: Context, clips: List<SavedClip>) {
        val array = JSONArray()
        clips.forEach { clip ->
            array.put(JSONObject().put("id", clip.id).put("folderId", clip.folderId)
                .put("text", clip.text).put("uri", clip.uri).put("displayName", clip.displayName)
                .put("color", clip.color).put("createdAt", clip.createdAt))
        }
        prefs(context).edit().putString("saved_clips", array.toString()).apply()
    }
    fun newFolder(name: String, color: Int = DEFAULT_GOLD) = ClipFolder(UUID.randomUUID().toString(), name.trim(), color)
    fun newClip(folderId: String, text: String = "", uri: String = "", displayName: String = "", color: Int = 0xFF252525.toInt()) =
        SavedClip(UUID.randomUUID().toString(), folderId, text, uri, displayName, color, System.currentTimeMillis())
    fun hex(color: Int): String = String.format("#%06X", 0xFFFFFF and color)
    fun parseHex(value: String): Int? = runCatching {
        val clean = value.trim().removePrefix("#")
        require(clean.length == 6)
        Color.rgb(clean.substring(0, 2).toInt(16), clean.substring(2, 4).toInt(16), clean.substring(4, 6).toInt(16))
    }.getOrNull()
}
