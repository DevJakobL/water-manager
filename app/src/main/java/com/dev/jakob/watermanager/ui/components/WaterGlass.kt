package com.dev.jakob.watermanager.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.ui.theme.WaterManagerTheme

/**
 * Eine Composable-Funktion, die den Trinkfortschritt visuell als gefülltes Glas darstellt.
 * Die Glasform ist leicht konisch (oben breiter als unten), ähnlich einem IKEA-Glas.
 *
 * Das Glas füllt sich proportional zur aktuellen Wassermenge im Verhältnis zum Tagesziel.
 * Eine rote Linie markiert das Tagesziel. Wenn das Ziel überschritten wird, steigt der Wasserstand
 * über die rote Ziellinie hinaus. Ab 120% des Tagesziels steigt der Wasserstand nicht weiter.
 * Ab einem bestimmten Überlauf beginnen Blumen an den Seiten des Glases zu wachsen,
 * um den Erfolg zu visualisieren.
 * Das Wasser wird mit einem Farbverlauf dargestellt, um es realistischer erscheinen zu lassen.
 * Wenn kein Ziel gesetzt ist (Tagesziel ist 0), wird ein alternativer Zustand mit einer
 * entsprechenden Nachricht angezeigt.
 *
 * @param currentAmount Die aktuell getrunkene Wassermenge in Millilitern.
 * @param dailyGoal Das festgelegte Tagesziel für die Wasseraufnahme in Millilitern.
 * @param modifier Der [Modifier] für diese Composable.
 */
@Composable
fun WaterGlass(
    currentAmount: Int,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    if (dailyGoal <= 0) {
        // Edge Case: Kein Ziel gesetzt
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.no_goal_set),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val waterLevelRatio = (currentAmount.toFloat() / dailyGoal.toFloat()).coerceAtMost(1.2f) // Cap at 120%
    val animatedWaterLevel by animateFloatAsState(
        targetValue = waterLevelRatio,
        animationSpec = tween(durationMillis = 1000)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val glassHeight = size.height * 0.9f
            val glassBottomWidth = size.width * 0.4f // Narrower at the bottom
            val glassTopWidth = size.width * 0.6f // Wider at the top
            val glassStrokeWidth = 4.dp.toPx()

            val centerX = size.width / 2f
            val glassBottomY = size.height
            val glassTopY = size.height - glassHeight

            val glassBottomLeftX = centerX - glassBottomWidth / 2f
            val glassBottomRightX = centerX + glassBottomWidth / 2f
            val glassTopLeftX = centerX - glassTopWidth / 2f
            val glassTopRightX = centerX + glassTopWidth / 2f

            // Draw glass outline (trapezoidal shape)
            val glassPath = Path().apply {
                moveTo(glassBottomLeftX, glassBottomY)
                lineTo(glassTopLeftX, glassTopY)
                lineTo(glassTopRightX, glassTopY)
                lineTo(glassBottomRightX, glassBottomY)
                close()
            }

            drawPath(
                path = glassPath,
                color = Color.Gray.copy(alpha = 0.3f),
                style = Stroke(width = glassStrokeWidth)
            )

            // Draw water
            val waterFillHeight = glassHeight * animatedWaterLevel
            val waterTopY = glassBottomY - waterFillHeight

            // Calculate water width at its current top level (interpolation for conical shape)
            // This calculation needs to handle overflow correctly, so the water can extend beyond glassTopY
            val currentWaterTopWidth = if (animatedWaterLevel <= 1f) {
                val relativeWaterHeight = (glassBottomY - waterTopY) / glassHeight
                glassBottomWidth + (glassTopWidth - glassBottomWidth) * relativeWaterHeight
            } else {
                // If overflowing, the water width at the top should be at least glassTopWidth
                // and can expand slightly for a "spill" effect, but capped at 1.2f
                glassTopWidth * (1f + (animatedWaterLevel - 1f) * 0.2f).coerceAtMost(1.1f)
            }


            val waterPath = Path().apply {
                moveTo(centerX - glassBottomWidth / 2f, glassBottomY) // Bottom-left of water
                lineTo(centerX + glassBottomWidth / 2f, glassBottomY) // Bottom-right of water
                lineTo(centerX + currentWaterTopWidth / 2f, waterTopY) // Top-right of water
                lineTo(centerX - currentWaterTopWidth / 2f, waterTopY) // Top-left of water
                close()
            }

            val waterBrush = Brush.linearGradient(
                colors = listOf(Color(0xFF87CEEB), Color(0xFF4682B4)), // Light blue to steel blue
                start = Offset(centerX, glassBottomY),
                end = Offset(centerX, waterTopY)
            )

            drawPath(
                path = waterPath,
                brush = waterBrush,
                style = Fill
            )

            // Draw goal line
            val goalLineY = glassTopY
            val goalLineWidth = glassTopWidth // Width of the glass at the top

            drawLine(
                color = Color.Red,
                start = Offset(centerX - goalLineWidth / 2f, goalLineY),
                end = Offset(centerX + goalLineWidth / 2f, goalLineY),
                strokeWidth = 3.dp.toPx()
            )

            // Draw flowers if overflowing significantly
            if (animatedWaterLevel > 1.05f) { // Start growing flowers after 105%
                val flowerGrowth = ((animatedWaterLevel - 1.05f) * 5).coerceIn(0f, 1f) // Grow over 20% overflow
                val flowerSize = 10.dp.toPx() * flowerGrowth

                // Left flower
                drawFlower(
                    center = Offset(glassTopLeftX - flowerSize * 0.8f, glassTopY + flowerSize * 0.5f),
                    size = flowerSize,
                    petalColor = Color(0xFFFFFACD), // Light yellow
                    centerColor = Color(0xFFFFD700) // Gold
                )

                // Right flower
                drawFlower(
                    center = Offset(glassTopRightX + flowerSize * 0.8f, glassTopY + flowerSize * 0.5f),
                    size = flowerSize,
                    petalColor = Color(0xFFFFFACD), // Light yellow
                    centerColor = Color(0xFFFFD700) // Gold
                )
            }
        }
    }
}

/**
 * Helper function to draw a simple flower.
 *
 * @param center The center [Offset] of the flower.
 * @param size The overall size of the flower.
 * @param petalColor The color of the flower petals.
 * @param centerColor The color of the flower center.
 */
private fun DrawScope.drawFlower(
    center: Offset,
    size: Float,
    petalColor: Color,
    centerColor: Color
) {
    val petalRadius = size / 3
    val flowerCenterRadius = size / 6

    // Draw petals
    for (i in 0 until 6) {
        val angle = Math.toRadians(i * 60.0).toFloat()
        val petalOffsetX = center.x + petalRadius * 0.7f * kotlin.math.cos(angle)
        val petalOffsetY = center.y + petalRadius * 0.7f * kotlin.math.sin(angle)
        drawCircle(
            color = petalColor,
            radius = petalRadius,
            center = Offset(petalOffsetX, petalOffsetY)
        )
    }
    // Draw flower center
    drawCircle(
        color = centerColor,
        radius = flowerCenterRadius,
        center = center
    )
}

@Preview(showBackground = true)
@Composable
fun WaterGlassPreview() {
    WaterManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text("0% Progress")
            WaterGlass(currentAmount = 0, dailyGoal = 2000)
            Spacer(modifier = Modifier.height(16.dp))

            Text("50% Progress")
            WaterGlass(currentAmount = 1000, dailyGoal = 2000)
            Spacer(modifier = Modifier.height(16.dp))

            Text("100% Progress")
            WaterGlass(currentAmount = 2000, dailyGoal = 2000)
            Spacer(modifier = Modifier.height(16.dp))

            Text("110% Progress (Overflow & Flowers)")
            WaterGlass(currentAmount = 2200, dailyGoal = 2000)
            Spacer(modifier = Modifier.height(16.dp))

            Text("120% Progress (Overflow & Flowers)")
            WaterGlass(currentAmount = 2400, dailyGoal = 2000)
            Spacer(modifier = Modifier.height(16.dp))

            Text("150% Progress (Capped at 120%)")
            WaterGlass(currentAmount = 3000, dailyGoal = 2000)
            Spacer(modifier = Modifier.height(16.dp))

            Text("No Goal Set")
            WaterGlass(currentAmount = 500, dailyGoal = 0)
        }
    }
}
