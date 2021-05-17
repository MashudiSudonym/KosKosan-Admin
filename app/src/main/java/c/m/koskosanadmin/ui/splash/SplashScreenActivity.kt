package c.m.koskosanadmin.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import c.m.koskosanadmin.databinding.ActivitySplashScreenBinding
import c.m.koskosanadmin.ui.login.LoginActivity
import c.m.koskosanadmin.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var splashScreenBinding: ActivitySplashScreenBinding
    private val splashScreenViewModel: SplashScreenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // viewBinding initialize
        splashScreenBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(splashScreenBinding.root)

        // observe return value of the data
        splashScreenViewModel.isUserAuthenticated().observe(this, { isUserAuth ->
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            val loginActivityIntent = Intent(this, LoginActivity::class.java)

            // using kotlin coroutine for create the delay
            CoroutineScope(Dispatchers.Main).launch {
                // delay for 2 second
                delay(2000L)

                // if userAuth is false open Login Activity
                // else open Main Activity
                if (!isUserAuth) {
                    finish()
                    startActivity(loginActivityIntent)
                } else {
                    finish()
                    startActivity(mainActivityIntent)
                }
            }
        })
    }
}