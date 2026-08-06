package com.toyrobotworkshop.auspex.ui.main

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.viewinterop.AndroidView

/**
 * SurfaceView-based camera preview with aspect ratio preservation.
 *
 * Replaces the previous TextureView implementation. SurfaceView writes directly to its own
 * native surface, bypassing the texture composition step that adds latency and CPU overhead.
 * This is significantly more efficient for high-resolution (4K) camera previews.
 *
 * The preview is constrained to the camera's native aspect ratio so the image is not stretched.
 * Black bars (letterbox/pillarbox) fill the remaining space.
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

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // Surface size changed — the native side handles this
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                onSurfaceDestroyed()
            }
        }
    }

    // Compute aspect ratio from camera resolution, accounting for device rotation.
    // When rotated 90° or 270°, width and height swap from the viewer's perspective.
    val aspectRatio = remember(cameraResolutionWidth, cameraResolutionHeight, deviceRotationDegrees) {
        if (cameraResolutionWidth != null && cameraResolutionHeight != null) {
            val isLandscape = deviceRotationDegrees in 45f..315f
            if (isLandscape) {
                cameraResolutionHeight.toFloat() / cameraResolutionWidth.toFloat()
            } else {
                cameraResolutionWidth.toFloat() / cameraResolutionHeight.toFloat()
            }
        } else {
            1f // fallback: square
        }
    }

    AndroidView(
        factory = { context ->
            SurfaceView(context).apply {
                holder.addCallback(callback)
            }
        },
        update = {},
        modifier = modifier
            .aspectRatio(aspectRatio),
    )
}
