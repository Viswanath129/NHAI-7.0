package com.example.data

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE email = :email")
    suspend fun updatePassword(email: String, newPasswordHash: String)
}
