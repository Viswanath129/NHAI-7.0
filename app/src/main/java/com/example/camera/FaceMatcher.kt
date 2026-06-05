package com.example.camera

import kotlin.math.sqrt

data class MatchResult(
    val employee: com.example.data.EmployeeProfile?,
    val score: Float
)

object FaceMatcher {
    // Typical threshold for cosine similarity (0.6 - 0.8 depending on model)
    private const val MATCH_THRESHOLD = 0.75f

    /**
     * Finds the best match for the given embedding among all enrolled employees.
     */
    fun findBestMatch(
        scannedEmbedding: FloatArray,
        allEmployees: List<com.example.data.EmployeeProfile>
    ): MatchResult {
        var bestMatch: com.example.data.EmployeeProfile? = null
        var bestScore = -1f

        for (employee in allEmployees) {
            val similarity = cosineSimilarity(scannedEmbedding, employee.faceEmbedding)
            
            // Log for debugging (similar to the user's debug code suggestion)
            android.util.Log.d("FaceMatcher", "Comparing with ${employee.fullName}: score $similarity")

            if (similarity > bestScore) {
                bestScore = similarity
                bestMatch = employee
            }
        }

        return if (bestScore >= MATCH_THRESHOLD) {
            MatchResult(bestMatch, bestScore)
        } else {
            MatchResult(null, bestScore)
        }
    }

    private fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.size != v2.size || v1.isEmpty()) return 0.0f
        
        var dotProduct = 0.0f
        var norm1 = 0.0f
        var norm2 = 0.0f
        
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            norm1 += v1[i] * v1[i]
            norm2 += v2[i] * v2[i]
        }
        
        if (norm1 <= 0.0f || norm2 <= 0.0f) return 0.0f
        
        return dotProduct / (sqrt(norm1) * sqrt(norm2))
    }
}
