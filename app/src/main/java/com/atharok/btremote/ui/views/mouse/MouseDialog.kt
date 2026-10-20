package com.atharok.btremote.ui.views.mouse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atharok.btremote.R
import com.atharok.btremote.common.utils.AppIcons
import com.atharok.btremote.domain.entities.remoteInput.MouseAction
import com.atharok.btremote.ui.components.DialogInkStyleToggle
import com.atharok.btremote.ui.components.DialogSystemBarAppearance
import com.atharok.btremote.ui.components.TextMedium

@Composable
fun MouseDialog(
    mouseSpeed: Float,
    scrollSpeed: Float,
    shouldInvertMouseScrollingDirection: Boolean,
    useGyroscope: Boolean,
    sendMouseInput: (MouseAction, Float, Float, Float) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEInkTheme = MaterialTheme.colorScheme.background == Color.White
    var useInkStyle by rememberSaveable { mutableStateOf(isEInkTheme) }
    val useDarkSystemBarIcons = if (useInkStyle) {
        true
    } else {
        MaterialTheme.colorScheme.background.luminance() > 0.5f
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        DialogSystemBarAppearance(useDarkIcons = useDarkSystemBarIcons)

        Surface(
            modifier = modifier.fillMaxSize(),
            color = if (useInkStyle) Color.White else MaterialTheme.colorScheme.background,
            contentColor = if (useInkStyle) Color.Black else MaterialTheme.colorScheme.onBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(dimensionResource(id = R.dimen.padding_large)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextMedium(
                        text = stringResource(id = R.string.mouse),
                        modifier = Modifier.weight(1f)
                    )

                    DialogInkStyleToggle(
                        inkStyle = useInkStyle,
                        onInkStyleChange = { useInkStyle = it }
                    )

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = stringResource(id = R.string.close)
                        )
                    }
                }

                MousePadLayout(
                    mouseSpeed = mouseSpeed,
                    scrollSpeed = scrollSpeed,
                    shouldInvertMouseScrollingDirection = shouldInvertMouseScrollingDirection,
                    useGyroscope = useGyroscope,
                    sendMouseInput = sendMouseInput,
                    inkStyle = useInkStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}
