package com.bolt.keybolt

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import android.graphics.Color as AndroidColor

private val Ink = Color(0xFF111111)
private val CardSurface = Color(0xFF202020)
private val Gold = Color(0xFFD6A642)
private val WarmWhite = Color(0xFFF4F0E5)
private val Layouts = listOf("QWERTY", "AZERTY", "QWERTZ", "DVORAK", "COLEMAK", "ALPHABETICAL")
private val Modes = listOf("FULL", "ONE_HAND_LEFT", "ONE_HAND_RIGHT", "THUMB", "COMPACT")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KeyBoltApp() }
    }
}

@Composable
private fun KeyBoltApp() {
    var page by remember { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Gold, secondary = Gold, background = Ink, surface = CardSurface,
        onPrimary = Ink, onBackground = WarmWhite, onSurface = WarmWhite
    )) {
        Scaffold(
            containerColor = Ink,
            bottomBar = {
                NavigationBar(containerColor = CardSurface, contentColor = WarmWhite) {
                    listOf("Keyboard", "Theme", "Clipboard", "Editor", "Settings").forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = page == index,
                            onClick = { page = index },
                            icon = {
                                Icon(when (index) {
                                    0 -> Icons.Default.Keyboard
                                    1 -> Icons.Default.Palette
                                    2 -> Icons.Default.ContentPaste
                                    3 -> Icons.Default.Edit
                                    else -> Icons.Default.Settings
                                }, contentDescription = title)
                            },
                            label = { Text(title) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", color = Gold, fontSize = 30.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("KEY-BOLT", color = WarmWhite, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("Your keyboard. Your rules.", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
                when (page) {
                    0 -> KeyboardSettings()
                    1 -> ThemeSettings()
                    2 -> ClipboardManagerScreen()
                    3 -> EditorScreen()
                    else -> AdvancedSettings()
                }
            }
        }
    }
}

@Composable
private fun PageColumn(content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) { item { Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content) } }
}

@Composable
private fun SectionCard(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardSurface), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = WarmWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = Color.LightGray, fontSize = 12.sp)
            content()
        }
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun KeyboardSettings() {
    val context = LocalContext.current
    var layout by remember { mutableStateOf(KeyBoltPrefs.layout(context)) }
    var mode by remember { mutableStateOf(KeyBoltPrefs.mode(context)) }
    var scale by remember { mutableFloatStateOf(KeyBoltPrefs.keyScale(context)) }
    val enabled = remember { mutableStateOf(isKeyboardEnabled(context)) }
    PageColumn {
        SectionCard("Get started", "Enable Key-Bolt, then choose it as your active keyboard.") {
            Text(if (enabled.value) "✓ Keyboard enabled in Android settings" else "Keyboard not enabled yet", color = if (enabled.value) Gold else Color(0xFFFFB86B))
            ActionButton("Open keyboard settings") {
                context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            ActionButton("Choose Key-Bolt keyboard") {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
            }
        }
        SectionCard("Keyboard layout", "Choose the key arrangement. Composition dictionaries remain supplied by the operating system.") {
            Layouts.forEach { item ->
                Row(Modifier.fillMaxWidth().clickable {
                    layout = item
                    KeyBoltPrefs.setLayout(context, item)
                }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = layout == item, onClick = {
                        layout = item
                        KeyBoltPrefs.setLayout(context, item)
                    }, colors = RadioButtonDefaults.colors(selectedColor = Gold))
                    Text(item, color = WarmWhite)
                }
            }
        }
        SectionCard("Keyboard shape", "Adjust the footprint for your grip.") {
            Modes.forEach { item ->
                Row(Modifier.fillMaxWidth().clickable {
                    mode = item
                    KeyBoltPrefs.setMode(context, item)
                }.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = mode == item, onClick = {
                        mode = item
                        KeyBoltPrefs.setMode(context, item)
                    }, colors = RadioButtonDefaults.colors(selectedColor = Gold))
                    Text(item.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, color = WarmWhite)
                }
            }
            Text("Keyboard size · \${(scale * 100).toInt()}%", color = WarmWhite)
            Slider(value = scale, onValueChange = {
                scale = it
                KeyBoltPrefs.setKeyScale(context, it)
            }, valueRange = .8f..1.2f, colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold))
        }
    }
}

@Composable
private fun ThemeSettings() {
    val context = LocalContext.current
    var accent by remember { mutableIntStateOf(KeyBoltPrefs.accent(context)) }
    var red by remember { mutableFloatStateOf(AndroidColor.red(accent).toFloat()) }
    var green by remember { mutableFloatStateOf(AndroidColor.green(accent).toFloat()) }
    var blue by remember { mutableFloatStateOf(AndroidColor.blue(accent).toFloat()) }
    var hex by remember { mutableStateOf(KeyBoltPrefs.hex(accent)) }

    fun applyColor(r: Float, g: Float, b: Float) {
        accent = AndroidColor.rgb(r.toInt(), g.toInt(), b.toInt())
        KeyBoltPrefs.setAccent(context, accent)
        hex = KeyBoltPrefs.hex(accent)
    }
    PageColumn {
        SectionCard("Keyboard appearance", "Bolt Gold is the default. Pick a preset or create an exact color.") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).background(Color(accent), RoundedCornerShape(14.dp)))
                Column {
                    Text("Live accent preview", color = WarmWhite)
                    Text(hex, color = Color.LightGray, fontSize = 12.sp)
                }
            }
            KeyBoltPrefs.presets.forEach { (name, color) ->
                Row(Modifier.fillMaxWidth().clickable {
                    accent = color
                    red = AndroidColor.red(color).toFloat()
                    green = AndroidColor.green(color).toFloat()
                    blue = AndroidColor.blue(color).toFloat()
                    KeyBoltPrefs.setAccent(context, color)
                    hex = KeyBoltPrefs.hex(color)
                }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(24.dp).background(Color(color), RoundedCornerShape(7.dp)))
                    Spacer(Modifier.width(10.dp))
                    Text(name, color = WarmWhite)
                    Spacer(Modifier.weight(1f))
                    RadioButton(accent == color, onClick = {
                        accent = color
                        red = AndroidColor.red(color).toFloat()
                        green = AndroidColor.green(color).toFloat()
                        blue = AndroidColor.blue(color).toFloat()
                        KeyBoltPrefs.setAccent(context, color)
                        hex = KeyBoltPrefs.hex(color)
                    }, colors = RadioButtonDefaults.colors(selectedColor = Gold))
                }
            }
            Text("Red · \${red.toInt()}", color = WarmWhite)
            Slider(red, { red = it; applyColor(red, green, blue) }, valueRange = 0f..255f)
            Text("Green · \${green.toInt()}", color = WarmWhite)
            Slider(green, { green = it; applyColor(red, green, blue) }, valueRange = 0f..255f)
            Text("Blue · \${blue.toInt()}", color = WarmWhite)
            Slider(blue, { blue = it; applyColor(red, green, blue) }, valueRange = 0f..255f)
            OutlinedTextField(
                value = hex, onValueChange = { candidate ->
                    hex = candidate
                    KeyBoltPrefs.parseHex(candidate)?.let { color ->
                        accent = color
                        red = AndroidColor.red(color).toFloat()
                        green = AndroidColor.green(color).toFloat()
                        blue = AndroidColor.blue(color).toFloat()
                        KeyBoltPrefs.setAccent(context, color)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("HEX color · e.g. #D6A642") },
                singleLine = true
            )
            Text("RGB \${red.toInt()}, \${green.toInt()}, \${blue.toInt()}", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AdvancedSettings() {
    val context = LocalContext.current
    var autoCorrect by remember { mutableStateOf(KeyBoltPrefs.autoCorrect(context)) }
    var voice by remember { mutableStateOf(KeyBoltPrefs.voiceEnabled(context)) }
    var packageName by remember { mutableStateOf("") }
    var appAuto by remember { mutableStateOf(true) }
    PageColumn {
        SectionCard("Language support", "Key-Bolt declares language subtypes to Android. Switch using the globe key and install the language input methods you need in device settings.") {
            Text("English · French · German · Italian · Spanish · Portuguese · Chinese · Japanese · Korean · Arabic · Hindi · Russian · Turkish · Indonesian · Vietnamese", color = Color.LightGray, fontSize = 13.sp)
            Text("Script display works through system fonts. Full Pinyin, Kana/Kanji and Indic composition engines are a separate roadmap item.", color = Color.LightGray, fontSize = 12.sp)
        }
        SectionCard("Typing preferences") {
            SettingSwitch("Autocorrection preference", autoCorrect) {
                autoCorrect = it
                KeyBoltPrefs.setAutoCorrect(context, it)
            }
            SettingSwitch("Voice dictation", voice) {
                voice = it
                KeyBoltPrefs.setVoiceEnabled(context, it)
            }
            Text("The current keyboard baseline stores the autocorrection preference but does not yet implement its own language-aware suggestion engine.", color = Color.LightGray, fontSize = 12.sp)
        }
        SectionCard("Per-app typing preference", "Enter the Android package name to save a separate autocorrection preference.") {
            OutlinedTextField(packageName, { packageName = it }, label = { Text("App package name") }, singleLine = true)
            SettingSwitch("Autocorrection for this app", appAuto) {
                appAuto = it
                if (packageName.isNotBlank()) KeyBoltPrefs.setAppAutoCorrect(context, packageName.trim(), it)
            }
            ActionButton("Load saved app preference", onClick = {
                if (packageName.isNotBlank()) appAuto = KeyBoltPrefs.appAutoCorrect(context, packageName.trim())
            })
        }
        SectionCard("Privacy and incognito") {
            Text("Typed text is not sent by Key-Bolt. The keyboard saves no typing history. Clipboard items are saved only when you add them in the Clipboard tab. Incognito hides the saved-clip panel for the active session.", color = Color.LightGray, fontSize = 13.sp)
            Text("Android password fields automatically open the keyboard with incognito enabled. Host apps and the operating system remain outside Key-Bolt's control.", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = WarmWhite, modifier = Modifier.weight(1f))
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedThumbColor = Ink, checkedTrackColor = Gold))
    }
}

@Composable
private fun ClipboardManagerScreen() {
    val context = LocalContext.current
    var folders by remember { mutableStateOf(KeyBoltPrefs.folders(context)) }
    var clips by remember { mutableStateOf(KeyBoltPrefs.clips(context)) }
    var selectedFolder by remember { mutableStateOf(KeyBoltPrefs.activeFolder(context).takeIf { id -> folders.any { it.id == id } } ?: folders.first().id) }
    var clipText by remember { mutableStateOf("") }
    var newFolderName by remember { mutableStateOf("") }
    var showFolderDialog by remember { mutableStateOf(false) }
    var activeColor by remember { mutableIntStateOf(0xFF252525.toInt()) }
    val colors = listOf(0xFF252525.toInt(), 0xFF3C2F1A.toInt(), 0xFF233B34.toInt(), 0xFF25374A.toInt(), 0xFF472C3B.toInt(), 0xFF3B3154.toInt())
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            val name = queryDisplayName(context, it)
            clips = clips + KeyBoltPrefs.newClip(selectedFolder, uri = it.toString(), displayName = name, color = activeColor)
            KeyBoltPrefs.saveClips(context, clips)
        }
    }
    PageColumn {
        SectionCard("Your clip collections", "Create named spaces for snippets, photos and documents.") {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                folders.forEach { folder ->
                    FilterChip(
                        selected = selectedFolder == folder.id,
                        onClick = { selectedFolder = folder.id; KeyBoltPrefs.setActiveFolder(context, folder.id) },
                        label = { Text(folder.name) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Gold, selectedLabelColor = Ink)
                    )
                }
                IconButton(onClick = { showFolderDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Add collection", tint = Gold)
                }
            }
            OutlinedTextField(clipText, { clipText = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Paste or type a snippet") }, minLines = 3)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Save text") {
                    if (clipText.isNotBlank()) {
                        clips = clips + KeyBoltPrefs.newClip(selectedFolder, text = clipText, color = activeColor)
                        KeyBoltPrefs.saveClips(context, clips)
                        clipText = ""
                    }
                }
                OutlinedButton(onClick = { filePicker.launch(arrayOf("image/*", "application/pdf", "text/*", "application/*")) }) {
                    Text("Add photo / file", color = Gold)
                }
            }
            Text("Card background", color = WarmWhite, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { color ->
                    Box(Modifier.size(30.dp).background(Color(color), RoundedCornerShape(8.dp)).clickable { activeColor = color })
                }
            }
        }
        SectionCard("Saved items · \${clips.count { it.folderId == selectedFolder }}") {
            val visible = clips.filter { it.folderId == selectedFolder }
            if (visible.isEmpty()) Text("Nothing saved in this collection yet.", color = Color.LightGray)
            visible.forEach { clip ->
                Row(
                    Modifier.fillMaxWidth().background(Color(clip.color), RoundedCornerShape(10.dp)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(if (clip.uri.isBlank()) clip.text.take(180) else "📎 \${clip.displayName}", color = WarmWhite, maxLines = 4)
                        Text(if (clip.uri.isBlank()) "Text snippet" else "Saved attachment reference", color = Color.LightGray, fontSize = 11.sp)
                    }
                    IconButton(onClick = {
                        clips = clips.filterNot { it.id == clip.id }
                        KeyBoltPrefs.saveClips(context, clips)
                    }) { Icon(Icons.Default.Delete, contentDescription = "Delete clip", tint = Color.LightGray) }
                }
            }
        }
    }
    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text("New collection") },
            text = { OutlinedTextField(newFolderName, { newFolderName = it }, label = { Text("Collection name") }) },
            confirmButton = {
                TextButton(onClick = {
                    if (newFolderName.isNotBlank()) {
                        val folder = KeyBoltPrefs.newFolder(newFolderName)
                        folders = folders + folder
                        KeyBoltPrefs.saveFolders(context, folders)
                        selectedFolder = folder.id
                        KeyBoltPrefs.setActiveFolder(context, folder.id)
                        newFolderName = ""
                    }
                    showFolderDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showFolderDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun EditorScreen() {
    val clipboard = LocalClipboardManager.current
    var text by remember { mutableStateOf("") }
    PageColumn {
        SectionCard("Long-text editor", "Draft, edit and copy long passages without relying on a destination app's text field.") {
            OutlinedTextField(
                value = text, onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp),
                placeholder = { Text("Write or paste a long text…") },
                minLines = 10
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Copy all text") { clipboard.setText(AnnotatedString(text)) }
                OutlinedButton(onClick = { text = "" }) { Text("Clear", color = Gold) }
            }
            Text("\${text.length} characters · \${text.split(Regex("\\s+")).count { it.isNotBlank() }} words", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String {
    var name = uri.lastPathSegment?.substringAfterLast('/') ?: "Attachment"
    var cursor: Cursor? = null
    try {
        cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        if (cursor?.moveToFirst() == true) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) name = cursor.getString(index) ?: name
        }
    } finally {
        cursor?.close()
    }
    return name
}

private fun isKeyboardEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_INPUT_METHODS) ?: return false
    return enabled.contains("\${context.packageName}/.KeyboardService") || enabled.contains("com.bolt.keybolt/.KeyboardService")
}
