package app.schedula.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.data.model.FamilyMember
import app.schedula.ui.theme.SchedulaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    onBackClick: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    SchedulaTheme(darkTheme = false) {
        val familyMembers by profileViewModel.familyMembers.collectAsState()

        var showAddDialog by remember { mutableStateOf(false) }
        var memberToEdit by remember { mutableStateOf<FamilyMember?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Family Members") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    actions = {
                        var expanded by remember { mutableStateOf(false) }

                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, null)
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Add Member") },
                                    leadingIcon = {
                                        Icon(Icons.Default.PersonAdd, null)
                                    },
                                    onClick = {
                                        expanded = false
                                        showAddDialog = true
                                    }
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(familyMembers) { member ->
                    FamilyMemberItem(
                        member = member,
                        onEdit = {
                            memberToEdit = member
                        },
                        onDelete = {
                            profileViewModel.deleteFamilyMember(member.id)
                        }
                    )
                }
            }
        }

        // Add Dialog
        if (showAddDialog) {
            AddOrEditFamilyMemberDialog(
                title = "Add Family Member",
                onDismiss = { showAddDialog = false },
                onSave = { name, relation, age, gender ->
                    profileViewModel.addFamilyMember(name, relation, age, gender)
                    showAddDialog = false
                }
            )
        }

        // Edit Dialog
        memberToEdit?.let { member ->
            AddOrEditFamilyMemberDialog(
                title = "Edit Family Member",
                initialName = member.name ?: "",
                initialRelation = member.relation ?: "",
                initialAge = member.age?.toString() ?: "",
                initialGender = member.gender ?: "",
                onDismiss = { memberToEdit = null },
                onSave = { name, relation, age, gender ->
                    profileViewModel.updateFamilyMember(
                        member.id,
                        name,
                        relation,
                        age,
                        gender
                    )
                    memberToEdit = null
                }
            )
        }
    }
}

@Composable
fun FamilyMemberItem(
    member: FamilyMember,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Name: ${member.name ?: ""}", style = MaterialTheme.typography.titleMedium)
                Text("Relation: ${member.relation ?: ""}", style = MaterialTheme.typography.bodySmall)
                Text("Age: ${member.age ?: ""}", style = MaterialTheme.typography.bodySmall)
                Text("Gender: ${member.gender ?: ""}", style = MaterialTheme.typography.bodySmall)
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, null)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, null)
                        },
                        onClick = {
                            expanded = false
                            onEdit()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null)
                        },
                        onClick = {
                            expanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddOrEditFamilyMemberDialog(
    title: String,
    initialName: String = "",
    initialRelation: String = "",
    initialAge: String = "",
    initialGender: String = "",
    onDismiss: () -> Unit,
    onSave: (String, String, Int, String) -> Unit
) {

    var name by remember { mutableStateOf(initialName) }
    var relation by remember { mutableStateOf(initialRelation) }
    var age by remember { mutableStateOf(initialAge) }
    var gender by remember { mutableStateOf(initialGender) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(name, relation, age.toIntOrNull() ?: 0, gender) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("Relation") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender") }
                )
            }
        }
    )
}
