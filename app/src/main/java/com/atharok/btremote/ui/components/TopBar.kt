package com.atharok.btremote.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.atharok.btremote.R
import com.atharok.btremote.ui.theme.surfaceElevationHigh
import com.atharok.btremote.ui.theme.surfaceElevationLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigateUp: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            // 标题（设备名）与右侧操作按钮之间留出间距，长名称由 Ellipsis 截断，不会压到图标上。
            TextLarge(
                text = title,
                modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_medium))
            )
        },
        modifier = modifier,
        navigationIcon = navigateUp,
        actions = actions,
        windowInsets = TopAppBarDefaults.windowInsets,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                elevation = surfaceElevationLow()
            ),
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                elevation = surfaceElevationHigh()
            )
        ),
        scrollBehavior = scrollBehavior
    )
}