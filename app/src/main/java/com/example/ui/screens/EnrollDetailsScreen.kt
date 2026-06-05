package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollDetailsScreen(navController: NavController, viewModel: EnrollViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AGENT ENROLLMENT") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.employeeId,
                onValueChange = { viewModel.employeeId = it },
                label = { Text("Employee ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.fullName,
                onValueChange = { viewModel.fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.department,
                onValueChange = { viewModel.department = it },
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.role,
                onValueChange = { viewModel.role = it },
                label = { Text("Designation / Role") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.contactNumber,
                onValueChange = { viewModel.contactNumber = it },
                label = { Text("Contact Number") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    navController.navigate("enroll_prep") 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = viewModel.fullName.isNotBlank() && viewModel.employeeId.isNotBlank()
            ) {
                Text("CONTINUE TO BIOMETRIC CAPTURE")
            }
        }
    }
}
