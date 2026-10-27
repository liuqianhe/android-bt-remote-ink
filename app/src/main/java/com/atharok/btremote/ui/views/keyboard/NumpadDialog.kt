package com.atharok.btremote.ui.views.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atharok.btremote.R
import com.atharok.btremote.common.utils.AppIcons
import com.atharok.btremote.common.utils.REMOTE_INPUT_NONE
import com.atharok.btremote.domain.entities.remoteInput.keyboard.KeyboardKey
import com.atharok.btremote.ui.components.AdaptiveText
import com.atharok.btremote.ui.components.DialogInkStyleToggle
import com.atharok.btremote.ui.components.DialogSystemBarAppearance
import com.atharok.btremote.ui.components.TextMedium
import com.atharok.btremote.ui.components.TextNormal
import com.atharok.btremote.ui.components.customButtons.RawButton
import com.atharok.btremote.ui.components.customButtons.TextKeyboardKey
import com.atharok.btremote.ui.theme.surfaceElevationHigh

const val NUMPAD_DEFAULT_OFFSET_FRACTION = 0.67f
private const val NUMPAD_HEIGHT_FRACTION = 0.65f

// 横屏时高度才是瓶颈：宽度按高度成比例收缩，保证键帽接近正方形、不会被压小压扁
private const val NUMPAD_LANDSCAPE_WIDTH_TO_HEIGHT_RATIO = 0.95f

@Composable
fun NumpadDialog(
    sendKeyboardKeyReport: (ByteArray) -> Unit,
    offsetFraction: Float,
    onOffsetFractionChange: (Float) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEInkTheme = MaterialTheme.colorScheme.background == Color.White
    var useInkStyle by rememberSaveable { mutableStateOf(isEInkTheme) }
    var isAdjusting by rememberSaveable { mutableStateOf(false) }
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
            val isLandscapeScreen = with(LocalConfiguration.current) {
                screenWidthDp > screenHeightDp
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(
                        dimensionResource(
                            id = if (isLandscapeScreen) R.dimen.padding_medium else R.dimen.padding_large
                        )
                    ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                if (isLandscapeScreen) {
                    // 横屏：标题和工具按钮合并成一行，把省下的高度全给键帽
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextMedium(
                            text = stringResource(id = R.string.numeric_keypad),
                            modifier = Modifier.weight(1f)
                        )

                        DialogInkStyleToggle(
                            inkStyle = useInkStyle,
                            onInkStyleChange = { useInkStyle = it }
                        )

                        if (isAdjusting) {
                            TextNormal(
                                text = stringResource(id = R.string.numpad_position_hint),
                                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                                maxLines = 1
                            )
                        }

                        NumpadAdjustButton(
                            isAdjusting = isAdjusting,
                            onClick = { isAdjusting = !isAdjusting }
                        )

                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = AppIcons.Close,
                                contentDescription = stringResource(id = R.string.close)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextMedium(
                            text = stringResource(id = R.string.numeric_keypad),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = AppIcons.Close,
                                contentDescription = stringResource(id = R.string.close)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DialogInkStyleToggle(
                            inkStyle = useInkStyle,
                            onInkStyleChange = { useInkStyle = it }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (isAdjusting) {
                            TextNormal(
                                text = stringResource(id = R.string.numpad_position_hint),
                                modifier = Modifier.padding(end = 4.dp),
                                maxLines = 1
                            )
                        }

                        NumpadAdjustButton(
                            isAdjusting = isAdjusting,
                            onClick = { isAdjusting = !isAdjusting }
                        )
                    }
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // 横屏：可用高度很有限，直接吃满高度，并把宽度按比例收窄居中，
                    // 让每行键帽都能长到接近正方形；竖屏保持原来的 65% 高度。
                    val isLandscape = maxWidth > maxHeight
                    val keypadHeight = if (isLandscape) {
                        maxHeight
                    } else {
                        maxHeight * NUMPAD_HEIGHT_FRACTION
                    }
                    val keypadWidth = if (isLandscape) {
                        minOf(maxWidth, keypadHeight * NUMPAD_LANDSCAPE_WIDTH_TO_HEIGHT_RATIO)
                    } else {
                        maxWidth
                    }
                    val freeSpace = (maxHeight - keypadHeight).coerceAtLeast(0.dp)
                    val keypadTop = freeSpace * offsetFraction.coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .width(keypadWidth)
                            .height(keypadHeight)
                            .offset(y = keypadTop)
                    ) {
                        NumpadLayout(
                            sendKeyboardKeyReport = sendKeyboardKeyReport,
                            inkStyle = useInkStyle,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (isAdjusting) {
                        NumpadDragArea(
                            offsetFraction = offsetFraction,
                            onOffsetFractionChange = onOffsetFractionChange,
                            freeSpace = freeSpace,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumpadAdjustButton(
    isAdjusting: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (isAdjusting) MaterialTheme.colorScheme.primary else Color.Transparent
            )
    ) {
        Icon(
            imageVector = AppIcons.Gesture,
            contentDescription = stringResource(id = R.string.numpad_adjust_position),
            tint = if (isAdjusting) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                LocalContentColor.current
            }
        )
    }
}

@Composable
private fun NumpadDragArea(
    offsetFraction: Float,
    onOffsetFractionChange: (Float) -> Unit,
    freeSpace: Dp,
    modifier: Modifier = Modifier
) {
    val currentOffsetFraction by rememberUpdatedState(offsetFraction)
    val currentOnOffsetFractionChange by rememberUpdatedState(onOffsetFractionChange)
    var startFraction by remember { mutableStateOf(0f) }
    var dragDistance by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier.pointerInput(freeSpace) {
            detectDragGestures(
                onDragStart = {
                    startFraction = currentOffsetFraction
                    dragDistance = 0f
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val freeSpacePx = freeSpace.toPx()
                    if (freeSpacePx > 0f) {
                        dragDistance += dragAmount.y
                        currentOnOffsetFractionChange(
                            (startFraction + dragDistance / freeSpacePx).coerceIn(0f, 1f)
                        )
                    }
                }
            )
        }
    ) { }
}

@Composable
private fun NumpadLayout(
    sendKeyboardKeyReport: (ByteArray) -> Unit,
    inkStyle: Boolean,
    modifier: Modifier = Modifier
) {
    val gap = dimensionResource(id = R.dimen.padding_small)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            NumpadKey(
                text = "NumLock",
                key = KeyboardKey.KEY_NUM_LOCK,
                sendKeyboardKeyReport = sendKeyboardKeyReport,
                inkStyle = inkStyle,
                modifier = Modifier.weight(1f)
            )
            NumpadKey(
                text = "/",
                key = KeyboardKey.KEY_NUM_DIVIDE,
                sendKeyboardKeyReport = sendKeyboardKeyReport,
                inkStyle = inkStyle,
                modifier = Modifier.weight(1f)
            )
            NumpadKey(
                text = "*",
                key = KeyboardKey.KEY_NUM_MULTIPLY,
                sendKeyboardKeyReport = sendKeyboardKeyReport,
                inkStyle = inkStyle,
                modifier = Modifier.weight(1f)
            )
            NumpadKey(
                text = "-",
                key = KeyboardKey.KEY_NUM_SUBTRACT,
                sendKeyboardKeyReport = sendKeyboardKeyReport,
                inkStyle = inkStyle,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                NumpadRow(
                    keys = arrayOf(
                        "7" to KeyboardKey.KEY_NUM_7,
                        "8" to KeyboardKey.KEY_NUM_8,
                        "9" to KeyboardKey.KEY_NUM_9
                    ),
                    sendKeyboardKeyReport = sendKeyboardKeyReport,
                    inkStyle = inkStyle,
                    modifier = Modifier.weight(1f)
                )
                NumpadRow(
                    keys = arrayOf(
                        "4" to KeyboardKey.KEY_NUM_4,
                        "5" to KeyboardKey.KEY_NUM_5,
                        "6" to KeyboardKey.KEY_NUM_6
                    ),
                    sendKeyboardKeyReport = sendKeyboardKeyReport,
                    inkStyle = inkStyle,
                    modifier = Modifier.weight(1f)
                )
                NumpadRow(
                    keys = arrayOf(
                        "1" to KeyboardKey.KEY_NUM_1,
                        "2" to KeyboardKey.KEY_NUM_2,
                        "3" to KeyboardKey.KEY_NUM_3
                    ),
                    sendKeyboardKeyReport = sendKeyboardKeyReport,
                    inkStyle = inkStyle,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    NumpadKey(
                        text = "0",
                        key = KeyboardKey.KEY_NUM_0,
                        sendKeyboardKeyReport = sendKeyboardKeyReport,
                        inkStyle = inkStyle,
                        modifier = Modifier.weight(2f)
                    )
                    NumpadKey(
                        text = ".",
                        key = KeyboardKey.KEY_NUM_DOT,
                        sendKeyboardKeyReport = sendKeyboardKeyReport,
                        inkStyle = inkStyle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                NumpadKey(
                    text = "+",
                    key = KeyboardKey.KEY_NUM_ADD,
                    sendKeyboardKeyReport = sendKeyboardKeyReport,
                    inkStyle = inkStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                )
                NumpadKey(
                    text = "Enter",
                    key = KeyboardKey.KEY_NUM_ENTER,
                    sendKeyboardKeyReport = sendKeyboardKeyReport,
                    inkStyle = inkStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                )
            }
        }
    }
}

@Composable
private fun NumpadRow(
    keys: Array<Pair<String, KeyboardKey>>,
    sendKeyboardKeyReport: (ByteArray) -> Unit,
    inkStyle: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
    ) {
        keys.forEach { (text, key) ->
            NumpadKey(
                text = text,
                key = key,
                sendKeyboardKeyReport = sendKeyboardKeyReport,
                inkStyle = inkStyle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NumpadKey(
    text: String,
    key: KeyboardKey,
    sendKeyboardKeyReport: (ByteArray) -> Unit,
    inkStyle: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(dimensionResource(id = R.dimen.keyboard_key_corner_radius))
    val touchDown = {
        sendKeyboardKeyReport(byteArrayOf(0x00, key.byte))
    }
    val touchUp = {
        sendKeyboardKeyReport(REMOTE_INPUT_NONE)
    }

    if (inkStyle) {
        RawButton(
            touchDown = touchDown,
            touchUp = touchUp,
            modifier = modifier,
            shape = shape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .border(2.dp, Color.Black, shape),
                contentAlignment = Alignment.Center
            ) {
                AdaptiveText(
                    text = text,
                    percent = 0.5f,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(id = R.dimen.padding_small)),
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        TextKeyboardKey(
            text = text,
            touchDown = touchDown,
            touchUp = touchUp,
            modifier = modifier,
            shape = shape,
            elevation = surfaceElevationHigh(),
            textHeightFraction = 0.4f
        )
    }
}
