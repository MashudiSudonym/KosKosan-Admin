package c.m.koskosanadmin.ui.form.add.user.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import c.m.koskosanadmin.databinding.ActivityAddUserProfileBinding

class AddUserProfileActivity : AppCompatActivity() {

    private lateinit var addUserProfileBinding: ActivityAddUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize viewBinding
        addUserProfileBinding = ActivityAddUserProfileBinding.inflate(layoutInflater)
        setContentView(addUserProfileBinding.root)
    }
}