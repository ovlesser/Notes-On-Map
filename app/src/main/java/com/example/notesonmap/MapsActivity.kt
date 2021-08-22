package com.example.notesonmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.notesonmap.data.Note
import com.example.notesonmap.databinding.ActivityMapsBinding
import com.example.notesonmap.databinding.DialogInputBinding
import com.example.notesonmap.viewModel.NotesViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private val notesViewModel: NotesViewModel by lazy {
        ViewModelProvider(this).get(NotesViewModel::class.java)
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private var cameraPosition: CameraPosition? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener { marker -> showNoteDetail(notesViewModel.notes.value?.firstOrNull { note -> note.latLng == marker.position }, marker.position)}
        mMap.setOnMapLongClickListener { latLng -> showNoteDetail(null, latLng)}
        getLocationPermission()
        getDeviceLocation()
//        lastKnownLocation?.apply {
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                LatLng(latitude, longitude), DEFAULT_ZOOM.toFloat()))
//        }
        updateUI()
        notesViewModel.notes.observe(this, Observer {
            if (it != null) {
                updateUI()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_CAMERA_POSITION, mMap.cameraPosition)
        outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("MissingPermission")
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            mMap.isMyLocationEnabled = true
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            mMap.isMyLocationEnabled = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mMap.isMyLocationEnabled) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun updateUI() {
        notesViewModel.notes.value?.forEach {
            val bmp = BitmapDescriptorFactory.fromBitmap(buildBitmap(it, 36.0F, Color.BLACK))
            mMap.addMarker(
                MarkerOptions()
                    .position(it.latLng)
//                    .title("${it.text} - ${it.user}")
                    .icon(bmp)
            )
                .showInfoWindow()
        }
        try {
            if (mMap.isMyLocationEnabled) {
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    fun buildBitmap(note: Note, textSize: Float, textColor: Int): Bitmap? {
        val lines = "${note.text}\n\n - ${note.user}".split("\n")
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = textColor
        paint.textAlign = Paint.Align.LEFT
        var baseline: Float = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(lines.maxByOrNull{ it.length }) + 0.5f).toInt() + 16 // round
        val height = lines.size * 44
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image).apply {
            val paint = Paint()
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            drawRect(0.0f, 0.0F, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            drawRect(0.0f, 0.0F, (width-1).toFloat(), (height-1).toFloat(), paint)
        }
        lines.forEach {
            canvas.drawText(it, 8.0f, baseline, paint)
            baseline += 44
        }
        return image
    }

    private fun showNoteDetail( note: Note?, latLng: LatLng): Boolean {
        val dialogInputBinding: DialogInputBinding = DialogInputBinding.inflate(layoutInflater)
        dialogInputBinding.setUser(note?.user)
        dialogInputBinding.setText(note?.text)
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Note")
            .setView( dialogInputBinding.root)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        if (note == null) {
            dialogBuilder.setPositiveButton("OK") { dialog, _ ->
                val user = dialogInputBinding.getUser() ?: ""
                val text = dialogInputBinding.getText() ?: ""
                if (user.isNotEmpty() and text.isNotEmpty()) {
                    notesViewModel.addNote(Note(user = user, text = text, latLng = latLng))
                    updateUI()
                }
            }
        }
        dialogBuilder.show()
        return true
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }
}
