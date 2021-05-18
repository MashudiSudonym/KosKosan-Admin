package c.m.koskosanadmin.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import c.m.koskosanadmin.data.model.UserResponse
import c.m.koskosanadmin.data.repository.AuthRepository
import c.m.koskosanadmin.data.repository.FirebaseRepository
import c.m.koskosanadmin.vo.ResponseState

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    // get user uid
    private val userUID: LiveData<String> = authRepository.getUserUid()

    // get user profile data by user uid
    fun getUserProfile(): LiveData<ResponseState<UserResponse>> =
        firebaseRepository.readUserProfileData(userUID.value.toString())
}