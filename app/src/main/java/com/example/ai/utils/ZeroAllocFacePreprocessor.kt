package com.example.ai.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A zero-allocation (after initialization) preprocessor for face inference.
 * Reuses internal ByteBuffers and IntArrays to eliminate per-frame GC pressure.
 */
class ZeroAllocFacePreprocessor(
    val targetWidth: Int = 112,
    val targetHeight: Int = 112
) {
    // Reusable buffers
    private val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(4 * targetWidth * targetHeight * 3)
    private val intValues: IntArray = IntArray(targetWidth * targetHeight)
    
    // Reusable matrix and rects
    private val matrix = Matrix()
    private val safeRect = Rect()

    init {
        byteBuffer.order(ByteOrder.nativeOrder())
    }

    /**
     * Extracts the face from the original bitmap, scales it, normalizes to RGB [-1, 1],
     * and puts it into the reusable ByteBuffer.
     * 
     * Note: This method still creates a temporary Bitmap due to Android Canvas/Bitmap
     * scaling limitations, but avoids creating IntArrays and ByteBuffers per frame.
     * A true direct YUV to ByteBuffer pipeline requires native code (libyuv).
     */
    fun processFace(
        originalBitmap: Bitmap,
        boundingBox: Rect,
        rotationDegrees: Int
    ): ByteBuffer {
        byteBuffer.clear()

        val paddingX = (boundingBox.width() * 0.1).toInt()
        val paddingY = (boundingBox.height() * 0.1).toInt()

        safeRect.set(
            (boundingBox.left - paddingX).coerceAtLeast(0),
            (boundingBox.top - paddingY).coerceAtLeast(0),
            (boundingBox.right + paddingX).coerceAtMost(originalBitmap.width),
            (boundingBox.bottom + paddingY).coerceAtMost(originalBitmap.height)
        )

        val width = safeRect.width()
        val height = safeRect.height()
        
        if (width <= 0 || height <= 0) return byteBuffer

        // Ideally, we'd use a reusable Bitmap here, but Bitmap.createBitmap with source 
        // rect isn't easily reusable without drawing to a preallocated Canvas.
        // For Android Go compatibility, we use Canvas drawing onto a reused targetBitmap
        // if we fully optimize, but for now we reduce the buffer churn.
        val croppedBitmap = Bitmap.createBitmap(originalBitmap, safeRect.left, safeRect.top, width, height)

        matrix.reset()
        if (rotationDegrees != 0) {
            matrix.postRotate(rotationDegrees.toFloat(), width / 2f, height / 2f)
        }
        matrix.postScale(targetWidth.toFloat() / width, targetHeight.toFloat() / height)

        val scaledBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, width, height, matrix, true)
        
        scaledBitmap.getPixels(intValues, 0, targetWidth, 0, 0, targetWidth, targetHeight)

        for (pixelValue in intValues) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            byteBuffer.putFloat((r - 127.5f) / 128.0f)
            byteBuffer.putFloat((g - 127.5f) / 128.0f)
            byteBuffer.putFloat((b - 127.5f) / 128.0f)
        }

        if (croppedBitmap != originalBitmap) croppedBitmap.recycle()
        if (scaledBitmap != originalBitmap && scaledBitmap != croppedBitmap) scaledBitmap.recycle()

        byteBuffer.rewind()
        return byteBuffer
    }
}
