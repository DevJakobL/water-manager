package com.dev.jakob.watermanager.ui.welcome

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.ui.welcome.viewmodel.WelcomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.*

/**
 * Der zustandsbehaftete Hauptbildschirm, der die Logik und den Zustand verwaltet.
 *
 * @param welcomeViewModel Das ViewModel, das den Zustand und die Geschäftslogik bereitstellt.
 */
@Composable
fun WelcomeScreen(
    welcomeViewModel: WelcomeViewModel = koinViewModel()
) {
    val uiState by welcomeViewModel.uiState.collectAsState()
    val progress = if (uiState.dailyGoal > 0) {
        (uiState.totalWaterToday.toFloat() / uiState.dailyGoal.toFloat())
    } else {
        0f
    }

    WelcomeScreenContent(
        progress = progress,
        totalWaterToday = uiState.totalWaterToday,
        dailyGoal = uiState.dailyGoal,
        containers = uiState.containers,
        onAddWater = { container -> welcomeViewModel.addWater(container) }
    )
}

/**
 * Die zustandslose Composable-Funktion, die die UI für den WelcomeScreen darstellt.
 */
@Composable
private fun WelcomeScreenContent(
    progress: Float,
    totalWaterToday: Int,
    dailyGoal: Int,
    containers: List<Container>,
    onAddWater: (Container) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101319)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hauptinhaltsbereich mit 78% der Höhe
            Column(
                modifier = Modifier
                    .weight(0.78f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tagesziel: ${formatWaterAmount(dailyGoal)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                CircularProgressRing(
                    progress = progress,
                    currentAmount = totalWaterToday,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .aspectRatio(1f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Mehr trinken ist meist unkritisch, hör auf dein Durstgefühl.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            // Unterer Bereich mit 22% der Höhe, vertikal scrollbar
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(0.22f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(containers) { container ->
                    OutlinedButton(
                        onClick = { onAddWater(container) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${container.name} ${container.size} ml",
                            textAlign = TextAlign.Center // Text zentrieren für Umbruch
                        )
                    }
                }
            }
        }
    }
}


/**
 * Ein kreisförmiger Fortschrittsring, der den Trinkfortschritt anzeigt.
 */
@Composable
private fun CircularProgressRing(
    progress: Float,
    currentAmount: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000), label = "ProgressAnimation"
    )

    val ringColor = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val backgroundColor = Color.DarkGray
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.width * 0.1f // Proportionale Strichstärke
            val diameter = size.width - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(diameter, diameter),
                topLeft = topLeft
            )

            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(diameter, diameter),
                topLeft = topLeft
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatWaterAmount(currentAmount),
                style = MaterialTheme.typography.displayLarge,
                color = ringColor,
                fontSize = 56.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }
    }
}

private fun formatWaterAmount(amount: Int): String {
    return if (amount >= 1000) {
        String.format(Locale.getDefault(), "%.1f l", amount / 1000.0)
    } else {
        "$amount ml"
    }
}
