package com.atharok.btremote.ui.components

import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.awaitCancellation

@Composable
internal fun DialogSystemBarAppearance(useDarkIcons: Boolean) {
    val view = LocalView.current

    LaunchedEffect(useDarkIcons, view) {
        while (!view.isAttachedToWindow) {
            withFrameNanos { }
        }
        withFrameNanos { }

        val appearanceMask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = view.windowInsetsController ?: return@LaunchedEffect
            val appearance = if (useDarkIcons) appearanceMask else 0

            controller.setSystemBarsAppearance(appearance, appearanceMask)
            try {
                awaitCancellation()
            } finally {
                controller.setSystemBarsAppearance(0, appearanceMask)
            }
        } else {
            val rootView = view.rootView
            val flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            val previous = rootView.systemUiVisibility

            rootView.systemUiVisibility = if (useDarkIcons) {
                previous or flags
            } else {
                previous and flags.inv()
            }
            try {
                awaitCancellation()
            } finally {
                rootView.systemUiVisibility = previous
            }
        }
    }
}
