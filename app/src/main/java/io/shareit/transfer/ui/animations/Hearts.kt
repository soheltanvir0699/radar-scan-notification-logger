package io.shareit.transfer.ui.animations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import io.shareit.transfer.ui.theme.LovePink
import io.shareit.transfer.ui.theme.RoseGold
import io.shareit.transfer.ui.theme.SoftPink
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class Heart(
    val xFraction: Float,
    val size: Float,
    val color: Color,
    val phase: Float,
    val swayAmplitude: Float,
    val rotation: Float,
    val speed: Float,
    val outline: Boolean,
)

@Composable
fun FloatingHearts(
    modifier: Modifier = Modifier,
    count: Int = 26,
    seed: Long = 1234L,
) {
    val random = remember(seed) { Random(seed) }
    val palette = listOf(LovePink, SoftPink, RoseGold)
    val hearts = remember(seed, count) {
        List(count) {
            Heart(
                xFraction = random.nextFloat(),
                size = 14f + random.nextFloat() * 30f,
                color = palette[random.nextInt(palette.size)].copy(
                    alpha = 0.55f + random.nextFloat() * 0.4f
                ),
                phase = random.nextFloat() * 2f * PI.toFloat(),
                swayAmplitude = 18f + random.nextFloat() * 38f,
                rotation = (random.nextFloat() - 0.5f) * 30f,
                speed = 0.6f + random.nextFloat() * 1.1f,
                outline = random.nextFloat() < 0.25f,
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "hearts")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing)
        ),
        label = "hearts-progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (heart in hearts) {
            val travel = (progress * heart.speed + heart.phase / (2f * PI.toFloat())) % 1f
            val y = h * (1.1f - travel * 1.25f)
            val sway = heart.swayAmplitude * sin(progress * 2f * PI.toFloat() * heart.speed + heart.phase)
            val x = (heart.xFraction * w + sway).coerceIn(-30f, w + 30f)
            translate(left = x - heart.size, top = y - heart.size) {
                rotate(heart.rotation, pivot = Offset(heart.size, heart.size)) {
                    drawHeart(heart.size, heart.color, heart.outline)
                }
            }
        }
    }
}

private fun DrawScope.drawHeart(size: Float, color: Color, outline: Boolean) {
    val width = size * 2f
    val height = size * 2f
    val path = Path().apply {
        moveTo(width / 2f, height * 0.85f)
        cubicTo(
            -width * 0.15f, height * 0.55f,
            width * 0.10f, height * 0.05f,
            width / 2f, height * 0.30f
        )
        cubicTo(
            width * 0.90f, height * 0.05f,
            width * 1.15f, height * 0.55f,
            width / 2f, height * 0.85f
        )
        close()
    }
    if (outline) {
        drawPath(path, color, style = Stroke(width = 2.4f))
    } else {
        drawPath(path, color)
    }
}
