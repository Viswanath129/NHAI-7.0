package com.example.ai.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect

object FacePreprocessor {
    fun cropAndAlignFace(originalBitmap: Bitmap, boundingBox: Rect): Bitmap {
        val paddingX = (boundingBox.width() * 0.1).toInt()
        val paddingY = (boundingBox.height() * 0.1).toInt()

        val left = (boundingBox.left - paddingX).coerceAtLeast(0)
        val top = (boundingBox.top - paddingY).coerceAtLeast(0)
        val right = (boundingBox.right + paddingX).coerceAtMost(originalBitmap.width)
        val bottom = (boundingBox.bottom + paddingY).coerceAtMost(originalBitmap.height)
        val safeRect = Rect(left, top, right, bottom)

        val width = safeRect.width()
        val height = safeRect.height()
        
        if (width <= 0 || height <= 0) return originalBitmap

        return Bitmap.createBitmap(originalBitmap, safeRect.left, safeRect.top, width, height)
    }
}
