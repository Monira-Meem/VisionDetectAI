package com.visiondetect.ai.core.utils
class FpsCalculator(private val windowSize: Int = 30) {
    private val timestamps = ArrayDeque<Long>(windowSize)
    fun recordFrameAndGetFps(): Float {
        val now = System.currentTimeMillis()
        timestamps.addLast(now)
        while (timestamps.size > windowSize) timestamps.removeFirst()
        if (timestamps.size < 2) return 0f
        val elapsed = (timestamps.last() - timestamps.first()) / 1000f
        return if (elapsed <= 0f) 0f else (timestamps.size - 1) / elapsed
    }
    fun reset() = timestamps.clear()
}
