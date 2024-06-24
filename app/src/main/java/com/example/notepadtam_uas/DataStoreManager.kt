package com.example.notepadtam_uas

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "notes")

class DataStoreManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val NOTES_KEY = stringSetPreferencesKey("notes")
    }

    private val notes: Flow<Set<String>> = dataStore.data
        .map { preferences ->
            preferences[NOTES_KEY] ?: emptySet()
        }

    suspend fun saveNoteAndDate(note: String) {
        dataStore.edit { preferences ->
            val currentNotes = preferences[NOTES_KEY]?.toMutableSet() ?: mutableSetOf()
            currentNotes.add(note)
            preferences[NOTES_KEY] = currentNotes
        }
    }

    suspend fun deleteNoteAndDate(note: String) {
        dataStore.edit { preferences ->
            val currentNotes = preferences[NOTES_KEY]?.toMutableSet() ?: mutableSetOf()
            currentNotes.remove(note)
            preferences[NOTES_KEY] = currentNotes
        }
    }

    suspend fun updateNoteAndDate(oldNote: String, newNote: String) {
        dataStore.edit { preferences ->
            val currentNotes = preferences[NOTES_KEY]?.toMutableSet() ?: mutableSetOf()
            currentNotes.remove(oldNote)
            currentNotes.add(newNote)
            preferences[NOTES_KEY] = currentNotes
        }
    }

    fun getFilteredNotes(searchQuery: String): Flow<List<String>> {
        return notes.map { notesSet ->
            if (searchQuery.isBlank()) {
                notesSet.toList()
            } else {
                notesSet.filter { note ->
                    note.contains(searchQuery, ignoreCase = true)
                }.toList()
            }
        }
    }
}
