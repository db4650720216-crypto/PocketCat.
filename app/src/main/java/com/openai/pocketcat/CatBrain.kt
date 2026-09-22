package com.openai.pocketcat

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class CatAction {
    IDLE, WALKING, SLEEPING, PURRING, PLAYING, CURIOUS
}

data class CatSnapshot(
    val x: Float,
    val y: Float,
    val facing: Float,
    val action: CatAction,
    val affection: Float,
    val energy: Float,
    val curiosity: Float,
    val blink: Boolean,
    val purr: Boolean,
    val tailPhase: Float
)

class CatBrain(seed: Int = 1337) {
    private val random = Random(seed)

    var x = 0.52f
        private set
    var y = 0.78f
        private set
    var facing = 1f
        private set
    var action = CatAction.IDLE
        private set

    var affection = 0.45f
        private set
    var energy = 0.78f
        private set
    var curiosity = 0.64f
        private set

    private var targetX = x
    private var actionTimer = 2.4f
    private var blinkTimer = 1.5f
    private var blinkFor = 0f
    private var purrFor = 0f
    private var toyX: Float? = null
    private var toyY: Float? = null
    private var toyFreshFor = 0f
    private var tailPhase = 0f

    fun update(dtRaw: Float) {
        val dt = dtRaw.coerceIn(0f, 0.05f)
        tailPhase += dt * when (action) {
            CatAction.PLAYING -> 7.5f
            CatAction.PURRING -> 5.0f
            CatAction.WALKING -> 4.0f
            else -> 2.0f
        }

        energy = (energy - dt * when (action) {
            CatAction.PLAYING -> 0.010f
            CatAction.WALKING -> 0.004f
            CatAction.SLEEPING -> -0.035f
            else -> 0.0015f
        }).coerceIn(0f, 1f)

        curiosity = (curiosity + dt * if (action == CatAction.IDLE) 0.004f else -0.001f)
            .coerceIn(0f, 1f)

        if (purrFor > 0f) purrFor -= dt
        if (toyFreshFor > 0f) toyFreshFor -= dt else {
            toyX = null
            toyY = null
        }

        blinkTimer -= dt
        if (blinkTimer <= 0f) {
            blinkFor = 0.14f
            blinkTimer = random.nextFloat() * 3.2f + 1.4f
        }
        if (blinkFor > 0f) blinkFor -= dt

        val activeToy = toyX
        if (activeToy != null && energy > 0.12f) {
            action = CatAction.PLAYING
            moveToward(activeToy, dt, 0.34f)
            return
        }

        if (action == CatAction.SLEEPING) {
            if (energy > 0.82f) {
                action = CatAction.IDLE
                actionTimer = 1.2f
            }
            return
        }

        if (action == CatAction.PURRING && purrFor > 0f) return
        if (action == CatAction.PURRING) action = CatAction.IDLE

        actionTimer -= dt
        when (action) {
            CatAction.WALKING, CatAction.CURIOUS -> {
                moveToward(targetX, dt, if (action == CatAction.CURIOUS) 0.11f else 0.16f)
                if (abs(targetX - x) < 0.012f) actionTimer = 0f
            }
            else -> Unit
        }

        if (actionTimer <= 0f) chooseNextAction()
    }

    fun tap(nx: Float, ny: Float) {
        val nearCat = abs(nx - x) < 0.18f && abs(ny - y) < 0.20f
        if (nearCat) {
            affection = min(1f, affection + 0.025f)
            energy = min(1f, energy + 0.008f)
            purrFor = 1.8f + affection * 1.4f
            action = CatAction.PURRING
            actionTimer = purrFor
        } else {
            curiosity = min(1f, curiosity + 0.04f)
            targetX = nx.coerceIn(0.12f, 0.88f)
            action = CatAction.CURIOUS
            actionTimer = 2.4f
        }
    }

    fun moveToy(nx: Float, ny: Float) {
        toyX = nx.coerceIn(0.06f, 0.94f)
        toyY = ny.coerceIn(0.18f, 0.88f)
        toyFreshFor = 0.7f
        curiosity = min(1f, curiosity + 0.01f)
    }

    fun releaseToy() {
        toyFreshFor = max(toyFreshFor, 0.8f)
    }

    fun wake() {
        if (action == CatAction.SLEEPING) {
            action = CatAction.IDLE
            actionTimer = 1.1f
        }
    }

    fun snapshot() = CatSnapshot(
        x = x,
        y = y,
        facing = facing,
        action = action,
        affection = affection,
        energy = energy,
        curiosity = curiosity,
        blink = blinkFor > 0f,
        purr = purrFor > 0f,
        tailPhase = tailPhase
    )

    fun toyPosition(): Pair<Float, Float>? {
        val tx = toyX ?: return null
        val ty = toyY ?: return null
        return tx to ty
    }

    private fun chooseNextAction() {
        if (energy < 0.18f) {
            action = CatAction.SLEEPING
            actionTimer = 12f
            return
        }

        val r = random.nextFloat()
        when {
            r < 0.38f -> {
                action = CatAction.IDLE
                actionTimer = 1.5f + random.nextFloat() * 3.2f
            }
            r < 0.76f -> {
                action = CatAction.WALKING
                targetX = 0.12f + random.nextFloat() * 0.76f
                actionTimer = 4.5f
            }
            else -> {
                action = CatAction.CURIOUS
                targetX = 0.16f + random.nextFloat() * 0.68f
                actionTimer = 3.2f
            }
        }
    }

    private fun moveToward(target: Float, dt: Float, speed: Float) {
        val delta = target - x
        if (abs(delta) < 0.002f) return
        facing = if (delta >= 0f) 1f else -1f
        x = (x + delta.coerceIn(-speed * dt, speed * dt)).coerceIn(0.08f, 0.92f)
    }
}
