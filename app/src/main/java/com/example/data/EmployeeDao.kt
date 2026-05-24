package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EmployeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: EmployeeProfile)

    @Query("SELECT * FROM employee_profile WHERE employeeId = :employeeId")
    suspend fun getProfile(employeeId: String): EmployeeProfile?

    @Query("SELECT * FROM employee_profile")
    suspend fun getAllProfiles(): List<EmployeeProfile>

    @Query("DELETE FROM employee_profile")
    suspend fun deleteAll()
}
