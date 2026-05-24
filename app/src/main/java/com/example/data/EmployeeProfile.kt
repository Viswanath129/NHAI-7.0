package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employee_profile")
data class EmployeeProfile(
    @PrimaryKey val employeeId: String,
    val fullName: String,
    val department: String,
    val role: String,
    val contactNumber: String,
    val faceEmbedding: ByteArray // Encrypted embedding
)
