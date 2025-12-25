package com.dev.jakob.watermanager.ui.welcome

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
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
import androidx.compose.ui.platform.LocalContext
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
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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

    // WindowSizeClass berechnen, um das Layout anzupassen
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as Activity)

    WelcomeScreenContent(
        progress = progress,
        totalWaterToday = uiState.totalWaterToday,
        dailyGoal = uiState.dailyGoal,
        containers = uiState.containers,
        onAddWater = { container -> welcomeViewModel.addWater(container) },
        widthSizeClass = windowSizeClass.widthSizeClass
    )
}

/**
 * Die zustandslose Composable-Funktion, die die UI für den WelcomeScreen darstellt.
 * Passt das Layout basierend auf der Breite des Bildschirms an.
 */
@Composable
private fun WelcomeScreenContent(
    progress: Float,
    totalWaterToday: Int,
    dailyGoal: Int,
    containers: List<Container>,
    onAddWater: (Container) -> Unit,
    widthSizeClass: WindowWidthSizeClass
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101319)
    ) {
        if (widthSizeClass == WindowWidthSizeClass.Compact) {
            // Layout für Smartphones (Portrait)
            CompactLayout(
                progress = progress,
                totalWaterToday = totalWaterToday,
                dailyGoal = dailyGoal,
                containers = containers,
                onAddWater = onAddWater
            )
        } else {
            // Layout für Tablets und Landscape (Expanded/Medium)
            ExpandedLayout(
                progress = progress,
                totalWaterToday = totalWaterToday,
                dailyGoal = dailyGoal,
                containers = containers,
                onAddWater = onAddWater
            )
        }
    }
}

/**
 * Layout für kompakte Bildschirme (Smartphones im Hochformat).
 */
@Composable
private fun CompactLayout(
    progress: Float,
    totalWaterToday: Int,
    dailyGoal: Int,
    containers: List<Container>,
    onAddWater: (Container) -> Unit
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
                ContainerButton(container = container, onClick = { onAddWater(container) })
            }
        }
    }
}

/**
 * Layout für breitere Bildschirme (Tablets, Landscape).
 * Teilt den Bildschirm in zwei Spalten: Links der Fortschritt, rechts die Buttons.
 */
@Composable
private fun ExpandedLayout(
    progress: Float,
    totalWaterToday: Int,
    dailyGoal: Int,
    containers: List<Container>,
    onAddWater: (Container) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Linke Spalte: Fortschrittsanzeige (50% Breite)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tagesziel: ${formatWaterAmount(dailyGoal)}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressRing(
                progress = progress,
                currentAmount = totalWaterToday,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mehr trinken ist meist unkritisch, hör auf dein Durstgefühl.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }

        // Rechte Spalte: Container-Buttons (50% Breite)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(containers) { container ->
                ContainerButton(
                    container = container,
                    onClick = { onAddWater(container) },
                    modifier = Modifier.height(80.dp)
                )
            }
        }
    }
}

@Composable
private fun ContainerButton(
    container: Container,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "${container.name} ${container.size} ml",
            textAlign = TextAlign.Center
        )
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

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Verwende constraints.maxWidth und constraints.maxHeight statt maxWidth/maxHeight direkt
        // um sicherzustellen, dass wir Pixelwerte für Berechnungen haben, wenn nötig,
        // aber hier nutzen wir die Dp-Eigenschaften des Scopes.
        
        // Der Fehler "BoxWithConstraints scope is not used" tritt auf, wenn man die Eigenschaften des Scopes
        // (maxWidth, maxHeight, constraints) nicht verwendet. Wir verwenden sie hier:
        val size = minOf(maxWidth, maxHeight)
        
        val strokeWidth = size * 0.1f // Proportionale Strichstärke
        
        // Canvas benötigt eine feste Größe oder Modifier.fillMaxSize()
        // Da wir size berechnet haben, nutzen wir diese.
        Canvas(modifier = Modifier.size(size)) {
            val canvasStrokeWidth = size.toPx() * 0.1f
            val canvasDiameter = size.toPx() - canvasStrokeWidth
            val canvasTopLeft = Offset(canvasStrokeWidth / 2, canvasStrokeWidth / 2)
            val canvasSize = Size(canvasDiameter, canvasDiameter)

            drawArc(
                color = Color.DarkGray,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = canvasStrokeWidth, cap = StrokeCap.Round),
                size = canvasSize,
                topLeft = canvasTopLeft
            )

            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = canvasStrokeWidth, cap = StrokeCap.Round),
                size = canvasSize,
                topLeft = canvasTopLeft
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Schriftgröße dynamisch anpassen, basierend auf der verfügbaren Größe
            val fontSize = (size.value * 0.2f).sp
            
            Text(
                text = formatWaterAmount(currentAmount),
                style = MaterialTheme.typography.displayLarge,
                color = ringColor,
                fontSize = fontSize,
                lineHeight = fontSize
            )
            Spacer(modifier = Modifier.height(size * 0.02f))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontSize = fontSize * 0.4f
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
