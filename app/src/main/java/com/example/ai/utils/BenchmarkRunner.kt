package com.example.ai.utils

import android.util.Log

object BenchmarkRunner {
    private const val TAG = "Benchmark"
    
    data class BenchmarkResult(
        val faceDetectionMs: Long = 0,
        val preprocessingMs: Long = 0,
        val embeddingMs: Long = 0,
        val livenessMs: Long = 0,
        val totalMs: Long = 0
    )

    private val latencies = mutableListOf<BenchmarkResult>()

    fun record(result: BenchmarkResult) {
        latencies.add(result)
        Log.d(TAG, "Frame stats: $result")
    }

    fun report() {
        if (latencies.isEmpty()) {
            Log.d(TAG, "No benchmark data collected.")
            return
        }

        val avgTotal = latencies.map { it.totalMs }.average()
        val avgDetection = latencies.map { it.faceDetectionMs }.average()
        val avgPreprocess = latencies.map { it.preprocessingMs }.average()
        val avgEmbedding = latencies.map { it.embeddingMs }.average()
        val avgLiveness = latencies.map { it.livenessMs }.average()
        val fps = if (avgTotal > 0) 1000.0 / avgTotal else 0.0

        Log.i(TAG, """
            ==== BENCHMARK RESULTS ====
            Total Frames: ${latencies.size}
            Average FPS: ${"%.2f".format(fps)}
            Avg Total Latency: ${"%.2f".format(avgTotal)} ms
            Avg Detection Latency: ${"%.2f".format(avgDetection)} ms
            Avg Preprocessing Latency: ${"%.2f".format(avgPreprocess)} ms
            Avg Embedding Latency: ${"%.2f".format(avgEmbedding)} ms
            Avg Liveness Latency: ${"%.2f".format(avgLiveness)} ms
            ===========================
        """.trimIndent())
    }
    
    fun clear() {
        latencies.clear()
    }
}
