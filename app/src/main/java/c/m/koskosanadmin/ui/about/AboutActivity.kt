package c.m.koskosanadmin.ui.about

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var aboutBinding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize view binding
        aboutBinding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(aboutBinding.root)

        // AppBar / ActionBar Title Setup
        setSupportActionBar(aboutBinding.toolbarAbout)
        supportActionBar?.apply {
            title = getString(R.string.about)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}