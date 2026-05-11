package io.shareit.transfer.ui.animations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.shareit.transfer.ui.theme.Champagne
import io.shareit.transfer.ui.theme.Gold
import io.shareit.transfer.ui.theme.Lavender
import io.shareit.transfer.ui.theme.LovePink
import io.shareit.transfer.ui.theme.RoseGold
import io.shareit.transfer.ui.theme.SoftPink
import io.shareit.transfer.ui.theme.SunsetOrange
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Spark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val color: Color,
    val size: Float,
)

@Composable
fun Fireworks(
    modifier: Modifier = Modifier,
    seed: Long = 555L,
    spawnIntervalMs: Long = 900L,
) {
    val palette = remember {
        listOf(LovePink, SoftPink, RoseGold, Gold, Champagne, Lavender, SunsetOrange)
    }
    val random = remember(seed) { Random(seed) }
    val sparks = remember { mutableListOf<Spark>() }

    var width by remember { mutableStateOf(0f) }
    var height by remember { mutableStateOf(0f) }

    LaunchedEffect(width, height) {
        if (width <= 0f || height <= 0f) return@LaunchedEffect
        var lastNs = 0L
        var lastSpawnMs = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNs == 0L) 0.016f else (now - lastNs) / 1_000_000_000f
                lastNs = now
                val nowMs = now / 1_000_000L
                if (nowMs - lastSpawnMs >= spawnIntervalMs) {
                    spawnFirework(sparks, random, palette, width, height)
                    lastSpawnMs = nowMs
                }
                val gravity = height * 0.18f
                val it = sparks.iterator()
                while (it.hasNext()) {
                    val s = it.next()
                    s.life -= dt
                    if (s.life <= 0f) {
                        it.remove()
                        continue
                    }
                    s.vy += gravity * dt
                    s.vx *= 0.985f
                    s.vy *= 0.985f
                    s.x += s.vx * dt
                    s.y += s.vy * dt
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width != width || size.height != height) {
            width = size.width
            height = size.height
        }
        for (s in sparks) {
            val alpha = (s.life / s.maxLife).coerceIn(0f, 1f)
            drawCircle(
                color = s.color.copy(alpha = alpha),
                radius = s.size * (0.5f + alpha * 0.7f),
                center = Offset(s.x, s.y)
            )
        }
    }
}

private fun spawnFirework(
    sparks: MutableList<Spark>,
    random: Random,
    palette: List<Color>,
    w: Float,
    h: Float,
) {
    val cx = w * (0.15f + random.nextFloat() * 0.7f)
    val cy = h * (0.15f + random.nextFloat() * 0.45f)
    val color = palette[random.nextInt(palette.size)]
    val particles = 70 + random.nextInt(40)
    val baseSpeed = 220f + random.nextFloat() * 180f
    repeat(particles) { i ->
        val angle = (i.toFloat() / particles) * 2f * PI.toFloat() + random.nextFloat() * 0.05f
        val speed = baseSpeed * (0.65f + random.nextFloat() * 0.6f)
        val life = 1.1f + random.nextFloat() * 0.9f
        sparks.add(
            Spark(
                x = cx,
                y = cy,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                life = life,
                maxLife = life,
                color = color,
                size = 2.5f + random.nextFloat() * 2.5f,
            )
        )
    }
}
