package com.openai.pocketcat

import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder

class CatWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = CatEngine()

    private inner class CatEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val brain = CatBrain((System.currentTimeMillis() and 0x7fffffff).toInt())
        private val painter = CatPainter()
        private var visible = false
        private var lastFrame = 0L
        private var activeUntil = 0L

        private val drawTask = object : Runnable {
            override fun run() {
                if (!visible) return
                val now = SystemClock.uptimeMillis()
                val dt = if (lastFrame == 0L) 0f else (now - lastFrame) / 1000f
                lastFrame = now
                brain.update(dt)
                drawFrame()
                val delay = if (now < activeUntil) 33L else 100L
                handler.postDelayed(this, delay)
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
        }

        override fun onVisibilityChanged(visibleNow: Boolean) {
            visible = visibleNow
            handler.removeCallbacks(drawTask)
            if (visibleNow) {
                lastFrame = SystemClock.uptimeMillis()
                drawTask.run()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            visible = false
            handler.removeCallbacks(drawTask)
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            handler.removeCallbacks(drawTask)
            super.onDestroy()
        }

        override fun onTouchEvent(event: MotionEvent) {
            val frame = surfaceHolder.surfaceFrame
            val w = frame.width().coerceAtLeast(1)
            val h = frame.height().coerceAtLeast(1)
            val nx = (event.x / w).coerceIn(0f, 1f)
            val ny = (event.y / h).coerceIn(0f, 1f)
            activeUntil = SystemClock.uptimeMillis() + 2500L
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    brain.wake()
                    brain.tap(nx, ny)
                }
                MotionEvent.ACTION_MOVE -> brain.moveToy(nx, ny)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> brain.releaseToy()
            }
            drawFrame()
            super.onTouchEvent(event)
        }

        private fun drawFrame() {
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    painter.drawScene(
                        canvas,
                        canvas.width,
                        canvas.height,
                        brain.snapshot(),
                        brain.toyPosition(),
                        showHud = false
                    )
                }
            } finally {
                if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
