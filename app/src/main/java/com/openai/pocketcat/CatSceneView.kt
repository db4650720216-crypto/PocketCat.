package com.openai.pocketcat

import android.content.Context
import android.graphics.Canvas
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View

class CatSceneView(
    context: Context,
    private val showHud: Boolean = true
) : View(context) {
    private val brain = CatBrain((System.currentTimeMillis() and 0x7fffffff).toInt())
    private val painter = CatPainter()
    private var running = false
    private var lastFrame = 0L
    private var draggingToy = false
    private var downX = 0f
    private var downY = 0f
    private var moved = false

    private val frame = object : Runnable {
        override fun run() {
            if (!running) return
            val now = SystemClock.uptimeMillis()
            val dt = if (lastFrame == 0L) 0f else (now - lastFrame) / 1000f
            lastFrame = now
            brain.update(dt)
            invalidate()
            postOnAnimation(this)
        }
    }

    init {
        isClickable = true
        contentDescription = "Interactive orange cat. Tap the cat to pet it, or drag anywhere to move a toy."
    }

    fun start() {
        if (running) return
        running = true
        lastFrame = SystemClock.uptimeMillis()
        postOnAnimation(frame)
    }

    fun stop() {
        running = false
        removeCallbacks(frame)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        painter.drawScene(canvas, width, height, brain.snapshot(), brain.toyPosition(), showHud)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val nx = if (width == 0) 0.5f else event.x / width
        val ny = if (height == 0) 0.5f else event.y / height
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                downX = event.x
                downY = event.y
                moved = false
                draggingToy = true
                brain.wake()
                brain.moveToy(nx, ny)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (kotlin.math.abs(event.x - downX) > 14f || kotlin.math.abs(event.y - downY) > 14f) moved = true
                if (draggingToy) brain.moveToy(nx, ny)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!moved && event.actionMasked == MotionEvent.ACTION_UP) brain.tap(nx, ny)
                else brain.releaseToy()
                draggingToy = false
                performClick()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
