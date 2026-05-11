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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import io.shareit.transfer.ui.theme.Champagne
import io.shareit.transfer.ui.theme.Gold
import io.shareit.transfer.ui.theme.Lavender
import io.shareit.transfer.ui.theme.LovePink
import io.shareit.transfer.ui.theme.RoseGold
import io.shareit.transfer.ui.theme.SoftPink
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class Balloon(
    val xFraction: Float,
    val radius: Float,
    val color: Color,
    val phase: Float,
    val speed: Float,
    val swayAmp: Float,
    val startDelay: Float,
)

@Composable
fun FloatingBalloons(
    modifier: Modifier = Modifier,
    count: Int = 14,
    seed: Long = 4242L,
) {
    val palette = listOf(LovePink, SoftPink, RoseGold, Gold, Champagne, Lavender)
    val random = remember(seed) { Random(seed) }
    val balloons = remember(seed, count) {
        List(count) { i ->
            Balloon(
                xFraction = (i + random.nextFloat()) / count.toFloat(),
                radius = 32f + random.nextFloat() * 28f,
                color = palette[random.nextInt(palette.size)],
                phase = random.nextFloat() * 2f * PI.toFloat(),
                speed = 0.35f + random.nextFloat() * 0.5f,
                swayAmp = 25f + random.nextFloat() * 50f,
                startDelay = random.nextFloat(),
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "balloons")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing)
        ),
        label = "balloons-progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (b in balloons) {
            val travel = ((progress + b.startDelay) * b.speed) % 1f
            val y = h * (1.15f - travel * 1.4f)
            val sway = b.swayAmp * sin(progress * 2f * PI.toFloat() * b.speed * 1.5f + b.phase)
            val x = (b.xFraction * w + sway).coerceIn(b.radius, w - b.radius)
            drawBalloon(Offset(x, y), b.radius, b.color)
        }
    }
}

private fun DrawScope.drawBalloon(center: Offset, radius: Float, color: Color) {
    val highlight = Color.White.copy(alpha = 0.55f)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 1f), color.copy(alpha = 0.85f)),
            center = Offset(center.x - radius * 0.3f, center.y - radius * 0.4f),
            radius = radius * 1.6f
        ),
        topLeft = Offset(center.x - radius, center.y - radius * 1.15f),
        size = Size(radius * 2f, radius * 2.3f)
    )
    drawOval(
        color = highlight,
        topLeft = Offset(center.x - radius * 0.55f, center.y - radius * 0.85f),
        size = Size(radius * 0.4f, radius * 0.65f)
    )
    val tieY = center.y + radius * 1.15f
    val triangle = Path().apply {
        moveTo(center.x - radius * 0.16f, tieY - radius * 0.05f)
        lineTo(center.x + radius * 0.16f, tieY - radius * 0.05f)
        lineTo(center.x, tieY + radius * 0.18f)
        close()
    }
    drawPath(triangle, color)

    val stringPath = Path().apply {
        moveTo(center.x, tieY + radius * 0.18f)
        cubicTo(
            center.x - radius * 0.6f, tieY + radius * 0.9f,
            center.x + radius * 0.6f, tieY + radius * 1.7f,
            center.x - radius * 0.2f, tieY + radius * 2.6f
        )
    }
    drawPath(stringPath, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 2f))
}
