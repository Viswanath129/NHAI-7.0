package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [EmployeeProfile::class, User::class, AuditLog::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun userDao(): UserDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val keyManager = com.example.data.security.DatabaseKeyManager(context)
                val passphrase = keyManager.getDatabasePassphrase()
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nhai_auth_db"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
