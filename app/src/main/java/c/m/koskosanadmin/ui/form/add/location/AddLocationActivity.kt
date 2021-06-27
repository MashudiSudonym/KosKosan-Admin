package c.m.koskosanadmin.ui.form.add.location

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.ActivityAddLocationBinding
import c.m.koskosanadmin.util.Constants
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddLocationActivity : AppCompatActivity() {

    private val addLocationViewModel: AddLocationViewModel by viewModel()
    private lateinit var addLocationBinding: ActivityAddLocationBinding
    private lateinit var layout: View
    private var locationType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize view binding
        addLocationBinding = ActivityAddLocationBinding.inflate(layoutInflater)
        setContentView(addLocationBinding.root)

        // for handle view widget utilities
        layout = addLocationBinding.root

        // appbar/actionbar title setup
        setSupportActionBar(addLocationBinding.toolbarAddLocation)
        supportActionBar?.apply {
            title = getString(R.string.add_new_location)
            setDisplayHomeAsUpEnabled(true)
        }

        // radio button selected listener
        addLocationBinding.radioGroupLocationType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_button_location_type_man -> locationType = Constants.TYPE_OF_MAN
                R.id.radio_button_location_type_woman -> locationType = Constants.TYPE_OF_WOMAN
                R.id.radio_button_location_type_mix -> locationType = Constants.TYPE_OF_MIX
                R.id.radio_button_location_type_other -> locationType = Constants.TYPE_OF_OTHER
            }
        }
    }

    // activate back button arrow
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}