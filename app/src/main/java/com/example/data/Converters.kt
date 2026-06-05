package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromFloatArray(value: FloatArray): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toFloatArray(value: String): FloatArray {
        if (value.isBlank()) return floatArrayOf()
        return try {
            value.split(",")
                .filter { it.isNotBlank() }
                .map { it.trim().toFloat() }
                .toFloatArray()
        } catch (e: Exception) {
            floatArrayOf()
        }
    }
}
