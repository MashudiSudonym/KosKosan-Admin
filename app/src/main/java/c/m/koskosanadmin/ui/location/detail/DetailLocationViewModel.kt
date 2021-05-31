package c.m.koskosanadmin.ui.location.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import c.m.koskosanadmin.data.model.LocationResponse
import c.m.koskosanadmin.data.repository.FirebaseRepository
import c.m.koskosanadmin.vo.ResponseState

class DetailLocationViewModel(private val firebaseRepository: FirebaseRepository) : ViewModel() {
    // get detail location by location uid
    private lateinit var _locationUIDInput: String

    fun setLocationUID(locationUID: String) {
        this._locationUIDInput = locationUID
    }

    fun getLocationDetailByLocationUID(): LiveData<ResponseState<LocationResponse>> =
        firebaseRepository.readLocationDetailByLocationUid(_locationUIDInput)
}