package com.bolt.keybolt

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View

class BoltKeyboardView(context: android.content.Context, private val service: KeyboardService) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var shifted = true
    private var symbols = false
    private var panel = Panel.KEYS
    private val density = resources.displayMetrics.density
    private val gold get() = KeyBoltPrefs.accent(context)
    private val ink = 0xFF171717.toInt()
    private enum class Panel { KEYS, CLIPS, EMOJI }
    private fun dp(value: Float) = value * density
    private fun sp(value: Float) = value * resources.displayMetrics.scaledDensity

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = (dp(274f) * KeyBoltPrefs.keyScale(context)).toInt()
        setMeasuredDimension(resolveSize(suggestedMinimumWidth, widthMeasureSpec), resolveSize(desired, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(0xFF101010.toInt())
        drawToolbar(canvas)
        when (panel) {
            Panel.KEYS -> drawKeys(canvas)
            Panel.CLIPS -> drawClips(canvas)
            Panel.EMOJI -> drawEmoji(canvas)
        }
    }

    private fun drawToolbar(canvas: Canvas) {
        val h = dp(43f)
        paint.color = 0xFF1B1B1B.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), h, paint)
        val labels = listOf(if (symbols) "ABC" else "123", "🌐", "🎙", "📋", "☺", if (service.incognito) "◉" else "○")
        val cell = width / labels.size.toFloat()
        labels.forEachIndexed { index, label ->
            val active = (index == 3 && panel == Panel.CLIPS) || (index == 4 && panel == Panel.EMOJI) || (index == 5 && service.incognito)
            paint.color = if (active) gold else 0xFFBDBDBD.toInt()
            paint.textSize = sp(17f)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(label, cell * (index + .5f), h * .67f, paint)
            if (active) {
                paint.color = gold
                canvas.drawRoundRect(cell * index + dp(9f), h - dp(3f), cell * (index + 1) - dp(9f), h - dp(1f), dp(2f), dp(2f), paint)
            }
        }
    }

    private data class Key(val label: String, val weight: Float = 1f, val special: Boolean = false)

    private fun keyRows(): List<List<Key>> {
        if (symbols) return listOf(
            "1234567890".map { Key(it.toString()) },
            "@#\$%&-+()".map { Key(it.toString()) },
            listOf(Key("⇧", 1.4f, true)) + "*/:;!?'".map { Key(it.toString()) } + listOf(Key("⌫", 1.4f, true)),
            listOf(Key("ABC", 1.4f, true), Key("☺", 1f, true), Key(",", .8f), Key("space", 4.2f, true), Key(".", .8f), Key("↵", 1.4f, true))
        )
        val layout = KeyBoltPrefs.layout(context)
        val first = when (layout) {
            "AZERTY" -> "azertyuiop"
            "QWERTZ" -> "qwertzuiop"
            "DVORAK" -> "pyfgcrlaoe"
            "COLEMAK" -> "qwfpgjluy;"
            "ALPHABETICAL" -> "abcdefghij"
            else -> "qwertyuiop"
        }
        val second = when (layout) {
            "AZERTY" -> "qsdfghjklm"
            "QWERTZ" -> "asdfghjkl"
            "DVORAK" -> "uidhtns"
            "COLEMAK" -> "arstdhneio"
            "ALPHABETICAL" -> "klmnopqrst"
            else -> "asdfghjkl"
        }
        val third = when (layout) {
            "AZERTY" -> "wxcvbn"
            "QWERTZ" -> "yxcvbnm"
            "DVORAK" -> ";qjkxbmwvz"
            "COLEMAK" -> "zxcvbkm"
            "ALPHABETICAL" -> "uvwxyz"
            else -> "zxcvbnm"
        }
        return listOf(
            first.map { Key(it.toString()) },
            second.map { Key(it.toString()) },
            listOf(Key("⇧", 1.4f, true)) + third.map { Key(it.toString()) } + listOf(Key("⌫", 1.4f, true)),
            listOf(Key("123", 1.35f, true), Key("☺", 1f, true), Key(",", .75f), Key("space", 4.5f, true), Key(".", .75f), Key("↵", 1.35f, true))
        )
    }

    private fun geometry(): Triple<Float, Float, Float> {
        val mode = KeyBoltPrefs.mode(context)
        val scale = KeyBoltPrefs.keyScale(context)
        val widthFactor = when (mode) {
            "ONE_HAND_LEFT", "ONE_HAND_RIGHT", "THUMB" -> .78f
            "COMPACT" -> .9f
            else -> 1f
        }
        val maxWidth = width * widthFactor * scale.coerceIn(.8f, 1.2f)
        val startX = when (mode) {
            "ONE_HAND_RIGHT" -> width - maxWidth
            "ONE_HAND_LEFT" -> 0f
            else -> (width - maxWidth) / 2f
        }
        return Triple(startX, maxWidth, scale)
    }

    private fun drawKeys(canvas: Canvas) {
        val top = dp(48f)
        val bottom = height.toFloat() - dp(5f)
        val rows = keyRows()
        val gap = dp(5f)
        val rowHeight = (bottom - top - gap * (rows.size - 1)) / rows.size
        val (startX, maxWidth, scale) = geometry()
        rows.forEachIndexed { rowIndex, row ->
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val rowWidth = maxWidth - dp(6f) * (row.size - 1)
            var x = startX + dp(3f)
            val y = top + rowIndex * (rowHeight + gap)
            row.forEach { key ->
                val keyWidth = rowWidth * key.weight / totalWeight
                val rect = RectF(x, y, x + keyWidth, y + rowHeight)
                paint.color = if (key.special) 0xFF9B782E.toInt() else gold
                canvas.drawRoundRect(rect, dp(7f), dp(7f), paint)
                paint.color = ink
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = if (key.label == "space") sp(10f) else sp(19f) * scale.coerceIn(.9f, 1.1f)
                val label = when (key.label) {
                    "space" -> "English"
                    else -> if (shifted) key.label.uppercase() else key.label.lowercase()
                }
                canvas.drawText(label, rect.centerX(), rect.centerY() + dp(6f), paint)
                if (!key.special && key.label.length == 1) drawBolt(canvas, rect.centerX(), rect.bottom - dp(5f), dp(3.5f))
                x += keyWidth + dp(6f)
            }
        }
    }

    private fun drawBolt(canvas: Canvas, x: Float, y: Float, size: Float) {
        val path = Path().apply {
            moveTo(x + size * .25f, y - size)
            lineTo(x - size * .15f, y - size * .12f)
            lineTo(x + size * .1f, y - size * .12f)
            lineTo(x - size * .22f, y + size)
            lineTo(x + size * .12f, y + size * .16f)
            lineTo(x - size * .03f, y + size * .16f)
            close()
        }
        paint.color = 0xFF161616.toInt()
        canvas.drawPath(path, paint)
    }

    private fun drawClips(canvas: Canvas) {
        if (service.incognito) {
            paint.color = 0xFFBDBDBD.toInt()
            paint.textSize = sp(14f)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Clipboard hidden in incognito", width / 2f, height / 2f, paint)
            return
        }
        val activeFolder = KeyBoltPrefs.activeFolder(context)
        val clips = KeyBoltPrefs.clips(context).filter { it.folderId == activeFolder }.take(5)
        clips.forEachIndexed { index, clip ->
            val y = dp(69f) + index * dp(37f)
            paint.color = clip.color
            canvas.drawRoundRect(dp(8f), y - dp(19f), width - dp(8f), y + dp(9f), dp(7f), dp(7f), paint)
            paint.color = 0xFFF4F0E5.toInt()
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = sp(12f)
            val preview = if (clip.uri.isBlank()) clip.text else "📎 \${clip.displayName}"
            canvas.drawText(preview.take(72), dp(17f), y, paint)
        }
        if (clips.isEmpty()) {
            paint.color = 0xFFBDBDBD.toInt()
            paint.textSize = sp(14f)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Save clips in the Key-Bolt app", width / 2f, height / 2f, paint)
        }
    }

    private fun drawEmoji(canvas: Canvas) {
        val emojis = emojiList()
        val cell = width / 5f
        emojis.forEachIndexed { index, emoji ->
            paint.textSize = sp(25f)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(emoji, (index % 5 + .5f) * cell, dp(79f) + (index / 5) * dp(43f), paint)
        }
    }

    private fun emojiList() = listOf("😀","😂","🥹","😍","😎","🤔","😭","🔥","✨","❤️","🫶","👏","👍","🙏","🎉","⚡","💛","🖤","🌍","🚀")
    fun toggleShift() { shifted = !shifted; invalidate() }
    fun consumeShift(): Boolean { val was = shifted; shifted = false; invalidate(); return was }
    fun toggleSymbols() { symbols = !symbols; panel = Panel.KEYS; invalidate() }
    fun refresh() { if (service.incognito) panel = Panel.KEYS; requestLayout(); invalidate() }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val x = event.x
        val y = event.y
        val toolbar = dp(43f)
        if (y <= toolbar) {
            val slot = (x / (width / 6f)).toInt().coerceIn(0, 5)
            when (slot) {
                0 -> toggleSymbols()
                1 -> service.switchToNextInputMethod(false)
                2 -> service.launchVoiceInput()
                3 -> { panel = if (panel == Panel.CLIPS) Panel.KEYS else Panel.CLIPS }
                4 -> { panel = if (panel == Panel.EMOJI) Panel.KEYS else Panel.EMOJI }
                5 -> service.toggleIncognito()
            }
            invalidate()
            return true
        }
        when (panel) {
            Panel.KEYS -> {
                val top = dp(48f)
                val gap = dp(5f)
                val rowHeight = (height - top - dp(5f) - gap * 3) / 4f
                val rowIndex = ((y - top) / (rowHeight + gap)).toInt().coerceIn(0, 3)
                val rows = keyRows()
                val (startX, maxWidth, _) = geometry()
                val row = rows[rowIndex]
                val total = row.sumOf { it.weight.toDouble() }.toFloat()
                val available = maxWidth - dp(6f) * (row.size - 1)
                var cursor = startX + dp(3f)
                for (key in row) {
                    val keyWidth = available * key.weight / total
                    if (x >= cursor && x <= cursor + keyWidth) {
                        if (key.label == "☺") { panel = Panel.EMOJI; invalidate() }
                        else service.handleKey(key.label)
                        break
                    }
                    cursor += keyWidth + dp(6f)
                }
            }
            Panel.CLIPS -> {
                if (service.incognito) return true
                val index = ((y - dp(50f)) / dp(37f)).toInt()
                val activeFolder = KeyBoltPrefs.activeFolder(context)
                val clip = KeyBoltPrefs.clips(context).filter { it.folderId == activeFolder }.getOrNull(index)
                if (clip != null) {
                    if (clip.uri.isNotBlank()) service.commitImageOrDocument(android.net.Uri.parse(clip.uri), clip.displayName)
                    else service.insertText(clip.text)
                }
            }
            Panel.EMOJI -> {
                val emojis = emojiList()
                val col = (x / (width / 5f)).toInt().coerceIn(0, 4)
                val row = ((y - dp(57f)) / dp(43f)).toInt().coerceAtLeast(0)
                emojis.getOrNull(row * 5 + col)?.let(service::insertText)
            }
        }
        return true
    }
}
