package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeProfile(
    @PrimaryKey val employeeId: String,
    val fullName: String,
    val department: String,
    val role: String,
    val contactNumber: String,
    val faceEmbedding: FloatArray, // 128 or 512 dimensions for typical models
    val enrollmentDate: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EmployeeProfile
        return employeeId == other.employeeId
    }

    override fun hashCode(): Int = employeeId.hashCode()
}
