package cz.petrschopp.skladovysystem.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.petrschopp.skladovysystem.ui.theme.MovementInColor
import cz.petrschopp.skladovysystem.ui.theme.MovementNeutralColor
import cz.petrschopp.skladovysystem.ui.theme.MovementOutColor

@Composable
fun MovementIndicatorRow(
    direction: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .height(IntrinsicSize.Min)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(
                    color = movementIndicatorColor(direction),
                    shape = RoundedCornerShape(50)
                )
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            content()
        }
    }
}

private fun movementIndicatorColor(direction: String?) = when (direction) {
    "IN" -> MovementInColor
    "OUT" -> MovementOutColor
    else -> MovementNeutralColor
}