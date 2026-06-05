package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val employees by db.employeeDao().getAllEmployees().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredEmployees = if (searchQuery.isBlank()) {
        employees
    } else {
        employees.filter { 
            it.fullName.contains(searchQuery, ignoreCase = true) || 
            it.employeeId.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ENROLLED AGENTS") },
                actions = {
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) { db.employeeDao().deleteAll() }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search by name, ID, or dept...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEmployees) { employee ->
                    AgentCard(employee, onDelete = {
                        scope.launch(Dispatchers.IO) { db.employeeDao().delete(employee.employeeId) }
                    }, onUpdate = { updatedEmployee ->
                        scope.launch(Dispatchers.IO) { db.employeeDao().update(updatedEmployee) }
                    })
                }
            }
        }
    }
}

@Composable
fun AgentCard(employee: com.example.data.EmployeeProfile, onDelete: () -> Unit, onUpdate: (com.example.data.EmployeeProfile) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
        var editName by remember { mutableStateOf(employee.fullName) }
        var editDept by remember { mutableStateOf(employee.department) }
        var editRole by remember { mutableStateOf(employee.role) }
        var editContact by remember { mutableStateOf(employee.contactNumber) }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editDept, onValueChange = { editDept = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editRole, onValueChange = { editRole = it }, label = { Text("Role") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editContact, onValueChange = { editContact = it }, label = { Text("Contact") }, modifier = Modifier.fillMaxWidth())
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { isEditing = false }) { Text("CANCEL") }
                    Button(onClick = { 
                        onUpdate(employee.copy(fullName = editName, department = editDept, role = editRole, contactNumber = editContact))
                        isEditing = false 
                    }) { Text("SAVE") }
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(employee.fullName, style = MaterialTheme.typography.titleLarge)
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = employee.role,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text("ID: ${employee.employeeId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Department", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(employee.department, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Contact", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(employee.contactNumber, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
