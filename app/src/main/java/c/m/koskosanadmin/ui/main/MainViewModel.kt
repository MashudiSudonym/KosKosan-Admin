package c.m.koskosanadmin.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import c.m.koskosanadmin.data.repository.AuthRepository
import c.m.koskosanadmin.data.repository.FirebaseRepository

class MainViewModel(
    authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    // get user uid
    private val userUid: LiveData<String> = authRepository.getUserUid()

    // check user profile data status
    fun isUserProfileDataIsNull(): LiveData<Boolean> =
        firebaseRepository.checkUserProfileData(userUid.value.toString())
}