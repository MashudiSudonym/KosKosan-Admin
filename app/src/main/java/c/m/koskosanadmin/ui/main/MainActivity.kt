package c.m.koskosanadmin.ui.main

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.ActivityMainBinding
import c.m.koskosanadmin.ui.form.add.user.profile.AddUserProfileActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize viewBinding
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        // Bottom Navigation and Navigation Controller
        val navView: BottomNavigationView = mainBinding.navView
        val navController = findNavController(R.id.nav_host_fragment)

        // Attach view of bottom navigation to navigation controller
        navView.setupWithNavController(navController)

        // check user have a profile data or null
        mainViewModel.isUserProfileDataIsNull().observe(this, { userProfileDataIsNull ->
            val addUserProfileIntent = Intent(this, AddUserProfileActivity::class.java)

            // if user profile data is null to be true it's open form add user profile screen
            // else do nothing, stay on this activity
            if (userProfileDataIsNull) {
                finish()
                startActivity(addUserProfileIntent)
            }
        })
    }
}