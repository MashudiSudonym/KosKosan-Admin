package c.m.koskosanadmin.ui.form.update.user.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import c.m.koskosanadmin.data.model.UserResponse
import c.m.koskosanadmin.data.repository.AuthRepository
import c.m.koskosanadmin.data.repository.FirebaseRepository
import c.m.koskosanadmin.vo.ResponseState

class UpdateUserProfileViewModel(
    authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    // get user / owner uid
    private val userUID: LiveData<String> = authRepository.getUserUid()

    // get user / owner profile data to default field form value
    fun getUserProfileData(): LiveData<ResponseState<UserResponse>> =
        firebaseRepository.readUserProfileData(userUID.value.toString())

    // put user / owner profile data
    private lateinit var _nameInput: String
    private var _imageProfilePathInput: Uri? = null
    private lateinit var _phoneNumberInput: String
    private lateinit var _addressInput: String
    private lateinit var _emailInput: String

    fun setUserProfileDataInput(
        name: String,
        imageProfilePath: Uri?,
        phoneNumber: String,
        address: String,
        email: String
    ) {
        this._nameInput = name
        this._imageProfilePathInput = imageProfilePath
        this._phoneNumberInput = phoneNumber
        this._addressInput = address
        this._emailInput = email
    }

    fun putUserProfileData(): LiveData<ResponseState<Double>> {
        return if (_imageProfilePathInput == null) {
            firebaseRepository.updateUserProfileData(
                userUID.value.toString(),
                _nameInput,
                _addressInput,
                _emailInput
            )
        } else {
            firebaseRepository.createUserProfileData(
                userUID.value.toString(),
                _nameInput,
                _imageProfilePathInput,
                _phoneNumberInput,
                _addressInput,
                _emailInput
            )
        }
    }
}