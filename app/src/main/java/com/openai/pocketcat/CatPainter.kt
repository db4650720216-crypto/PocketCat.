package com.openai.pocketcat

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.sin

class CatPainter {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
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
        val w = width.toFloat()
        val h = height.toFloat()

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(26, 31, 47)
        canvas.drawRect(0f, 0f, w, h, paint)

        drawRoom(canvas, w, h)
        drawCat(canvas, w, h, snapshot)
        toy?.let { drawToy(canvas, w * it.first, h * it.second) }
        if (showHud) drawHud(canvas, w, h, snapshot)
    }

    private fun drawRoom(canvas: Canvas, w: Float, h: Float) {
        paint.color = Color.rgb(41, 48, 69)
        canvas.drawRect(0f, h * 0.72f, w, h, paint)

        val window = RectF(w * 0.08f, h * 0.10f, w * 0.45f, h * 0.49f)
        paint.color = Color.rgb(17, 23, 37)
        canvas.drawRoundRect(window, 24f, 24f, paint)
        paint.color = Color.rgb(100, 119, 166)
        canvas.drawCircle(w * 0.35f, h * 0.20f, minOf(w, h) * 0.045f, paint)
        paint.color = Color.rgb(224, 230, 244)
        for (i in 0..10) {
            val sx = w * (0.11f + ((i * 37) % 31) / 100f)
            val sy = h * (0.13f + ((i * 19) % 25) / 100f)
            canvas.drawCircle(sx, sy, 2.2f, paint)
        }
        stroke.color = Color.rgb(87, 96, 125)
        stroke.strokeWidth = 5f
        canvas.drawRoundRect(window, 24f, 24f, stroke)
        canvas.drawLine(w * 0.265f, h * 0.10f, w * 0.265f, h * 0.49f, stroke)

        paint.color = Color.rgb(104, 77, 58)
        canvas.drawRoundRect(RectF(w * 0.58f, h * 0.23f, w * 0.89f, h * 0.255f), 6f, 6f, paint)
        paint.color = Color.rgb(73, 107, 83)
        canvas.drawOval(RectF(w * 0.68f, h * 0.15f, w * 0.73f, h * 0.24f), paint)
        canvas.drawOval(RectF(w * 0.73f, h * 0.14f, w * 0.79f, h * 0.24f), paint)
        canvas.drawOval(RectF(w * 0.78f, h * 0.16f, w * 0.83f, h * 0.24f), paint)
        paint.color = Color.rgb(155, 102, 72)
        canvas.drawRoundRect(RectF(w * 0.72f, h * 0.215f, w * 0.80f, h * 0.27f), 8f, 8f, paint)

        paint.color = Color.rgb(82, 73, 96)
        canvas.drawOval(RectF(w * 0.18f, h * 0.75f, w * 0.86f, h * 0.96f), paint)

        paint.color = Color.rgb(107, 82, 83)
        canvas.drawOval(RectF(w * 0.06f, h * 0.77f, w * 0.28f, h * 0.90f), paint)
        paint.color = Color.rgb(145, 108, 104)
        canvas.drawOval(RectF(w * 0.09f, h * 0.79f, w * 0.25f, h * 0.87f), paint)
    }

    private fun drawCat(canvas: Canvas, w: Float, h: Float, s: CatSnapshot) {
        val scale = minOf(w, h) * 0.16f
        val cx = w * s.x
        val cy = h * s.y
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(s.facing, 1f)

        val bob = when (s.action) {
            CatAction.WALKING, CatAction.PLAYING -> sin(s.tailPhase * 1.7f) * scale * 0.025f
            else -> 0f
        }
        canvas.translate(0f, bob)

        val fur = Color.rgb(215, 150, 86)
        val furDark = Color.rgb(177, 108, 62)
        val cream = Color.rgb(246, 218, 178)

        if (s.action == CatAction.SLEEPING) {
            paint.color = fur
            canvas.drawOval(RectF(-scale * 0.65f, -scale * 0.22f, scale * 0.62f, scale * 0.40f), paint)
            canvas.drawCircle(scale * 0.40f, -scale * 0.13f, scale * 0.30f, paint)
            drawEars(canvas, scale, fur)
            stroke.color = furDark
            stroke.strokeWidth = scale * 0.07f
            canvas.drawArc(RectF(scale * 0.28f, -scale * 0.08f, scale * 0.45f, scale * 0.05f), 5f, 170f, false, stroke)
            canvas.drawArc(RectF(scale * 0.49f, -scale * 0.08f, scale * 0.66f, scale * 0.05f), 5f, 170f, false, stroke)
            stroke.strokeWidth = scale * 0.10f
            canvas.drawArc(RectF(-scale * 0.72f, -scale * 0.05f, scale * 0.05f, scale * 0.55f), 90f, 220f, false, stroke)
            canvas.restore()
            return
        }

        stroke.color = furDark
        stroke.strokeWidth = scale * 0.15f
        val tailLift = sin(s.tailPhase) * scale * 0.14f
        val tail = Path().apply {
            moveTo(-scale * 0.45f, scale * 0.13f)
            cubicTo(-scale * 0.95f, scale * 0.05f, -scale * 0.98f, -scale * 0.38f + tailLift, -scale * 0.55f, -scale * 0.43f + tailLift)
        }
        canvas.drawPath(tail, stroke)

        paint.color = fur
        canvas.drawOval(RectF(-scale * 0.55f, -scale * 0.25f, scale * 0.50f, scale * 0.50f), paint)
        paint.color = cream
        canvas.drawOval(RectF(scale * 0.04f, -scale * 0.13f, scale * 0.42f, scale * 0.42f), paint)

        paint.color = furDark
        val stride = if (s.action == CatAction.WALKING || s.action == CatAction.PLAYING) sin(s.tailPhase * 2f) * scale * 0.07f else 0f
        canvas.drawRoundRect(RectF(-scale * 0.30f + stride, scale * 0.30f, -scale * 0.08f + stride, scale * 0.62f), scale * 0.08f, scale * 0.08f, paint)
        canvas.drawRoundRect(RectF(scale * 0.22f - stride, scale * 0.30f, scale * 0.43f - stride, scale * 0.62f), scale * 0.08f, scale * 0.08f, paint)

        paint.color = fur
        canvas.drawCircle(scale * 0.38f, -scale * 0.35f, scale * 0.42f, paint)
        drawEars(canvas, scale, fur)

        paint.color = cream
        canvas.drawOval(RectF(scale * 0.25f, -scale * 0.27f, scale * 0.64f, scale * 0.02f), paint)

        stroke.color = Color.rgb(49, 43, 43)
        stroke.strokeWidth = scale * 0.045f
        if (s.blink) {
            canvas.drawLine(scale * 0.20f, -scale * 0.42f, scale * 0.32f, -scale * 0.42f, stroke)
            canvas.drawLine(scale * 0.49f, -scale * 0.42f, scale * 0.61f, -scale * 0.42f, stroke)
        } else {
            paint.color = Color.rgb(53, 72, 54)
            canvas.drawOval(RectF(scale * 0.19f, -scale * 0.49f, scale * 0.33f, -scale * 0.33f), paint)
            canvas.drawOval(RectF(scale * 0.48f, -scale * 0.49f, scale * 0.62f, -scale * 0.33f), paint)
            paint.color = Color.BLACK
            canvas.drawOval(RectF(scale * 0.245f, -scale * 0.485f, scale * 0.275f, -scale * 0.335f), paint)
            canvas.drawOval(RectF(scale * 0.535f, -scale * 0.485f, scale * 0.565f, -scale * 0.335f), paint)
        }

        paint.color = Color.rgb(125, 77, 77)
        val nose = Path().apply {
            moveTo(scale * 0.38f, -scale * 0.24f)
            lineTo(scale * 0.49f, -scale * 0.24f)
            lineTo(scale * 0.435f, -scale * 0.16f)
            close()
        }
        canvas.drawPath(nose, paint)
        stroke.color = Color.rgb(84, 62, 60)
        stroke.strokeWidth = scale * 0.025f
        canvas.drawArc(RectF(scale * 0.34f, -scale * 0.18f, scale * 0.44f, -scale * 0.07f), 350f, 120f, false, stroke)
        canvas.drawArc(RectF(scale * 0.43f, -scale * 0.18f, scale * 0.53f, -scale * 0.07f), 70f, 120f, false, stroke)

        if (s.purr) {
            paint.color = Color.rgb(232, 154, 168)
            val pulse = 0.88f + 0.12f * sin(s.tailPhase * 1.8f)
            drawHeart(canvas, scale * 0.74f, -scale * 0.75f, scale * 0.11f * pulse)
            drawHeart(canvas, scale * 0.96f, -scale * 0.55f, scale * 0.075f * pulse)
        }

        canvas.restore()
    }

    private fun drawEars(canvas: Canvas, scale: Float, fur: Int) {
        paint.color = fur
        val left = Path().apply {
            moveTo(scale * 0.06f, -scale * 0.56f)
            lineTo(scale * 0.16f, -scale * 0.91f)
            lineTo(scale * 0.36f, -scale * 0.67f)
            close()
        }
        val right = Path().apply {
            moveTo(scale * 0.46f, -scale * 0.68f)
            lineTo(scale * 0.69f, -scale * 0.91f)
            lineTo(scale * 0.75f, -scale * 0.52f)
            close()
        }
        canvas.drawPath(left, paint)
        canvas.drawPath(right, paint)
        paint.color = Color.rgb(219, 132, 129)
        val inner = Path().apply {
            moveTo(scale * 0.13f, -scale * 0.65f)
            lineTo(scale * 0.18f, -scale * 0.80f)
            lineTo(scale * 0.28f, -scale * 0.68f)
            close()
        }
        canvas.drawPath(inner, paint)
    }

    private fun drawToy(canvas: Canvas, x: Float, y: Float) {
        stroke.color = Color.rgb(198, 202, 214)
        stroke.strokeWidth = 3f
        canvas.drawLine(x, 0f, x, y - 18f, stroke)
        paint.color = Color.rgb(201, 90, 98)
        canvas.drawCircle(x, y, 18f, paint)
        paint.color = Color.rgb(239, 195, 86)
        canvas.drawCircle(x + 7f, y - 5f, 5f, paint)
    }

    private fun drawHud(canvas: Canvas, w: Float, h: Float, s: CatSnapshot) {
        paint.color = Color.argb(170, 12, 16, 25)
        canvas.drawRoundRect(RectF(20f, 20f, w - 20f, 92f), 20f, 20f, paint)
        paint.color = Color.WHITE
        paint.textSize = minOf(w, h) * 0.035f
        canvas.drawText("${s.action.name.lowercase().replaceFirstChar { it.uppercase() }}  •  Affection ${(s.affection * 100).toInt()}%  •  Energy ${(s.energy * 100).toInt()}%", 38f, 64f, paint)
    }

    private fun drawHeart(canvas: Canvas, x: Float, y: Float, size: Float) {
        val p = Path().apply {
            moveTo(x, y + size)
            cubicTo(x - size * 1.25f, y + size * 0.15f, x - size * 0.82f, y - size * 0.72f, x, y - size * 0.10f)
            cubicTo(x + size * 0.82f, y - size * 0.72f, x + size * 1.25f, y + size * 0.15f, x, y + size)
            close()
        }
        canvas.drawPath(p, paint)
    }
}
