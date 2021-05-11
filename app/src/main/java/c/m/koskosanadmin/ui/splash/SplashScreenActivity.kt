package c.m.koskosanadmin.ui.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var splashScreenBinding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // viewBinding initialize
        splashScreenBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(splashScreenBinding.root)
    }
}