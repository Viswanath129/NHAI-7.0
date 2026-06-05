package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees")
    fun getAllEmployees(): Flow<List<EmployeeProfile>>

    @Query("SELECT * FROM employees")
    suspend fun getAllProfiles(): List<EmployeeProfile>

    @Query("SELECT * FROM employees WHERE employeeId = :id LIMIT 1")
    suspend fun getProfile(id: String): EmployeeProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: EmployeeProfile)

    @androidx.room.Update
    suspend fun update(employee: EmployeeProfile)

    @Query("DELETE FROM employees WHERE employeeId = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM employees WHERE fullName LIKE '%' || :searchQuery || '%' OR employeeId LIKE '%' || :searchQuery || '%'")
    fun searchEmployees(searchQuery: String): Flow<List<EmployeeProfile>>

    @Query("DELETE FROM employees")
    suspend fun deleteAll()
}
