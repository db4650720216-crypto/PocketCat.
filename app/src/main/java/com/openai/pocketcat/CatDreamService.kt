package com.openai.pocketcat

import android.service.dreams.DreamService

class CatDreamService : DreamService() {
    private var scene: CatSceneView? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setInteractive(true)
        setFullscreen(true)
        setScreenBright(false)
        scene = CatSceneView(this, showHud = false).also { setContentView(it) }
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        scene?.start()
    }

    override fun onDreamingStopped() {
        scene?.stop()
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        scene?.stop()
        scene = null
        super.onDetachedFromWindow()
    }
}
