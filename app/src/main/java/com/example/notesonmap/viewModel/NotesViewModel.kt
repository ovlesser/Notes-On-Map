package com.example.notesonmap.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesonmap.data.Note
import com.example.notesonmap.data.sampleData
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class NotesViewModel: ViewModel() {
    private val _notes = MutableLiveData<MutableList<Note>>()

    val notes: MutableLiveData<MutableList<Note>>
        get() = _notes

    init {
//        _notes.value = sampleData.toMutableList()
        _notes.value = mutableListOf()
        getDataFromFirebase()
    }

    private fun getDataFromFirebase() {
        val database = Firebase.database
        val myRef = database.getReference(PATH)
        // Read from the database
//        myRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                val value= dataSnapshot.getValue()
//                Log.d(TAG, "Value is: $_notes.value")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//        })

//        myRef.get().addOnSuccessListener {
//            Log.i(TAG, "lql Got value ${it.value}")
//        }.addOnFailureListener{
//            Log.e(TAG, "lql Error getting data", it)
//        }

        myRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val value = snapshot.getValue<HashMap<String, Any>>()
                val latLng = value?.get("latLng") as HashMap< String, Double>
                _notes.value = _notes.value?.apply { add(
                    Note(
                        user = value.get("user") as String, text = value.get("text") as String,
                        latLng = LatLng(
                            latLng.get("latitude") ?: 0.0, latLng.get("longitude") ?: 0.0,
                        ),
                    ),
                )}
                Log.d(TAG, "lql, onChildAdded, ${_notes.value}")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun addNote(note: Note) {
        val database = Firebase.database
        val myRef = database.getReference(PATH)

        myRef.push().setValue(note)
    }

    companion object {
        private val TAG = NotesViewModel::class.simpleName
        private const val PATH = "note"
    }
}
