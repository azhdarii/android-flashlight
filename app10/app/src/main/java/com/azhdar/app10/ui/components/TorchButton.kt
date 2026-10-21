package com.azhdar.app10.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Large circular action button used for the torch and the strobe.
 *
 * The original implementation used raw `Image` composables with `clickable`, which meant no
 * ripple, no accessibility description and no disabled state. This version uses Material
 * icons, a proper ripple and a semantics description.
 */
@Composable
fun TorchActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
    size: Dp = 120.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val scale by animateFloatAsState(
        targetValue = if (active) 1.05f else 1f,
        label = "torchButtonScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.4f,
        label = "torchButtonAlpha",
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .alpha(alpha)
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        activeColor.copy(alpha = if (active) 0.95f else 0.45f),
                        inactiveColor.copy(alpha = 0.45f),
                    ),
                ),
                shape = CircleShape,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(size / 2.6f),
        )
    }
}

/** Icons used by the main screen, kept in one place for consistency. */
object TorchIcons {
    val TorchOn: ImageVector = Icons.Filled.FlashOn
    val TorchOff: ImageVector = Icons.Filled.FlashOff
    val Strobe: ImageVector = Icons.Filled.Highlight
}
