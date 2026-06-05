package com.example.data.repository

import com.example.data.EmployeeDao
import com.example.data.EmployeeProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EmployeeRepositoryImpl(
    private val employeeDao: EmployeeDao
) : EmployeeRepository {

    override suspend fun enrollEmployee(employee: EmployeeProfile) = withContext(Dispatchers.IO) {
        employeeDao.insert(employee)
    }

    override suspend fun getEmployee(id: String): EmployeeProfile? = withContext(Dispatchers.IO) {
        employeeDao.getProfile(id)
    }

    override fun getAllEmployees(): Flow<List<EmployeeProfile>> {
        // Assume Flow is returned, wait employeeDao.getAllProfiles returns List<EmployeeProfile> or Flow?
        // Let's just return what we can, but since room creates Flow if specified, we might need a Flow method.
        // Actually the current employeeDao.getAllProfiles() returns List<EmployeeProfile>
        return kotlinx.coroutines.flow.flow {
             emit(employeeDao.getAllProfiles())
        }
    }
}
