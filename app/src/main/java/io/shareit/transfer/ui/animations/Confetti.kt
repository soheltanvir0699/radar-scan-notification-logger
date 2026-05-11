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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
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

private data class Confetto(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val swayPhase: Float,
    val swaySpeed: Float,
    val swayAmp: Float,
)

@Composable
fun ConfettiBurst(
    modifier: Modifier = Modifier,
    pieces: Int = 220,
    seed: Long = 9821L,
    continuous: Boolean = true,
) {
    val palette = remember {
        listOf(LovePink, SoftPink, RoseGold, Gold, Champagne, Lavender, SunsetOrange)
    }
    val random = remember(seed) { Random(seed) }

    var width by remember { mutableStateOf(0f) }
    var height by remember { mutableStateOf(0f) }

    val confetti = remember(seed, pieces) {
        mutableListOf<Confetto>().apply {
            repeat(pieces) {
                add(spawnPiece(random, palette, 1080f, 1920f, initialBurst = true))
            }
        }
    }

    LaunchedEffect(width, height) {
        if (width <= 0f || height <= 0f) return@LaunchedEffect
        var lastNs = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNs == 0L) 0.016f else (now - lastNs) / 1_000_000_000f
                lastNs = now
                val gravity = height * 0.45f
                for (i in confetti.indices) {
                    val p = confetti[i]
                    p.vy += gravity * dt
                    val sway = p.swayAmp * sin((now / 1_000_000_000f) * p.swaySpeed + p.swayPhase)
                    p.x += (p.vx + sway) * dt
                    p.y += p.vy * dt
                    p.rotation += p.rotationSpeed * dt
                    if (p.y > height + 40f) {
                        if (continuous) {
                            confetti[i] = spawnPiece(random, palette, width, height, initialBurst = false)
                        }
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width != width || size.height != height) {
            width = size.width
            height = size.height
        }
        for (p in confetti) {
            drawConfetto(p)
        }
    }
}

private fun spawnPiece(
    random: Random,
    palette: List<Color>,
    w: Float,
    h: Float,
    initialBurst: Boolean,
): Confetto {
    val angle = random.nextFloat() * 2f * PI.toFloat()
    val speed = if (initialBurst) 200f + random.nextFloat() * 700f else 60f + random.nextFloat() * 120f
    val startX = if (initialBurst) w / 2f + (random.nextFloat() - 0.5f) * w * 0.3f else random.nextFloat() * w
    val startY = if (initialBurst) h * 0.55f + (random.nextFloat() - 0.5f) * 60f else -40f - random.nextFloat() * 200f
    return Confetto(
        x = startX,
        y = startY,
        vx = if (initialBurst) cos(angle) * speed else (random.nextFloat() - 0.5f) * 120f,
        vy = if (initialBurst) sin(angle) * speed - 250f else 100f + random.nextFloat() * 220f,
        rotation = random.nextFloat() * 360f,
        rotationSpeed = (random.nextFloat() - 0.5f) * 720f,
        width = 6f + random.nextFloat() * 10f,
        height = 10f + random.nextFloat() * 18f,
        color = palette[random.nextInt(palette.size)],
        swayPhase = random.nextFloat() * 2f * PI.toFloat(),
        swaySpeed = 1f + random.nextFloat() * 2.5f,
        swayAmp = 30f + random.nextFloat() * 60f,
    )
}

private fun DrawScope.drawConfetto(p: Confetto) {
    rotate(p.rotation, pivot = Offset(p.x, p.y)) {
        drawRect(
            color = p.color,
            topLeft = Offset(p.x - p.width / 2f, p.y - p.height / 2f),
            size = Size(p.width, p.height)
        )
    }
}
