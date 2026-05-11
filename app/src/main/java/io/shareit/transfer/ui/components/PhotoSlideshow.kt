package io.shareit.transfer.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.shareit.transfer.ui.theme.Champagne
import io.shareit.transfer.ui.theme.Cream
import io.shareit.transfer.ui.theme.LovePink
import kotlinx.coroutines.delay

@Composable
fun PhotoSlideshow(
    photoUris: List<String>,
    modifier: Modifier = Modifier,
    perPhotoMs: Long = 4500L,
    caption: String? = null,
) {
    if (photoUris.isEmpty()) return

    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(photoUris) {
        while (true) {
            delay(perPhotoMs)
            index = (index + 1) % photoUris.size
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.04f))
                )
            )
    ) {
        AnimatedContent(
            targetState = index,
            transitionSpec = {
                (fadeIn(tween(900)) togetherWith fadeOut(tween(900)))
            },
            label = "slideshow"
        ) { current ->
            KenBurnsImage(
                uri = photoUris[current],
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.55f)
                    )
                )
        )

        if (caption != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = caption,
                    color = Cream,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = "${index + 1} / ${photoUris.size}",
                color = Champagne,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .background(
                        color = LovePink.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun KenBurnsImage(uri: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "kenburns-${uri.hashCode()}")
    val scale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
        ),
        label = "kb-scale"
    )
    val translateX by transition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
        ),
        label = "kb-tx"
    )
    val translateY by transition.animateFloat(
        initialValue = 12f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
        ),
        label = "kb-ty"
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(uri)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationX = translateX
            translationY = translateY
        }
    )
}
