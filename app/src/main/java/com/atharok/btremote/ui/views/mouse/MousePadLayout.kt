package com.atharok.btremote.ui.views.mouse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.atharok.btremote.R
import com.atharok.btremote.common.utils.AppIcons
import com.atharok.btremote.common.utils.Ref
import com.atharok.btremote.domain.entities.remoteInput.MouseAction
import com.atharok.btremote.ui.components.TextNormal
import com.atharok.btremote.ui.components.customButtons.EmptySurfaceButton
import com.atharok.btremote.ui.components.customButtons.IconHoldButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MousePadLayout(
    mouseSpeed: Float,
    scrollSpeed: Float,
    shouldInvertMouseScrollingDirection: Boolean,
    useGyroscope: Boolean,
    sendMouseInput: (MouseAction, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    inkStyle: Boolean = false
) {
    val mouseAction: Ref<MouseAction> = remember { Ref(MouseAction.NONE) }

    if(useGyroscope) {
        MouseGyroscope(
            mouseSpeed = mouseSpeed,
            onMousePositionChange = { x: Float, y: Float ->
                sendMouseInput(mouseAction.value, x, y, 0f)
            }
        )
    }

    Column(modifier) {
        Row(
            modifier = Modifier
                .weight(0.8f)
                .padding(bottom = dimensionResource(id = R.dimen.padding_min))
        ) {
            MousePad(
                mouseSpeed = mouseSpeed,
                scrollSpeed = scrollSpeed,
                sendMouseAction = {
                    mouseAction.value = it
                    sendMouseInput(mouseAction.value, 0f, 0f, 0f)
                },
                sendMousePosition = { x: Float, y: Float ->
                    sendMouseInput(mouseAction.value, x, y, 0f)
                },
                sendMouseScroll = { wheel: Float ->
                    val mouseWheel = wheel * if(shouldInvertMouseScrollingDirection) -1f else 1f
                    sendMouseInput(mouseAction.value, 0f, 0f, mouseWheel)
                },
                shape = RoundedCornerShape(
                    topStart = dimensionResource(id = R.dimen.card_corner_radius),
                    topEnd = 0.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 0.dp
                ),
                modifier = Modifier
                    .weight(0.85f)
                    .padding(end = dimensionResource(id = R.dimen.padding_min)),
                inkStyle = inkStyle
            )

            ScrollMouseButtonsLayout(
                scrollSpeed = scrollSpeed,
                sendMouseScroll = {
                    sendMouseInput(mouseAction.value, 0f, 0f, it)
                },
                modifier = Modifier
                    .weight(0.15f)
                    .padding(start = dimensionResource(id = R.dimen.padding_min)),
                inkStyle = inkStyle
            )
        }

        MouseButtonsLayout(
            sendMouseAction = {
                mouseAction.value = it
                sendMouseInput(mouseAction.value, 0f, 0f, 0f)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f)
                .padding(top = dimensionResource(id = R.dimen.padding_min)),
            inkStyle = inkStyle
        )
    }
}

// ---- Mouse buttons ----

@Composable
private fun MouseButtonsLayout(
    sendMouseAction: (MouseAction) -> Unit,
    modifier: Modifier = Modifier,
    inkStyle: Boolean = false
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Absolute.Left) {

        // Start
        MouseButton(
            text = stringResource(id = R.string.mouse_click_left),
            touchDown = { sendMouseAction(MouseAction.MOUSE_CLICK_LEFT) },
            touchUp = { sendMouseAction(MouseAction.NONE) },
            inkStyle = inkStyle,
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight()
                .absolutePadding(right = dimensionResource(id = R.dimen.padding_min)),
            shape = AbsoluteRoundedCornerShape(
                topLeft = 0.dp,
                topRight = 0.dp,
                bottomRight = 0.dp,
                bottomLeft = dimensionResource(id = R.dimen.card_corner_radius)
            )
        )

        // Center
        MouseButton(
            text = stringResource(id = R.string.mouse_click_middle),
            touchDown = { sendMouseAction(MouseAction.MOUSE_CLICK_MIDDLE) },
            touchUp = { sendMouseAction(MouseAction.NONE) },
            inkStyle = inkStyle,
            modifier = Modifier
                .weight(0.24f)
                .fillMaxHeight()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_min)
                ),
            shape = RectangleShape
        )

        // End
        MouseButton(
            text = stringResource(id = R.string.mouse_click_right),
            touchDown = { sendMouseAction(MouseAction.MOUSE_CLICK_RIGHT) },
            touchUp = { sendMouseAction(MouseAction.NONE) },
            inkStyle = inkStyle,
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight()
                .absolutePadding(left = dimensionResource(id = R.dimen.padding_min)),
            shape = AbsoluteRoundedCornerShape(
                topLeft = 0.dp,
                topRight = 0.dp,
                bottomRight = dimensionResource(id = R.dimen.card_corner_radius),
                bottomLeft = 0.dp
            )
        )
    }
}

@Composable
private fun MouseButton(
    text: String,
    touchDown: () -> Unit,
    touchUp: () -> Unit,
    inkStyle: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape
) {
    if (inkStyle) {
        InkMouseButton(
            text = text,
            touchDown = touchDown,
            touchUp = touchUp,
            modifier = modifier,
            shape = shape
        )
    } else {
        EmptySurfaceButton(
            touchDown = touchDown,
            touchUp = touchUp,
            modifier = modifier,
            shape = shape
        )
    }
}

@Composable
private fun InkMouseButton(
    text: String,
    touchDown: () -> Unit,
    touchUp: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    touchDown()
                }
                is PressInteraction.Release -> touchUp()
                is PressInteraction.Cancel -> touchUp()
            }
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (isPressed) Color.Black else Color.White)
            .border(2.dp, Color.Black, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        TextNormal(
            text = text,
            color = if (isPressed) Color.White else Color.Black
        )
    }
}

// ---- Scroll Up/Down buttons ----

@Composable
private fun ScrollMouseButtonsLayout(
    scrollSpeed: Float,
    sendMouseScroll: (Float) -> Unit,
    modifier: Modifier = Modifier,
    inkStyle: Boolean = false
) {
    Column(modifier = modifier) {
        if (inkStyle) {
            InkIconHoldButton(
                image = AppIcons.MouseScrollUp,
                contentDescription = stringResource(id = R.string.mouse_wheel_up),
                onHold = {
                    sendMouseScroll(1f)
                },
                onRelease = {
                    sendMouseScroll(0f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .padding(bottom = dimensionResource(id = R.dimen.padding_min)),
                durationBetweenRepeatsInMillis = getScrollDurationBetweenRepeats(scrollSpeed),
                shape = RoundedCornerShape(topEnd = dimensionResource(id = R.dimen.card_corner_radius))
            )
        } else {
            IconHoldButton(
                image = AppIcons.MouseScrollUp,
                contentDescription = stringResource(id = R.string.mouse_wheel_up),
                onHold = {
                    sendMouseScroll(1f)
                },
                onRelease = {
                    sendMouseScroll(0f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .padding(bottom = dimensionResource(id = R.dimen.padding_min)),
                durationBetweenRepeatsInMillis = getScrollDurationBetweenRepeats(scrollSpeed),
                shape = RoundedCornerShape(topEnd = dimensionResource(id = R.dimen.card_corner_radius))
            )
        }

        if (inkStyle) {
            InkIconHoldButton(
                image = AppIcons.MouseScrollDown,
                contentDescription = stringResource(id = R.string.mouse_wheel_down),
                onHold = {
                    sendMouseScroll(-1f)
                },
                onRelease = {
                    sendMouseScroll(0f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .padding(top = dimensionResource(id = R.dimen.padding_min)),
                durationBetweenRepeatsInMillis = getScrollDurationBetweenRepeats(scrollSpeed),
                shape = RectangleShape
            )
        } else {
            IconHoldButton(
                image = AppIcons.MouseScrollDown,
                contentDescription = stringResource(id = R.string.mouse_wheel_down),
                onHold = {
                    sendMouseScroll(-1f)
                },
                onRelease = {
                    sendMouseScroll(0f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .padding(top = dimensionResource(id = R.dimen.padding_min)),
                durationBetweenRepeatsInMillis = getScrollDurationBetweenRepeats(scrollSpeed),
                shape = RectangleShape
            )
        }
    }
}

@Composable
private fun InkIconHoldButton(
    image: ImageVector,
    contentDescription: String,
    onHold: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
    durationBetweenRepeatsInMillis: Long = 100L,
    shape: Shape = RectangleShape
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (isPressed) Color.Black else Color.White)
            .border(2.dp, Color.Black, shape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    val job = coroutineScope.launch {
                        while (true) {
                            onHold()
                            delay(durationBetweenRepeatsInMillis)
                        }
                    }

                    waitForUpOrCancellation()
                    job.cancel()
                    isPressed = false
                    onRelease()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = image,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(0.5f),
            tint = if (isPressed) Color.White else Color.Black
        )
    }
}

private fun getScrollDurationBetweenRepeats(speed: Float): Long = speed.takeIf { it > 0f }?.let { (100f / it).toLong().coerceIn(16L, 200L) } ?: 100L