package com.toyrobotworkshop.auspex.camera

import java.util.concurrent.atomic.AtomicReference

/**
 * Lock-free latest-frame-only buffer using an atomic reference swap.
 *
 * The producer calls [offer] with each new frame; the buffer atomically replaces
 * the current slot and immediately invokes [onEvict] on the displaced frame so
 * native resources (e.g. android.media.Image) are released without leaking.
 *
 * The consumer calls [poll] to retrieve and clear the current frame, or [peek]
 * to read it without consuming. Each frame is consumed at most once via [poll].
 *
 * This is the "capture thread writes newest frame, render thread reads current"
 * pattern from issue #34 — no queue, no backlog, always the freshest frame.
 *
 * @param T the frame type
 * @param onEvict called with the evicted frame when a new one displaces it, or
 *                when [clear] is called. Use to close/release native frames.
 */
class LatestFrameBuffer<T>(
    private val onEvict: ((T) -> Unit)? = null,
) {
    private val slot = AtomicReference<T?>(null)

    /**
     * Offer a new frame. Atomically replaces whatever is in the slot.
     * The displaced frame (if any) is passed to [onEvict] immediately.
     */
    fun offer(frame: T) {
        val evicted = slot.getAndSet(frame)
        evicted?.let { onEvict?.invoke(it) }
    }

    /**
     * Poll the current frame, atomically clearing the slot.
     * Returns null if no frame is available.
     * The caller is responsible for releasing the returned frame after use.
     */
    fun poll(): T? = slot.getAndSet(null)

    /**
     * Peek at the current frame without consuming it.
     * The frame remains in the slot — a subsequent [offer] or [clear] may displace it.
     */
    fun peek(): T? = slot.get()

    /**
     * Discard the currently buffered frame, invoking [onEvict] if set.
     */
    fun clear() {
        val evicted = slot.getAndSet(null)
        evicted?.let { onEvict?.invoke(it) }
    }
}
