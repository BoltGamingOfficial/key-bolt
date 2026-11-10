package com.bolt.keybolt

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class BoltKeyboardView(context: android.content.Context, private val service: KeyboardService) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var shifted = true
    private var symbols = false
    private var panel = Panel.KEYS
    private val density = resources.displayMetrics.density
    private val gold get() = if (service.incognito) 0xFF929292.toInt() else KeyBoltPrefs.accent(context)
    private val ink get() = if (service.incognito) 0xFFF0F0F0.toInt() else 0xFF171717.toInt()
    private enum class Panel { KEYS, CLIPS, EMOJI }
    private fun dp(value: Float) = value * density
    private fun sp(value: Float) = value * resources.displayMetrics.scaledDensity

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = (dp(274f) * KeyBoltPrefs.keyScale(context)).toInt()
        setMeasuredDimension(resolveSize(suggestedMinimumWidth, widthMeasureSpec), resolveSize(desired, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(if (service.incognito) 0xFF080808.toInt() else 0xFF101010.toInt())
        drawToolbar(canvas)
        when (panel) {
            Panel.KEYS -> drawKeys(canvas)
            Panel.CLIPS -> drawClips(canvas)
            Panel.EMOJI -> drawEmoji(canvas)
        }
    }

    private fun drawToolbar(canvas: Canvas) {
        val h = dp(43f)
        paint.color = if (service.incognito) 0xFF171717.toInt() else 0xFF242424.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), h, paint)

        val cell = width / 6f
        val activeColor = if (service.incognito) 0xFFE0E0E0.toInt() else 0xFFD5A640.toInt()
        val idleColor = 0xFFB8B8B8.toInt()
        val active = listOf(false, false, false, panel == Panel.CLIPS, panel == Panel.EMOJI, service.incognito)

        for (index in 0..5) {
            val cx = cell * (index + .5f)
            val cy = h * .49f
            paint.color = if (active[index]) activeColor else idleColor
            when (index) {
                0 -> {
                    paint.textAlign = Paint.Align.CENTER
                    paint.textSize = sp(14f)
                    canvas.drawText(if (symbols) "ABC" else "123", cx, h * .68f, paint)
                }
                1 -> drawGlobeIcon(canvas, cx, cy, dp(9f))
                2 -> drawMicrophoneIcon(canvas, cx, cy, dp(9f))
                3 -> drawClipboardIcon(canvas, cx, cy, dp(9f))
                4 -> drawSmileyIcon(canvas, cx, cy, dp(9f))
                5 -> drawIncognitoIcon(canvas, cx, cy)
            }
            if (active[index]) {
                paint.style = Paint.Style.FILL
                paint.color = activeColor
                canvas.drawRoundRect(cell * index + dp(13f), h - dp(3f),
                    cell * (index + 1) - dp(13f), h - dp(1f), dp(1f), dp(1f), paint)
            }
        }
    }

    private fun iconPaint(color: Int) {
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.7f)
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
    }

    private fun drawGlobeIcon(canvas: Canvas, x: Float, y: Float, r: Float) {
        iconPaint(paint.color)
        canvas.drawCircle(x, y, r, paint)
        canvas.drawOval(RectF(x - r * .48f, y - r, x + r * .48f, y + r), paint)
        canvas.drawLine(x - r * .9f, y, x + r * .9f, y, paint)
        canvas.drawArc(x - r, y - r * .48f, x + r, y + r * .48f, 0f, 180f, false, paint)
        canvas.drawArc(x - r, y - r * .48f, x + r, y + r * .48f, 180f, 180f, false, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawMicrophoneIcon(canvas: Canvas, x: Float, y: Float, r: Float) {
        iconPaint(paint.color)
        val capsule = RectF(x - r * .38f, y - r, x + r * .38f, y + r * .35f)
        canvas.drawRoundRect(capsule, r, r, paint)
        canvas.drawArc(x - r * .78f, y - r * .18f, x + r * .78f, y + r * 1.12f, 0f, 180f, false, paint)
        canvas.drawLine(x, y + r * 1.12f, x, y + r * 1.5f, paint)
        canvas.drawLine(x - r * .48f, y + r * 1.5f, x + r * .48f, y + r * 1.5f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawClipboardIcon(canvas: Canvas, x: Float, y: Float, r: Float) {
        iconPaint(paint.color)
        canvas.drawRoundRect(RectF(x - r * .78f, y - r * .72f, x + r * .78f, y + r),
            dp(2f), dp(2f), paint)
        canvas.drawRoundRect(RectF(x - r * .34f, y - r * 1.08f, x + r * .34f, y - r * .52f),
            dp(2f), dp(2f), paint)
        canvas.drawLine(x - r * .42f, y - r * .12f, x + r * .42f, y - r * .12f, paint)
        canvas.drawLine(x - r * .42f, y + r * .25f, x + r * .42f, y + r * .25f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawSmileyIcon(canvas: Canvas, x: Float, y: Float, r: Float) {
        iconPaint(paint.color)
        canvas.drawCircle(x, y, r, paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(x - r * .34f, y - r * .2f, dp(1.1f), paint)
        canvas.drawCircle(x + r * .34f, y - r * .2f, dp(1.1f), paint)
        paint.style = Paint.Style.STROKE
        canvas.drawArc(x - r * .48f, y - r * .12f, x + r * .48f, y + r * .62f, 15f, 150f, false, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawIncognitoIcon(canvas: Canvas, centerX: Float, centerY: Float) {
        val radius = dp(9f)
        paint.color = 0xFFD0D0D0.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.8f)
        canvas.drawCircle(centerX, centerY, radius, paint)
        canvas.drawLine(centerX - radius * .72f, centerY + radius * .72f,
            centerX + radius * .72f, centerY - radius * .72f, paint)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF929292.toInt()
        canvas.drawCircle(centerX, centerY, dp(2f), paint)
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
        val maxWidth = min(width.toFloat(), width * widthFactor * scale.coerceIn(.8f, 1.2f))
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
                val isSpace = key.label == "space"
                paint.color = if (service.incognito) {
                    if (key.special) 0xFF454545.toInt() else 0xFF292929.toInt()
                } else when {
                    isSpace -> 0xFF37383B.toInt()
                    key.special -> 0xFFD0A044.toInt()
                    else -> 0xFFF0EEE8.toInt()
                }
                canvas.drawRoundRect(rect, dp(6f), dp(6f), paint)

                paint.color = when {
                    service.incognito -> 0xFFF1F1F1.toInt()
                    isSpace -> 0xFFF4F1E9.toInt()
                    else -> 0xFF242424.toInt()
                }
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = if (isSpace) sp(10f) else sp(if (key.special) 17f else 21f) * scale.coerceIn(.9f, 1.1f)
                val label = when (key.label) {
                    "space" -> "English"
                    else -> if (shifted) key.label.uppercase() else key.label.lowercase()
                }
                canvas.drawText(label, rect.centerX(), rect.centerY() + dp(if (isSpace) 3f else 7f), paint)
                x += keyWidth + dp(6f)
            }
        }
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
            val preview = if (clip.uri.isBlank()) clip.text else "FILE  ·  ${clip.displayName}"
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
