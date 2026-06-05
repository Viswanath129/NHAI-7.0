package com.example.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.EmployeeProfile
import com.example.data.repository.EmployeeRepository
import kotlinx.coroutines.launch

class EnrollViewModel(private val employeeRepository: EmployeeRepository) : ViewModel() {
    // Enrollment Details
    var employeeId by mutableStateOf("")
    var fullName by mutableStateOf("")
    var department by mutableStateOf("")
    var role by mutableStateOf("")
    var contactNumber by mutableStateOf("")
    var capturedEmbedding by mutableStateOf<FloatArray?>(null)
    var lastError by mutableStateOf("")

    fun saveEmployeeProfile(embedding: FloatArray, onComplete: () -> Unit) {
        viewModelScope.launch {
            employeeRepository.enrollEmployee(
                EmployeeProfile(
                    employeeId = if (employeeId.isNotBlank()) employeeId else "EMP-${System.currentTimeMillis()}",
                    fullName = if (fullName.isNotBlank()) fullName else "Operator " + (1000..9999).random(),
                    department = if (department.isNotBlank()) department else "Security",
                    role = if (role.isNotBlank()) role else "Field Agent",
                    contactNumber = if (contactNumber.isNotBlank()) contactNumber else "555-0000",
                    faceEmbedding = embedding
                )
            )
            onComplete()
        }
    }
}

class EnrollViewModelFactory(private val employeeRepository: EmployeeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EnrollViewModel(employeeRepository) as T
    }
}
