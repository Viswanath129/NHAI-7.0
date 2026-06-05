package com.example.ai.utils

import android.graphics.PointF
import kotlin.math.hypot

object EARCalculator {
    fun calculateEAR(eyeContourPoints: List<PointF>): Float {
        if (eyeContourPoints.size < 16) return 0.0f

        // ML Kit Eye Contours have 16 points.
        // Assuming 0 is leftmost, 8 is rightmost.
        // 4 is top-left, 6 is top-right, 12 is bottom-left, 10 is bottom-right.
        val p1 = eyeContourPoints[0]
        val p2 = eyeContourPoints[4]
        val p3 = eyeContourPoints[6]
        val p4 = eyeContourPoints[8]
        val p5 = eyeContourPoints[10]
        val p6 = eyeContourPoints[12]

        val v1 = distance(p2, p6)
        val v2 = distance(p3, p5)
        val h = distance(p1, p4)

        return if (h == 0f) 0f else (v1 + v2) / (2.0f * h)
    }

    private fun distance(p1: PointF, p2: PointF): Float {
        return hypot(p1.x - p2.x, p1.y - p2.y)
    }
}
