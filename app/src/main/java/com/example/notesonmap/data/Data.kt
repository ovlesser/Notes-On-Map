package com.example.notesonmap.data

import com.google.android.gms.maps.model.LatLng

data class Note(
    val user: String = "",
    val text: String = "",
    val latLng: LatLng = LatLng(-33.8523341, 151.2106085)
)
