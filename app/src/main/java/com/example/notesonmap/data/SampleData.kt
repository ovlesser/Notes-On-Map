package com.example.notesonmap.data

import com.google.android.gms.maps.model.LatLng

val sampleData = listOf(
    Note(user = "user1", text = "Manly Beach", latLng = LatLng(-33.78777149790135, 151.28657753901692)),
    Note(user = "user2", text = "Sydney Harbour Bridge", latLng = LatLng(-33.84738608026124, 151.21035989009738)),
    Note(user = "user3", text = "Taronga Zoo Sydney", latLng = LatLng(-33.83854635067856, 151.2405722914709)),
)