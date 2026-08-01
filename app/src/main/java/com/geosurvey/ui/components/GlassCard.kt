// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/ui/components/GlassCard.kt
package com.geosurvey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.geosurvey.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = TextMuted.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceGlass,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceGlass.copy(alpha = 0.9f),
                        BackgroundCard.copy(alpha = 0.95f)
                    )
                )
            )
        ) {
            content()
        }
    }
}
