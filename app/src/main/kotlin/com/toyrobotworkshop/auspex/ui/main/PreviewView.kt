package com.toyrobotworkshop.auspex.ui.main

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView

/**
 * SurfaceView-based preview with pinch zoom and one-finger panning.
 * SurfaceView's own transform properties are used so the camera can continue
 * rendering directly to its native surface without a TextureView copy.
 */
@Composable
fun PreviewView(
    modifier: Modifier = Modifier,
    cameraResolutionWidth: Int?,
    cameraResolutionHeight: Int?,
    deviceRotationDegrees: Float,
    onSurfaceReady: (Surface) -> Unit,
    onSurfaceDestroyed: () -> Unit,
) {
    val callback = remember {
        object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                holder.surface?.let { onSurfaceReady(it) }
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                onSurfaceDestroyed()
            }
        }
    }

    val aspectRatio = remember(cameraResolutionWidth, cameraResolutionHeight, deviceRotationDegrees) {
        if (cameraResolutionWidth != null && cameraResolutionHeight != null) {
            val isLandscape = deviceRotationDegrees in 45f..315f
            if (isLandscape) cameraResolutionHeight.toFloat() / cameraResolutionWidth.toFloat()
            else cameraResolutionWidth.toFloat() / cameraResolutionHeight.toFloat()
        } else 1f
    }

    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, panChange, zoomChange, _ ->
                    val nextZoom = (zoom * zoomChange).coerceIn(1f, 8f)
                    zoom = nextZoom
                    val maxX = size.width * (nextZoom - 1f) / 2f
                    val maxY = size.height * (nextZoom - 1f) / 2f
                    pan = if (nextZoom <= 1.001f) Offset.Zero else Offset(
                        x = (pan.x + panChange.x).coerceIn(-maxX, maxX),
                        y = (pan.y + panChange.y).coerceIn(-maxY, maxY),
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        zoom = 1f
                        pan = Offset.Zero
                    }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { context ->
                SurfaceView(context).apply {
                    holder.addCallback(callback)
                    addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
                        view.pivotX = view.width / 2f
                        view.pivotY = view.height / 2f
                    }
                }
            },
            update = { view ->
                if (cameraResolutionWidth != null && cameraResolutionHeight != null) {
                    val frame = view.holder.surfaceFrame
                    if (frame.width() != cameraResolutionWidth || frame.height() != cameraResolutionHeight) {
                        view.holder.setFixedSize(cameraResolutionWidth, cameraResolutionHeight)
                    }
                }
                view.pivotX = view.width / 2f
                view.pivotY = view.height / 2f
                view.scaleX = zoom
                view.scaleY = zoom
                view.translationX = pan.x
                view.translationY = pan.y
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
        )
    }
}
