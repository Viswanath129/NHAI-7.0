package com.example.data.repository

import com.example.data.EmployeeProfile
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    suspend fun enrollEmployee(employee: EmployeeProfile)
    suspend fun getEmployee(id: String): EmployeeProfile?
    fun getAllEmployees(): Flow<List<EmployeeProfile>>
}
