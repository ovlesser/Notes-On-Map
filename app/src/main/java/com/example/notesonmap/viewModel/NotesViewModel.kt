package com.example.notesonmap.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesonmap.data.Note
import com.example.notesonmap.data.sampleData
import com.google.android.gms.maps.model.LatLng

class NotesViewModel: ViewModel() {
    private val _notes = MutableLiveData<MutableList<Note>>()

    val notes: MutableLiveData<MutableList<Note>>
        get() = _notes

    init {
//        _notes.value = sampleData.toMutableList()
        _notes.value = mutableListOf()
    }

    fun addNote(note: Note) {
        _notes.value?.add( note)
    }
}
