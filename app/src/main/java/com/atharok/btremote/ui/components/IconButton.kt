package com.atharok.btremote.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.atharok.btremote.R
import com.atharok.btremote.common.extensions.autoMirroredIcon
import com.atharok.btremote.common.utils.AppIcons

// ---- Basic IconButton ----

@Composable
fun BasicIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    shape: Shape = IconButtonDefaults.standardShape,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.autoMirroredIcon(icon)
        )
    }
}

// ---- Compact IconButton (tight top bar actions) ----

// 顶部工具栏图标按钮尺寸：比标准 48dp 更紧凑，5 个按钮占用的总宽度与原 4 个按钮相当，
// 这样设备名不会被挤得太窄，同时图标本身大小与原来接近。
val TopBarActionButtonSize: Dp = 38.dp
val TopBarActionIconSize: Dp = 22.dp

@Composable
fun CompactIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    buttonSize: Dp = TopBarActionButtonSize,
    iconSize: Dp = TopBarActionIconSize
) {
    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(iconSize)
                .autoMirroredIcon(icon)
        )
    }
}

// ---- Some common IconButtons ----

@Composable
fun NavigateUpIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicIconButton(
        onClick = onClick,
        icon = AppIcons.Back,
        contentDescription = stringResource(id = R.string.back),
        modifier = modifier
    )
}

@Composable
fun SettingsIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicIconButton(
        onClick = onClick,
        icon = AppIcons.Settings,
        contentDescription = stringResource(id = R.string.settings),
        modifier = modifier
    )
}

@Composable
fun HelpIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicIconButton(
        onClick = onClick,
        icon = AppIcons.Help,
        contentDescription = stringResource(id = R.string.help),
        modifier = modifier
    )
}