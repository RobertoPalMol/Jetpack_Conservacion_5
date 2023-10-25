package com.example.jetpack_conservacion_5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val defaultTasks = List(20) { index ->
                Task(id = index,
                    name = "Tarea ${index + 1}",
                    isCompleted = false,
                    priority = Priority.LOW,
                    estimatedHours = 0,
                    actualHours = 0,
                    dueDate = "",
                    completionDate = "")
            }
            val tasks = remember { mutableStateListOf<Task>() }
            tasks.addAll(defaultTasks)
            MyApp(tasks)
        }
    }
}

data class Task(
    val id: Int,
    val name: String,
    var isCompleted: Boolean,
    var priority: Priority,  // Cambiado para que la prioridad sea modificable
    val estimatedHours: Int,
    val actualHours: Int,
    val dueDate: String,
    val completionDate: String
)

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

@Composable
fun TaskItem(task: Task, onUpdateTask: (Task) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(task.name) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(getBackgroundColorForPriority(priority = task.priority))
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { updated ->
                onUpdateTask(task.copy(isCompleted = updated))
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (editing) {
            BasicTextField(
                value = editedName,
                onValueChange = { updatedName ->
                    editedName = updatedName
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        editing = false
                        if (editedName.isNotBlank()) {
                            onUpdateTask(task.copy(name = editedName))
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White)
            )
        } else {
            Text(
                text = editedName + "\n" +
                        "Prioridad: ${task.priority}" + "\n" +
                        "Horas estimadas: ${task.estimatedHours}" + "\n",
                modifier = Modifier.weight(1f).clickable { editing = true }
            )
        }

        IconButton(
            onClick = {
                ShowPriorityDialog(task = task, onUpdateTask = onUpdateTask)
            }
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Cambiar Prioridad")
        }
    }
}

@Composable
fun ShowPriorityDialog(task: Task, onUpdateTask: (Task) -> Unit) {
    val context = LocalContext.current
    var selectedPriority by remember { mutableStateOf(task.priority) }
    var dialogOpen by remember { mutableStateOf(true) }

    AlertDialog(
        { dialogOpen = false }, {
            Button(
                onClick = {
                    // Actualiza la prioridad de la tarea y llama a onUpdateTask para reflejar los cambios
                    task.priority = selectedPriority
                    onUpdateTask(task)
                    dialogOpen = false
                }
            ) {
                Text("Guardar")
            }
        },
        title = { Text("Seleccionar Prioridad") },
        dismissButton = {
            Button(
                onClick = {
                    dialogOpen = false
                }
            ) {
                Text("Cancelar")
            }
        },
        content = {
            Priority.values().forEach { priority ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPriority = priority
                        },
                    content = {
                        RadioButton(
                            selected = (priority == selectedPriority),
                            onClick = { selectedPriority = priority }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(priority.name)
                    }
                )
            }
        }
    )
}


fun getBackgroundColorForPriority(priority: Priority): Color {
    return when (priority) {
        Priority.LOW -> Color.Green
        Priority.MEDIUM -> Color.Blue
        Priority.HIGH -> Color.Yellow
        Priority.URGENT -> Color.Red
    }
}

@Composable
fun TaskList(tasks: List<Task>, onUpdateTask: (Task) -> Unit) {
    Column {
        for (task in tasks) {
            TaskItem(task = task, onUpdateTask = onUpdateTask)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(tasks: MutableList<Task>) {
    var newTaskName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            TopAppBar(
                title = { Text("Lista de Tareas") },
                actions = {
                    IconButton(
                        onClick = {
                            tasks.removeAll { it.isCompleted }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    }
                }
            )
        }

        item {
            TaskList(tasks = tasks) { updatedTask ->
                val index = tasks.indexOfFirst { it.id == updatedTask.id }
                if (index != -1) {
                    tasks[index] = updatedTask
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            BasicTextField(
                value = newTaskName,
                onValueChange = { newTaskName = it },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newTaskName.isNotBlank()) {
                            val newTask = Task(
                                id = tasks.size,
                                name = newTaskName,
                                isCompleted = false,
                                priority = Priority.LOW,
                                estimatedHours = 0,
                                actualHours = 0,
                                dueDate = "",
                                completionDate = ""
                            )
                            tasks.add(newTask)
                            newTaskName = ""
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp)
            )
        }
    }
}

@Preview
@Composable
fun MyAppPreview() {
    val tasks = List(20) { index ->
        Task(
            id = index,
            name = "Tarea ${index + 1}",
            isCompleted = false,
            priority = Priority.LOW,
            estimatedHours = 0,
            actualHours = 0,
            dueDate = "",
            completionDate = ""
        )
    }
    MyApp(tasks = tasks.toMutableList())
}


