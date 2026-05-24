package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.StatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentDetailsScreen(navController: NavController) {
    var employeeId by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }

    val isFormValid = employeeId.isNotBlank() && fullName.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Registration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            }

            EnrollmentTextField(
                label = "EMPLOYEE ID",
                value = employeeId,
                onValueChange = { employeeId = it },
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
            
            EnrollmentTextField(
                label = "FULL NAME",
                value = fullName,
                onValueChange = { fullName = it },
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
            
            EnrollmentTextField(
                label = "DEPARTMENT",
                value = department,
                onValueChange = { department = it },
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            EnrollmentTextField(
                label = "ROLE",
                value = role,
                onValueChange = { role = it },
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            EnrollmentTextField(
                label = "CONTACT NUMBER",
                value = contactNumber,
                onValueChange = { contactNumber = it },
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate("enroll_camera") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = isFormValid
            ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(text = "PROCEED TO BIOMETRIC SCAN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction
) {
    Column {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            singleLine = true
        )
    }
}
