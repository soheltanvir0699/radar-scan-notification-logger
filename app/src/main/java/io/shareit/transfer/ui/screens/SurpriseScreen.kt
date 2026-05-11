package io.shareit.transfer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.shareit.transfer.ui.animations.ConfettiBurst
import io.shareit.transfer.ui.animations.Fireworks
import io.shareit.transfer.ui.animations.FloatingBalloons
import io.shareit.transfer.ui.animations.FloatingHearts
import io.shareit.transfer.ui.animations.TwinklingStars
import io.shareit.transfer.ui.components.BirthdayCake
import io.shareit.transfer.ui.components.PhotoSlideshow
import io.shareit.transfer.ui.components.VideoGallery
import io.shareit.transfer.util.PhotoLoader
import androidx.compose.ui.platform.LocalContext
import io.shareit.transfer.ui.theme.Champagne
import io.shareit.transfer.ui.theme.Cream
import io.shareit.transfer.ui.theme.Gold
import io.shareit.transfer.ui.theme.LovePink
import io.shareit.transfer.ui.theme.MidnightDeep
import io.shareit.transfer.ui.theme.MidnightPurple
import io.shareit.transfer.ui.theme.Plum
import io.shareit.transfer.ui.theme.RoseGold
import io.shareit.transfer.ui.theme.SoftPink
import kotlinx.coroutines.delay

@Composable
fun SurpriseScreen(
    wifeName: String,
    wifeShort: String,
    husbandName: String,
    message: String,
    onClose: () -> Unit,
) {
    var revealStage by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val photos = remember { PhotoLoader.listPhotoUris(context) }
    val videos = remember { PhotoLoader.listVideoAssetPaths(context) }

    LaunchedEffect(Unit) {
        delay(80L)
        revealStage = 1
        delay(900L)
        revealStage = 2
        delay(900L)
        revealStage = 3
        delay(1100L)
        revealStage = 4
        delay(900L)
        revealStage = 5
        delay(900L)
        revealStage = 6
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to MidnightDeep,
                    0.5f to MidnightPurple,
                    1f to Plum
                )
            )
    ) {
        TwinklingStars(modifier = Modifier.fillMaxSize(), count = 120)
        Fireworks(modifier = Modifier.fillMaxSize(), spawnIntervalMs = 750L)
        FloatingBalloons(modifier = Modifier.fillMaxSize(), count = 18)
        FloatingHearts(modifier = Modifier.fillMaxSize(), count = 36)
        ConfettiBurst(modifier = Modifier.fillMaxSize(), pieces = 240)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Cream)
                }
            }

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = revealStage >= 1,
                enter = fadeIn(animationSpec = tween(700)) +
                    scaleIn(initialScale = 0.6f, animationSpec = tween(700)),
                exit = fadeOut() + scaleOut()
            ) {
                ShimmeringTitle(text = "Happy Birthday")
            }

            Spacer(Modifier.height(6.dp))

            AnimatedVisibility(
                visible = revealStage >= 2,
                enter = fadeIn(animationSpec = tween(700)) +
                    slideInVertically(animationSpec = tween(700)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = wifeShort,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Bold,
                            color = LovePink,
                            fontSize = 96.sp,
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = wifeName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Champagne,
                            fontStyle = FontStyle.Italic
                        ),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(
                visible = revealStage >= 3,
                enter = fadeIn(tween(900)) + scaleIn(initialScale = 0.7f, animationSpec = tween(900))
            ) {
                BirthdayCake(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(260.dp),
                    candleCount = 5
                )
            }

            Spacer(Modifier.height(22.dp))

            AnimatedVisibility(
                visible = revealStage >= 4 && photos.isNotEmpty(),
                enter = fadeIn(tween(900)) + scaleIn(initialScale = 0.85f, animationSpec = tween(900))
            ) {
                PhotoSlideshow(
                    photoUris = photos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    perPhotoMs = 4500L,
                    caption = "My favorite person in the world."
                )
            }

            Spacer(Modifier.height(22.dp))

            AnimatedVisibility(
                visible = revealStage >= 5 && videos.isNotEmpty(),
                enter = fadeIn(tween(900)) + scaleIn(initialScale = 0.85f, animationSpec = tween(900))
            ) {
                VideoGallery(
                    assetPaths = videos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    caption = "Our little moments ♥"
                )
            }

            Spacer(Modifier.height(22.dp))

            AnimatedVisibility(
                visible = revealStage >= 6,
                enter = fadeIn(tween(900)) + expandVertically(tween(900))
            ) {
                LoveLetterCard(message = message, husbandName = husbandName)
            }

            Spacer(Modifier.height(18.dp))

            AnimatedVisibility(
                visible = revealStage >= 6,
                enter = fadeIn(tween(700))
            ) {
                BlinkingHint()
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ShimmeringTitle(text: String) {
    val transition = rememberInfiniteTransition(label = "title-shimmer")
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val brush = Brush.linearGradient(
        colors = listOf(Gold, Champagne, Cream, RoseGold, LovePink, Gold),
        start = androidx.compose.ui.geometry.Offset(shimmer * 600f - 300f, 0f),
        end = androidx.compose.ui.geometry.Offset(shimmer * 600f + 300f, 200f)
    )
    Text(
        text = text,
        style = TextStyle(
            brush = brush,
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
        ),
        modifier = Modifier.scale(pulse),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun LoveLetterCard(message: String, husbandName: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "A letter from ❤",
                color = SoftPink,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                color = Cream,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    lineHeight = 26.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "— $husbandName",
                color = Gold,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun BlinkingHint() {
    val transition = rememberInfiniteTransition(label = "hint")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint-alpha"
    )
    Text(
        text = "✦  Tap close when you're ready  ✦",
        color = Champagne,
        modifier = Modifier.alpha(alpha),
        style = MaterialTheme.typography.bodyMedium
    )
}
