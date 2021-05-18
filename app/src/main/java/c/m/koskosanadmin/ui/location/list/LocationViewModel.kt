package c.m.koskosanadmin.ui.location.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import c.m.koskosanadmin.data.model.LocationResponse
import c.m.koskosanadmin.data.repository.AuthRepository
import c.m.koskosanadmin.data.repository.FirebaseRepository
import c.m.koskosanadmin.vo.ResponseState

class LocationViewModel(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    // get owner / user uid
    private val userUID: LiveData<String> = authRepository.getUserUid()

    // get location list by owner / user uid
    fun getLocationByOwnerUID(): LiveData<ResponseState<List<LocationResponse>>> =
        firebaseRepository.readLocationByOwnerUid(userUID.value.toString())
}