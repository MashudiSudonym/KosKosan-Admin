package c.m.koskosanadmin.ui.form.add.location

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import c.m.koskosanadmin.data.repository.AuthRepository
import c.m.koskosanadmin.data.repository.FirebaseRepository
import c.m.koskosanadmin.vo.ResponseState
import com.google.firebase.firestore.GeoPoint

class AddLocationViewModel(
    authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    // get user uid
    private val locationOwnerUID: LiveData<String> = authRepository.getUserUid()

    // post location data and return of uploading data progress
    // set user input location data
    private lateinit var _locationNameInput: String
    private lateinit var _locationAddressInput: String
    private lateinit var _locationPhoneInput: String
    private lateinit var _locationGooglePlaceInput: String
    private lateinit var _locationTypeInput: String
    private lateinit var _locationCoordinateInput: GeoPoint
    private lateinit var _locationPhotoInput: Uri

    fun setLocationDataInput(
        locationName: String,
        locationAddress: String,
        locationPhone: String,
        locationGooglePlace: String,
        locationType: String,
        locationCoordinate: GeoPoint,
        locationPhoto: Uri
    ) {
        this._locationNameInput = locationName
        this._locationAddressInput = locationAddress
        this._locationPhoneInput = locationPhone
        this._locationGooglePlaceInput = locationGooglePlace
        this._locationTypeInput = locationType
        this._locationCoordinateInput = locationCoordinate
        this._locationPhotoInput = locationPhoto
    }

    fun postNewLocationData(): LiveData<ResponseState<Double>> =
        firebaseRepository.createNewLocationData(
            _locationNameInput,
            _locationAddressInput,
            _locationGooglePlaceInput,
            locationOwnerUID.value.toString(),
            _locationPhoneInput,
            _locationTypeInput,
            _locationPhotoInput,
            _locationCoordinateInput,
        )
}