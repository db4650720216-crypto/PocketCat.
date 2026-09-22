package com.openai.pocketcat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.util.Base64
import java.util.Calendar
import kotlin.math.max
import kotlin.math.sin

class CatPainter(context: Context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val overlay = Paint(Paint.ANTI_ALIAS_FLAG)

    private val sheet: Bitmap = run {
        val encoded = context.assets.open("cat_scenes.b64").bufferedReader().use { it.readText() }
        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("Unable to decode cat artwork")
    }

    private var pageOffset = 0.5f
    private var shownIndex = 0
    private var previousIndex = 0
    private var changeAt = SystemClock.uptimeMillis()

    fun setPageOffset(offset: Float) {
        pageOffset = offset.coerceIn(0f, 1f)
    }

    fun drawScene(
        canvas: Canvas,
        width: Int,
        height: Int,
        snapshot: CatSnapshot,
        toy: Pair<Float, Float>?,
        showHud: Boolean = false
    ) {
        if (width <= 0 || height <= 0) return

        val wanted = sceneFor(snapshot)
        if (wanted != shownIndex) {
            previousIndex = shownIndex
            shownIndex = wanted
            changeAt = SystemClock.uptimeMillis()
        }

        val elapsed = (SystemClock.uptimeMillis() - changeAt).coerceAtLeast(0L)
        val blend = (elapsed / 650f).coerceIn(0f, 1f)

        if (blend < 1f && previousIndex != shownIndex) {
            drawSceneCell(canvas, width, height, previousIndex, ((1f - blend) * 255).toInt(), snapshot)
        }
        drawSceneCell(canvas, width, height, shownIndex, (blend * 255).toInt().coerceAtLeast(if (blend < 1f) 1 else 255), snapshot)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour >= 21 || hour < 7) {
            overlay.color = Color.argb(if (hour >= 23 || hour < 6) 72 else 42, 9, 19, 42)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlay)
        }

        toy?.let {
            val x = width * it.first
            val y = height * it.second
            overlay.color = Color.argb(220, 165, 92, 70)
            canvas.drawCircle(x, y, max(12f, width * 0.025f), overlay)
            overlay.color = Color.argb(180, 240, 220, 185)
            canvas.drawCircle(x - width * 0.009f, y - width * 0.009f, max(3f, width * 0.007f), overlay)
        }

        if (snapshot.purr) {
            overlay.color = Color.argb(205, 238, 160, 168)
            overlay.textSize = max(26f, width * 0.075f)
            canvas.drawText("♥", width * 0.73f, height * 0.50f, overlay)
        }

        if (showHud) {
            overlay.color = Color.argb(160, 18, 14, 12)
            canvas.drawRoundRect(
                RectF(width * 0.06f, height * 0.045f, width * 0.94f, height * 0.135f),
                width * 0.04f,
                width * 0.04f,
                overlay
            )
            overlay.color = Color.WHITE
            overlay.textSize = max(18f, width * 0.048f)
            val state = snapshot.action.name.lowercase().replaceFirstChar { it.uppercase() }
            canvas.drawText("$state   •   Affection ${(snapshot.affection * 100).toInt()}%", width * 0.11f, height * 0.103f, overlay)
        }
    }

    private fun sceneFor(snapshot: CatSnapshot): Int {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if ((hour >= 23 || hour < 6) && snapshot.action !in setOf(CatAction.PLAYING, CatAction.PURRING, CatAction.CURIOUS)) {
            return 3
        }
        return when (snapshot.action) {
            CatAction.PLAYING -> 2
            CatAction.SLEEPING -> 3
            CatAction.PURRING, CatAction.CURIOUS, CatAction.IDLE -> 1
            CatAction.WALKING -> 0
        }
    }

    private fun drawSceneCell(
        canvas: Canvas,
        width: Int,
        height: Int,
        index: Int,
        alpha: Int,
        snapshot: CatSnapshot
    ) {
        val cellW = sheet.width / 2
        val cellH = sheet.height / 2
        val col = index % 2
        val row = index / 2
        val src = Rect(col * cellW, row * cellH, (col + 1) * cellW, (row + 1) * cellH)

        val sw = cellW.toFloat()
        val sh = cellH.toFloat()
        val cover = max(width / sw, height / sh)
        val breathing = if (index == 3 || index == 1) {
            1f + 0.007f * sin(snapshot.tailPhase * 0.65f)
        } else {
            1f + 0.004f * sin(snapshot.tailPhase * 0.8f)
        }
        val scale = cover * breathing
        val drawW = sw * scale
        val drawH = sh * scale

        val parallax = (pageOffset - 0.5f) * width * 0.08f
        val left = (width - drawW) / 2f - parallax
        val top = (height - drawH) / 2f
        val dst = RectF(left, top, left + drawW, top + drawH)

        paint.alpha = alpha.coerceIn(0, 255)
        canvas.drawBitmap(sheet, src, dst, paint)
        paint.alpha = 255
    }
}
