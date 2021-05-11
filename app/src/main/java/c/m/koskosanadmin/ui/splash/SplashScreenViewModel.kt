package c.m.koskosanadmin.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import c.m.koskosanadmin.data.repository.AuthRepository

class SplashScreenViewModel(private val authRepository: AuthRepository) : ViewModel() {
    // user it's login ?
    fun isUserAuthenticated(): LiveData<Boolean> = authRepository.checkUserAuthenticated()
}