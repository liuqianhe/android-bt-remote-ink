package com.atharok.btremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.atharok.btremote.R

@Composable
internal fun DialogInkStyleToggle(
    inkStyle: Boolean,
    onInkStyleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DialogInkStyleToggleItem(
            text = stringResource(id = R.string.numpad_style_normal),
            selected = !inkStyle,
            inkStyle = inkStyle,
            onClick = { onInkStyleChange(false) }
        )
        DialogInkStyleToggleItem(
            text = stringResource(id = R.string.numpad_style_ink),
            selected = inkStyle,
            inkStyle = inkStyle,
            onClick = { onInkStyleChange(true) }
        )
    }
}

@Composable
private fun DialogInkStyleToggleItem(
    text: String,
    selected: Boolean,
    inkStyle: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    val containerColor = when {
        selected && inkStyle -> Color.Black
        selected -> MaterialTheme.colorScheme.primary
        inkStyle -> Color.White
        else -> Color.Transparent
    }
    val contentColor = when {
        selected && inkStyle -> Color.White
        selected -> MaterialTheme.colorScheme.onPrimary
        inkStyle -> Color.Black
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderColor = if (inkStyle) Color.Black else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        TextNormal(
            text = text,
            color = contentColor
        )
    }
}
