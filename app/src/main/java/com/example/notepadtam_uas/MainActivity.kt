package com.example.notepadtam_uas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notepadtam_uas.ui.theme.NotePadTAM_UASTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStoreManager = DataStoreManager(applicationContext)
        setContent {
            NotePadTAM_UASTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "main_screen") {
                        composable("main_screen") { MainScreen(navController, dataStoreManager) }
                        composable("edit_add_screen/{note}") { backStackEntry ->
                            val note = backStackEntry.arguments?.getString("note")
                            EditAddScreen(navController, dataStoreManager, note)
                        }
                        composable("add_new_note_screen") {
                            AddNewNoteScreen(navController, dataStoreManager)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MainScreen(navController: NavController, dataStoreManager: DataStoreManager) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(searchQuery) {
        dataStoreManager.getFilteredNotes(searchQuery).collect { filteredNotes ->
            notes = filteredNotes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textStyle = TextStyle(color = Color.White),
            singleLine = true,
            placeholder = { Text("Search notes...", color = Color.Gray) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (notes.isEmpty()) {
                item {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(color = Color.White)){
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)) {
                            Icon(painter = painterResource(R.drawable.open_folder_outline_icon),
                                contentDescription = null,
                                Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(100.dp))
                            Text("No notes found",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.CenterHorizontally),
                                color = Color.Black)
                        }
                    }
                }
            } else {
                items(notes) { note ->
                    val noteParts = note.split("|")
                    val noteText = noteParts[0]
                    val noteDate = noteParts[1]

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(color = Color.White)
                    ){
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                        ) {
                            Text("Last saved: $noteDate", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(noteText, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row (modifier = Modifier. padding(8.dp)) {
                                Button(onClick = {
                                    navController.navigate("edit_add_screen/${note.replace("/", "\\/")}")
                                }) {
                                    Text("Edit")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    scope.launch {
                                        dataStoreManager.deleteNoteAndDate(note)
                                        notes = dataStoreManager.getFilteredNotes(searchQuery).first()
                                    }
                                }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("add_new_note_screen")
        }) {
            Text("Add New Note")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun EditAddScreen(navController: NavController, dataStoreManager: DataStoreManager, note: String?) {
    val scope = rememberCoroutineScope()
    val initialNoteText = note?.split("|")?.get(0) ?: ""
    var noteText by remember { mutableStateOf(TextFieldValue(initialNoteText)) }

    Box(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(color = Color.White)){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            TextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textStyle = TextStyle(color = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(onClick = {
                    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val newNote = "${noteText.text}|$currentDate"
                    scope.launch {
                        if (note != null) {
                            dataStoreManager.updateNoteAndDate(note, newNote)
                        } else {
                            dataStoreManager.saveNoteAndDate(newNote)
                        }
                        navController.navigate("main_screen") {
                            popUpTo("main_screen") { inclusive = true }
                        }
                    }
                }) {
                    Text("Save Note")
                }
                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    navController.navigate("main_screen") {
                        popUpTo("main_screen") { inclusive = true }
                    }
                }) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun AddNewNoteScreen(navController: NavController, dataStoreManager: DataStoreManager) {
    val scope = rememberCoroutineScope()
    var newNoteText by remember { mutableStateOf(TextFieldValue()) }

    Box(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(color = Color.White)){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(color = Color.White)
                .clip(RoundedCornerShape(16.dp))
        ) {
            TextField(
                value = newNoteText,
                onValueChange = { newNoteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textStyle = TextStyle(color = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(onClick = {
                    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val newNote = "${newNoteText.text}|$currentDate"
                    scope.launch {
                        dataStoreManager.saveNoteAndDate(newNote)
                        navController.navigate("main_screen") {
                            popUpTo("main_screen") { inclusive = true }
                        }
                    }
                }) {
                    Text("Save New Note")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    navController.navigate("main_screen") {
                        popUpTo("main_screen") { inclusive = true }
                    }
                }) {
                    Text("Cancel")
                }
            }
        }
    }
}

