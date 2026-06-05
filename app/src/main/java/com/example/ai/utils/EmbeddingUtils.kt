package com.example.ai.utils

import kotlin.math.sqrt

object EmbeddingUtils {

    fun normalize(embedding: FloatArray): FloatArray {
        var norm = 0.0f
        for (v in embedding) norm += v * v
        norm = sqrt(norm)
        if (norm > 0) {
            for (i in embedding.indices) {
                embedding[i] /= norm
            }
        }
        return embedding
    }

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Embeddings must be of same length" }
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom == 0.0f) 0.0f else (dotProduct / denom)
    }

    fun isSimilar(a: FloatArray, b: FloatArray, threshold: Float = 0.40f): Boolean {
        return cosineSimilarity(a, b) >= threshold
    }
}
