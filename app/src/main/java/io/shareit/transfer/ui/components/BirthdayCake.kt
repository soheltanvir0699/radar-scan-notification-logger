package io.shareit.transfer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import io.shareit.transfer.ui.theme.Champagne
import io.shareit.transfer.ui.theme.Cream
import io.shareit.transfer.ui.theme.Gold
import io.shareit.transfer.ui.theme.LovePink
import io.shareit.transfer.ui.theme.Magenta
import io.shareit.transfer.ui.theme.Plum
import io.shareit.transfer.ui.theme.RoseGold
import io.shareit.transfer.ui.theme.SoftPink
import io.shareit.transfer.ui.theme.SunsetOrange
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun BirthdayCake(
    modifier: Modifier = Modifier,
    candleCount: Int = 5,
) {
    val transition = rememberInfiniteTransition(label = "cake")
    val flicker by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing)
        ),
        label = "cake-flicker"
    )
    val random = remember { Random(91234L) }
    val candleOffsets = remember(candleCount) {
        List(candleCount) { random.nextFloat() * 2f * PI.toFloat() }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val plateY = h * 0.92f
        val plateWidth = w * 0.86f
        drawOval(
            color = Champagne.copy(alpha = 0.9f),
            topLeft = Offset((w - plateWidth) / 2f, plateY - h * 0.04f),
            size = Size(plateWidth, h * 0.08f)
        )

        val tier1Width = w * 0.78f
        val tier1Height = h * 0.22f
        val tier1Top = plateY - tier1Height - h * 0.005f
        drawRoundedTier(
            topLeft = Offset((w - tier1Width) / 2f, tier1Top),
            size = Size(tier1Width, tier1Height),
            primary = Magenta,
            secondary = LovePink
        )
        drawDripping(
            topLeft = Offset((w - tier1Width) / 2f, tier1Top),
            size = Size(tier1Width, tier1Height * 0.55f),
            color = SoftPink
        )

        val tier2Width = w * 0.58f
        val tier2Height = h * 0.20f
        val tier2Top = tier1Top - tier2Height - h * 0.005f
        drawRoundedTier(
            topLeft = Offset((w - tier2Width) / 2f, tier2Top),
            size = Size(tier2Width, tier2Height),
            primary = Plum,
            secondary = Magenta
        )
        drawDripping(
            topLeft = Offset((w - tier2Width) / 2f, tier2Top),
            size = Size(tier2Width, tier2Height * 0.55f),
            color = RoseGold
        )

        val tier3Width = w * 0.40f
        val tier3Height = h * 0.18f
        val tier3Top = tier2Top - tier3Height - h * 0.005f
        drawRoundedTier(
            topLeft = Offset((w - tier3Width) / 2f, tier3Top),
            size = Size(tier3Width, tier3Height),
            primary = LovePink,
            secondary = SoftPink
        )
        drawDripping(
            topLeft = Offset((w - tier3Width) / 2f, tier3Top),
            size = Size(tier3Width, tier3Height * 0.55f),
            color = Cream
        )

        val candleAreaTop = tier3Top
        val candleSpacing = tier3Width / (candleCount + 1)
        for (i in 0 until candleCount) {
            val cx = (w - tier3Width) / 2f + candleSpacing * (i + 1)
            drawCandle(
                baseCenter = Offset(cx, candleAreaTop),
                height = h * 0.18f,
                width = w * 0.022f,
                flickerPhase = flicker + candleOffsets[i]
            )
        }
    }
}

private fun DrawScope.drawRoundedTier(
    topLeft: Offset,
    size: Size,
    primary: Color,
    secondary: Color,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(secondary, primary),
        startY = topLeft.y,
        endY = topLeft.y + size.height
    )
    drawRoundRect(
        brush = gradient,
        topLeft = topLeft,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height * 0.18f, size.height * 0.18f)
    )
    val dotSpacing = size.width / 8f
    for (i in 1..7) {
        val dx = topLeft.x + dotSpacing * i
        val dy = topLeft.y + size.height * 0.55f
        drawCircle(
            color = Gold.copy(alpha = 0.85f),
            radius = size.height * 0.05f,
            center = Offset(dx, dy)
        )
    }
}

private fun DrawScope.drawDripping(
    topLeft: Offset,
    size: Size,
    color: Color,
) {
    val path = Path().apply {
        moveTo(topLeft.x, topLeft.y + size.height * 0.5f)
        var x = topLeft.x
        val step = size.width / 8f
        var down = true
        while (x < topLeft.x + size.width) {
            val nextX = x + step
            val controlY = if (down) topLeft.y + size.height else topLeft.y + size.height * 0.45f
            quadraticTo(
                (x + nextX) / 2f, controlY,
                nextX, topLeft.y + size.height * 0.5f
            )
            x = nextX
            down = !down
        }
        lineTo(topLeft.x + size.width, topLeft.y)
        lineTo(topLeft.x, topLeft.y)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawCandle(
    baseCenter: Offset,
    height: Float,
    width: Float,
    flickerPhase: Float,
) {
    drawRoundRect(
        color = Cream,
        topLeft = Offset(baseCenter.x - width / 2f, baseCenter.y - height),
        size = Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width / 3f, width / 3f)
    )
    for (i in 0..2) {
        val y = baseCenter.y - height + height * (0.25f + i * 0.25f)
        drawLine(
            color = LovePink.copy(alpha = 0.7f),
            start = Offset(baseCenter.x - width / 2f, y),
            end = Offset(baseCenter.x + width / 2f, y),
            strokeWidth = 1.2f
        )
    }
    val wickTop = Offset(baseCenter.x, baseCenter.y - height - width * 0.2f)
    drawLine(
        color = Color(0xFF222222),
        start = Offset(baseCenter.x, baseCenter.y - height),
        end = wickTop,
        strokeWidth = 2f
    )
    val flameScale = 0.85f + 0.2f * sin(flickerPhase)
    val flameWidth = width * 1.3f * flameScale
    val flameHeight = width * 3.0f * flameScale
    val flameCenter = Offset(wickTop.x, wickTop.y - flameHeight / 2f)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Cream, SunsetOrange.copy(alpha = 0.0f)),
            center = flameCenter,
            radius = flameHeight * 1.6f
        ),
        topLeft = Offset(flameCenter.x - flameWidth * 1.4f, flameCenter.y - flameHeight * 1.4f),
        size = Size(flameWidth * 2.8f, flameHeight * 2.8f)
    )
    drawOval(
        color = SunsetOrange,
        topLeft = Offset(flameCenter.x - flameWidth / 2f, flameCenter.y - flameHeight / 2f),
        size = Size(flameWidth, flameHeight)
    )
    drawOval(
        color = Gold,
        topLeft = Offset(flameCenter.x - flameWidth * 0.35f, flameCenter.y - flameHeight * 0.30f),
        size = Size(flameWidth * 0.7f, flameHeight * 0.65f)
    )
}
