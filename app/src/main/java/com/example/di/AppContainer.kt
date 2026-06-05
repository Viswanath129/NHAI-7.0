package com.example.di

import android.content.Context
import com.example.ai.tflite.MobileFaceNetEngine
import com.example.ai.tflite.SilentFaceEngine
import com.example.data.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.EmployeeRepositoryImpl
import com.example.data.security.CryptoManager
import com.example.data.security.SessionManager
import com.example.domain.ai.FaceRecognitionEngine
import com.example.domain.ai.LivenessEngine

interface AppContainer {
    val database: AppDatabase
    val cryptoManager: CryptoManager
    val sessionManager: SessionManager
    val faceRecognitionEngine: FaceRecognitionEngine
    val livenessEngine: LivenessEngine
    val authRepository: AuthRepository
    val employeeRepository: EmployeeRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val cryptoManager: CryptoManager by lazy {
        CryptoManager()
    }

    override val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    override val faceRecognitionEngine: FaceRecognitionEngine by lazy {
        MobileFaceNetEngine(context).apply { initialize() }
    }

    override val livenessEngine: LivenessEngine by lazy {
        SilentFaceEngine(context).apply { initialize() }
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            employeeDao = database.employeeDao(),
            auditLogDao = database.auditLogDao(),
            sessionManager = sessionManager,
            faceRecognitionEngine = faceRecognitionEngine
        )
    }

    override val employeeRepository: EmployeeRepository by lazy {
        EmployeeRepositoryImpl(database.employeeDao())
    }
}
