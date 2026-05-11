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
import io.shareit.transfer.ui.theme.Champagne
import io.shareit.transfer.ui.theme.Gold
import io.shareit.transfer.ui.theme.Lavender
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class Star(
    val x: Float,
    val y: Float,
    val baseRadius: Float,
    val color: Color,
    val twinkleSpeed: Float,
    val phase: Float,
)

@Composable
fun TwinklingStars(
    modifier: Modifier = Modifier,
    count: Int = 90,
    seed: Long = 7777L,
) {
    val random = remember(seed) { Random(seed) }
    val palette = listOf(Champagne, Gold, Lavender, Color.White)
    val stars = remember(seed, count) {
        List(count) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat(),
                baseRadius = 0.6f + random.nextFloat() * 1.8f,
                color = palette[random.nextInt(palette.size)],
                twinkleSpeed = 0.5f + random.nextFloat() * 2.5f,
                phase = random.nextFloat() * 2f * PI.toFloat(),
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "stars")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing)
        ),
        label = "stars-progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (s in stars) {
            val twinkle = (sin(progress * s.twinkleSpeed + s.phase) * 0.5f + 0.5f)
            val radius = s.baseRadius * (0.6f + twinkle * 1.4f)
            val alpha = 0.35f + twinkle * 0.65f
            drawCircle(
                color = s.color.copy(alpha = alpha),
                radius = radius,
                center = Offset(s.x * w, s.y * h)
            )
        }
    }
}
