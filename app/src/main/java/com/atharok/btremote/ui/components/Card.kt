package com.atharok.btremote.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.atharok.btremote.R
import com.atharok.btremote.ui.theme.surfaceElevationMedium
import com.atharok.btremote.ui.theme.surfaceElevationShadow

@Composable
fun DefaultElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(dimensionResource(id = R.dimen.card_corner_radius)),
    elevation: Dp = surfaceElevationMedium(),
    shadowElevation: Dp = surfaceElevationShadow(),
    content: @Composable () -> Unit
) {
    val isEInkTheme = MaterialTheme.colorScheme.background == Color.White

    ElevatedCard(
        modifier = modifier.then(
            if (isEInkTheme) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
            else Modifier
        ),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = shadowElevation
        )
    ) {
        content()
    }
}