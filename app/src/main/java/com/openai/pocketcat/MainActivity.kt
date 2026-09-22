package com.openai.pocketcat

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(26, 31, 47))
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        val title = TextView(this).apply {
            text = "Pocket Cat"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        root.addView(title, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val subtitle = TextView(this).apply {
            text = "Tap the cat to pet it. Drag your finger to tease it with a toy."
            textSize = 15f
            setTextColor(Color.rgb(190, 197, 214))
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, dp(8), 0, dp(14))
        }
        root.addView(subtitle, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val scene = CatSceneView(this, showHud = true)
        root.addView(scene, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, 0)
        }

        actions.addView(button("Set as live wallpaper") {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@MainActivity, CatWallpaperService::class.java)
                )
            }
            startActivity(intent)
        })

        actions.addView(button("Choose Pocket Cat screensaver") {
            runCatching { startActivity(Intent(Settings.ACTION_DREAM_SETTINGS)) }
                .onFailure { startActivity(Intent(Settings.ACTION_SETTINGS)) }
        })

        root.addView(actions, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        setContentView(root)
    }

    private fun button(label: String, onClick: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = 16f
        setOnClickListener { onClick() }
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        params.topMargin = dp(8)
        layoutParams = params
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
