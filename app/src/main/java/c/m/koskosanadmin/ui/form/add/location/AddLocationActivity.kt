package c.m.koskosanadmin.ui.form.add.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.ActivityAddLocationBinding
import c.m.koskosanadmin.databinding.BottomSheetOptionImageBinding
import c.m.koskosanadmin.ui.main.MainActivity
import c.m.koskosanadmin.util.*
import c.m.koskosanadmin.util.Constants.PERMISSION_REQUEST_LOCATION
import c.m.koskosanadmin.util.ViewUtilities.gone
import c.m.koskosanadmin.util.ViewUtilities.hideKeyboard
import c.m.koskosanadmin.util.ViewUtilities.invisible
import c.m.koskosanadmin.util.ViewUtilities.openGallery
import c.m.koskosanadmin.util.ViewUtilities.snackBarBasicIndefinite
import c.m.koskosanadmin.util.ViewUtilities.snackBarBasicIndefiniteAction
import c.m.koskosanadmin.util.ViewUtilities.snackBarBasicShort
import c.m.koskosanadmin.util.ViewUtilities.snackBarWarningIndefiniteAction
import c.m.koskosanadmin.util.ViewUtilities.snackBarWarningLong
import c.m.koskosanadmin.util.ViewUtilities.visible
import c.m.koskosanadmin.vo.ResponseState
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.GeoPoint
import id.rizmaulana.sheenvalidator.lib.SheenValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var bottomSheet: View
    private lateinit var sheetBehavior: BottomSheetBehavior<View>
    private var sheetDialog: BottomSheetDialog? = null
    private var photoPathURI: Uri? = null
    private var currentPhotoPath: String? = null
    private lateinit var sheenValidator: SheenValidator
    private var takePictureCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Get the dimensions of the View
                val targetWidth = 200
                val targetHeight = 200

                val bmOptions = BitmapFactory.Options().apply {
                    // Get the dimensions of the bitmap
                    inJustDecodeBounds = true

                    val photoWidth: Int = outWidth
                    val photoHeight: Int = outHeight

                    // Determine how much to scale down the image
                    val scaleFactor: Int =
                        (photoWidth / targetWidth).coerceAtMost(photoHeight / targetHeight)

                    // Decode the image file into a Bitmap sized to fill the View
                    inJustDecodeBounds = false
                    inSampleSize = scaleFactor
                }
                BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
                    addLocationBinding.animCamera.setImageBitmap(bitmap)
                }
            } else {
                addLocationBinding.root.snackBarWarningLong(getString(R.string.data_error_null))
            }
        }

    @Suppress("DEPRECATION")
    private var takePictureGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    photoPathURI = result.data?.data

                    val bitmap =
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            val photoSource =
                                photoPathURI?.let { uri ->
                                    ImageDecoder.createSource(
                                        this.contentResolver,
                                        uri
                                    )
                                }
                            photoSource?.let { ImageDecoder.decodeBitmap(it) }
                        } else {
                            MediaStore.Images.Media.getBitmap(this.contentResolver, photoPathURI)
                        }

                    addLocationBinding.animCamera.setImageBitmap(bitmap)
                }
            } else {
                addLocationBinding.root.snackBarWarningLong(getString(R.string.data_error_null))
            }
        }

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

        // initialize map / select location on map
        selectLocationOnMap(savedInstanceState)

        // radio button selected listener
        radioButtonSelectedListener()

        // Bottom Sheet Initialize
        bottomSheetInitialize()

        // Do Validator Form
        formValidation()
    }

    // Validate form on this activity
    private fun formValidation() {
        sheenValidator = SheenValidator(this).also { sheenValidator ->
            sheenValidator.registerAsRequired(addLocationBinding.edtLocationName)
            sheenValidator.registerAsRequired(addLocationBinding.edtLocationAddress)
            sheenValidator.registerAsRequired(addLocationBinding.edtLocationPhone)

            sheenValidator.setOnValidatorListener {
                postLocationData()
            }
        }

        addLocationBinding.btnSave.setOnClickListener {
            // hide keyboard
            hideKeyboard(addLocationBinding.root)

            when {
                // do validation check for photo path field
                photoPathURI == null -> {
                    addLocationBinding.root.snackBarWarningLong(getString(R.string.alert_photo_null))
                }
                // do validation check for selection of location marker
                locationLongitude == 0.0 && locationLatitude == 0.0 -> {
                    addLocationBinding.root.snackBarWarningLong(
                        getString(R.string.alert_location_mark_null)
                    )
                }
                // do validation check for selection of type
                locationType.isBlank() -> {
                    addLocationBinding.root.snackBarWarningLong(
                        getString(R.string.alert_type_of_location_null)
                    )
                }
                else -> sheenValidator.validate()
            }
        }
    }

    // posting location data to database
    private fun postLocationData() {
        val locationGooglePlace = if (addLocationBinding.edtLocationGooglePlace.text.toString()
                .isNotBlank()
        ) addLocationBinding.edtLocationGooglePlace.text.toString() else "-"

        addLocationViewModel.setLocationDataInput(
            addLocationBinding.edtLocationName.text.toString(),
            addLocationBinding.edtLocationAddress.text.toString(),
            addLocationBinding.edtLocationPhone.text.toString(),
            locationGooglePlace,
            locationType,
            GeoPoint(locationLatitude ?: 0.0, locationLongitude ?: 0.0),
            photoPathURI as Uri
        )

        addLocationViewModel.postNewLocationData().observe(this, { response ->
            if (response != null) when (response) {
                is ResponseState.Error -> {
                    response.message?.let {
                        hideSendingAnimation()
                        addLocationBinding.root.snackBarWarningLong(getString(R.string.error_upload_message) + it)
                    }
                }
                is ResponseState.Loading -> {
                    response.data?.let {
                        showSendingAnimation()
                        addLocationBinding.root.snackBarBasicIndefinite(
                            "Uploading data : $it %"
                        )
                    }
                }
                is ResponseState.Success -> {
                    response.data?.let {
                        hideSendingAnimation()

                        // back to main activity
                        val mainActivityIntent = Intent(this, MainActivity::class.java)

                        startActivity(mainActivityIntent)
                    }
                }
            }
        })
    }

    // Initialize setup of bottom sheet navigation
    private fun bottomSheetInitialize() {
        bottomSheet = addLocationBinding.bottomSheet
        sheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // Choose Image Button for trigger bottom navigation show up
        addLocationBinding.btnChooseImage.setOnClickListener {
            imageProfileSourceBottomSheet()
        }
    }

    // Bottom Sheet dialog for option image profile source
    private fun imageProfileSourceBottomSheet() {
        val bottomSheetOptionImageBinding = BottomSheetOptionImageBinding.inflate(layoutInflater)
        val viewBottomSheet = bottomSheetOptionImageBinding.root

        sheetDialog = BottomSheetDialog(this)
        sheetDialog?.setContentView(viewBottomSheet)
        sheetDialog?.show()
        sheetDialog?.setOnDismissListener { sheetDialog = null }

        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheetOptionImageBinding.btnCamera.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                showCamera()
            }

            sheetDialog?.dismiss()
        }

        bottomSheetOptionImageBinding.btnGallery.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                showGallery()
            }

            sheetDialog?.dismiss()
        }
    }

    // Show Camera Application
    private fun showCamera() {
        if (checkSelfPermissionCompat(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            addLocationBinding.root.snackBarBasicShort(
                getString(R.string.alert_camera_permission_available)
            )
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    // Open Gallery Image
    private fun showGallery() {
        if (checkSelfPermissionCompat(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery(packageManager, takePictureGalleryLauncher)
        } else {
            requestReadStoragePermission()
        }
    }

    // Request Read Storage Permission
    private fun requestReadStoragePermission() {
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            addLocationBinding.root.snackBarBasicIndefiniteAction(
                getString(R.string.alert_read_external_storage_required),
                getString(R.string.ok)
            ) {
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.CAMERA),
                    Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        } else {
            addLocationBinding.root.snackBarBasicShort(
                getString(R.string.alert_storage_permission_not_available)
            )

            requestPermissionsCompat(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
            )
        }
    }

    // Logic for open camera application
    @SuppressLint("QueryPermissionsNeeded")
    private fun startCamera() {
        try {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File = createImageFile()
                    // Continue only if the File was successfully created
                    photoFile.also {
                        photoPathURI = FileProvider.getUriForFile(
                            this,
                            "$packageName.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoPathURI)
                        takePictureCameraLauncher.launch(takePictureIntent)
                    }
                }
            }
        } catch (error: IOException) {
            Timber.e(error)
            addLocationBinding.root.snackBarWarningLong(getString(R.string.failed_load_photo_camera))
        }
    }

    // Creating photo path file and directory name
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        if (storageDir?.exists() == false) storageDir.mkdirs()

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    // Request Camera Permission
    // If self check permission is denied
    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.CAMERA)) {
            addLocationBinding.root.snackBarBasicIndefiniteAction(
                getString(R.string.alert_camera_access_required),
                getString(R.string.ok)
            ) {
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.CAMERA),
                    Constants.PERMISSION_REQUEST_CAMERA
                )
            }
        } else {
            addLocationBinding.root.snackBarBasicShort(
                getString(R.string.alert_camera_permission_not_available)
            )

            requestPermissionsCompat(
                arrayOf(Manifest.permission.CAMERA),
                Constants.PERMISSION_REQUEST_CAMERA
            )
        }
    }

    // initialize map / select location on map
    private fun selectLocationOnMap(savedInstanceState: Bundle?) {
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

        // request location permission
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }

        // Permission Request Camera
        if (requestCode == Constants.PERMISSION_REQUEST_CAMERA) {
            if (
                grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                addLocationBinding.root.snackBarBasicShort(
                    getString(R.string.alert_camera_permission_granted)
                )
                startCamera()
            } else {
                addLocationBinding.root.snackBarBasicShort(
                    getString(R.string.alert_camera_permission_denied)
                )
            }
        }

        // Permission Request Storage
        if (requestCode == Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (
                grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                addLocationBinding.root.snackBarBasicShort(
                    getString(R.string.alert_storage_permission_granted)
                )
                // Open Gallery
            } else {
                addLocationBinding.root.snackBarBasicShort(
                    getString(R.string.alert_storage_permission_denied)
                )
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

    // handling back button for give a warning before back to last activity
    override fun onBackPressed() {
        val shouldAllowBack = false

        if (shouldAllowBack) {
            super.onBackPressed()
        } else {
            addLocationBinding.root.snackBarWarningIndefiniteAction(
                getString(R.string.alert_cancel_add_new_location),
                getString(R.string.ok)
            ) {
                super.onBackPressed()
            }
            return
        }

    }

    // function for app bar back arrow
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    // show sending animation
    private fun showSendingAnimation() {
        addLocationBinding.formLayout.invisible()
        addLocationBinding.animSending.visible()
    }

    // hide sending animation
    private fun hideSendingAnimation() {
        addLocationBinding.formLayout.visible()
        addLocationBinding.animSending.gone()
    }
}