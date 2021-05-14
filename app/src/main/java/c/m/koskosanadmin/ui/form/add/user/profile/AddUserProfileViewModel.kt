package c.m.koskosanadmin.ui.form.add.user.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import c.m.koskosanadmin.data.repository.AuthRepository
import c.m.koskosanadmin.data.repository.FirebaseRepository
import c.m.koskosanadmin.vo.ResponseState

class AddUserProfileViewModel(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    // get user uid
    private val userUID: LiveData<String> = authRepository.getUserUid()

    // get user phone number
    fun getUserPhoneNumber(): LiveData<String> = authRepository.getUserPhoneNumber()

    // post user profile data and return of uploading data progress
    private lateinit var _nameInput: String
    private lateinit var _imageProfilePathInput: Uri
    private lateinit var _phoneNumberInput: String
    private lateinit var _addressInput: String
    private lateinit var _emailInput: String

    fun setUserProfileDataInput(
        name: String,
        imageProfilePath: Uri,
        phoneNumber: String,
        address: String,
        email: String,
    ) {
        this._nameInput = name
        this._imageProfilePathInput = imageProfilePath
        this._phoneNumberInput = phoneNumber
        this._addressInput = address
        this._emailInput = email
    }

    fun postUserProfileData(): LiveData<ResponseState<Double>> =
        firebaseRepository.createUserProfileData(
            userUID.value.toString(),
            _nameInput,
            _imageProfilePathInput,
            _phoneNumberInput,
            _addressInput,
            _emailInput
        )
}