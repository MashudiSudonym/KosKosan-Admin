package c.m.koskosanadmin.ui.form.add.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.ActivityAddLocationBinding
import c.m.koskosanadmin.util.Constants
import c.m.koskosanadmin.util.Constants.PERMISSION_REQUEST_LOCATION
import c.m.koskosanadmin.util.requestPermission
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddLocationActivity : AppCompatActivity() {

    private val addLocationViewModel: AddLocationViewModel by viewModel()
    private lateinit var addLocationBinding: ActivityAddLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var deviceLocationLatitude: Double? = 0.0
    private var deviceLocationLongitude: Double? = 0.0
    private var locationLatitude: Double? = 0.0
    private var locationLongitude: Double? = 0.0
    private var markerArrayList: ArrayList<Marker> = arrayListOf()
    private var locationType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize view binding
        addLocationBinding = ActivityAddLocationBinding.inflate(layoutInflater)
        setContentView(addLocationBinding.root)

        // appbar/actionbar title setup
        setSupportActionBar(addLocationBinding.toolbarAddLocation)
        supportActionBar?.apply {
            title = getString(R.string.add_new_location)
            setDisplayHomeAsUpEnabled(true)
        }

        // Initialize fused location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // get device coordinate
        getLastLocation()

        // initialize map
        addLocationBinding.mvLocationMap.onCreate(savedInstanceState)
        addLocationBinding.mvLocationMap.getMapAsync { googleMap ->
            // map type
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

            // map control setting
            if (checkPermission()) {
                googleMap.isMyLocationEnabled = true
            } else {
                this.requestPermission()
            }
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isCompassEnabled = true

            // map add marker
            googleMap.setOnMapClickListener { latLng ->
                // check no marker on map
                if (markerArrayList.size > 0) {
                    val markerToRemove = markerArrayList[0]

                    // remove marker from list
                    markerArrayList.remove(markerToRemove)

                    // remove marker on map
                    markerToRemove.remove()
                }

                // variable for save marker position
                val markerPosition = MarkerOptions().position(latLng).draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home_marker))
                val currentMarker = googleMap.addMarker(markerPosition)

                // add current marker to marker list
                markerArrayList.add(currentMarker as Marker)

                // set manual location
                locationLatitude = latLng.latitude
                locationLongitude = latLng.longitude
            }

            // check is there a marker attached
            // if there is, point the camera at the midpoint of the user's location detected by gps
            // if it is not there, point the camera at the center point of the marker
            if (markerArrayList.isNullOrEmpty()) {
                googleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().target(
                            LatLng(locationLatitude as Double, locationLongitude as Double)
                        ).zoom(16f).build()
                    )
                )

                // add default location marker
                val marker = MarkerOptions().position(
                    LatLng(
                        locationLatitude as Double,
                        locationLongitude as Double
                    )
                ).draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home_marker))
                val defaultLocationMarker = googleMap.addMarker(marker)

                markerArrayList.add(defaultLocationMarker as Marker)
            } else {
                googleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().target(
                            LatLng(
                                markerArrayList[0].position.latitude,
                                markerArrayList[0].position.longitude
                            )
                        ).zoom(16f).build()
                    )
                )
            }
        }


        // radio button selected listener
        radioButtonSelectedListener()
    }

    // radio button selected listener
    private fun radioButtonSelectedListener() {
        addLocationBinding.radioGroupLocationType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_button_location_type_man -> locationType = Constants.TYPE_OF_MAN
                R.id.radio_button_location_type_woman -> locationType = Constants.TYPE_OF_WOMAN
                R.id.radio_button_location_type_mix -> locationType = Constants.TYPE_OF_MIX
                R.id.radio_button_location_type_other -> locationType = Constants.TYPE_OF_OTHER
            }
        }
    }

    // activate back button arrow
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    // request last location
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        deviceLocationLatitude = location.latitude
                        deviceLocationLongitude = location.longitude
                    }
                }
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            this.requestPermission()
        }
    }

    // request new location
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        @Suppress("DEPRECATION") val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.myLooper()
        )
    }

    // callback location fuse
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            deviceLocationLatitude = lastLocation.latitude
            deviceLocationLongitude = lastLocation.longitude
        }
    }

    // check enable location source status
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // check ACCESS FINE LOCATION and ACCESS COARSE LOCATION permission
    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        addLocationBinding.mvLocationMap.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        addLocationBinding.mvLocationMap.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        addLocationBinding.mvLocationMap.onDestroy()
    }
}