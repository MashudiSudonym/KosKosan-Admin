package c.m.koskosanadmin.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.ActivityLoginBinding
import c.m.koskosanadmin.ui.main.MainActivity
import c.m.koskosanadmin.util.ViewUtilities.snackBarWarningLong
import com.firebase.ui.auth.IdpResponse
import android.view.View
import c.m.koskosanadmin.util.requestPermission
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var layout: View
    private var authUILauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val response = IdpResponse.fromResultIntent(result.data)

            // if login activity result accepted, user will be parsing to Main Activity
            // else show error login report on snackbar
            if (result.resultCode == Activity.RESULT_OK) {
                // stop login activity
                finish()
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
            } else {
                // if user cancel the login process, stay on this activity and show the result
                if (response == null) {
                    layout.snackBarWarningLong(getString(R.string.alert_login_cancel))
                }

                // if login process error because user connection internet interruption, show alert about it
                if (response?.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    layout.snackBarWarningLong(getString(R.string.alert_check_internet_connection))
                }

                Timber.e(
                    "Sign In error: ${response?.error?.message} || ${response?.error}"
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // viewBinding initialize
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        // for use View Utilities object
        layout = loginBinding.root

        // request location permission
        requestPermission()

        // login button
        loginBinding.btnLogin.setOnClickListener {
            // with FirebaseUI library, help me to build user login with phone number
            authUILauncher.launch(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                        listOf(
                            AuthUI.IdpConfig.PhoneBuilder()
                                .setDefaultCountryIso(getString(R.string.default_code_country))
                                .build()
                        )
                    ).build()
            )
        }
    }
}