package c.m.koskosanadmin.ui.location.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.ActivityDetailLocationBinding
import c.m.koskosanadmin.ui.form.update.location.UpdateLocationActivity
import c.m.koskosanadmin.util.Constants.TYPE_OF_MAN
import c.m.koskosanadmin.util.Constants.TYPE_OF_MIX
import c.m.koskosanadmin.util.Constants.TYPE_OF_OTHER
import c.m.koskosanadmin.util.Constants.TYPE_OF_WOMAN
import c.m.koskosanadmin.util.Constants.UID
import c.m.koskosanadmin.util.ViewUtilities.gone
import c.m.koskosanadmin.util.ViewUtilities.invisible
import c.m.koskosanadmin.util.ViewUtilities.visible
import c.m.koskosanadmin.vo.ResponseState
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailLocationActivity : AppCompatActivity() {

    private val detailLocationViewModel: DetailLocationViewModel by viewModel()
    private lateinit var detailLocationBinding: ActivityDetailLocationBinding
    private var uid: String? = ""
    private lateinit var shareIntent: Intent
    private lateinit var updateLocationActivityIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize view binding
        detailLocationBinding = ActivityDetailLocationBinding.inflate(layoutInflater)
        setContentView(detailLocationBinding.root)

        // maps initialize
        detailLocationBinding.mapLocation.onCreate(savedInstanceState)

        // get parsing intent data
        uid = intent.getStringExtra(UID)

        // app bar / action bar title setup
        setSupportActionBar(detailLocationBinding.toolbarDetail)
        supportActionBar?.apply {
            title = getString(R.string.detail)
            setDisplayHomeAsUpEnabled(true)
        }

        // initialize get detail location by location uid
        initializeGetLocationByLocationUid()

        // initialize swipe refresh data
        detailLocationBinding.detailSwipeRefreshView.setOnRefreshListener {
            detailLocationBinding.detailSwipeRefreshView.isRefreshing = false

            // get data
            initializeGetLocationByLocationUid()
        }
    }

    // initialize get detail location by location uid
    @SuppressLint("SetTextI18n")
    private fun initializeGetLocationByLocationUid() {
        detailLocationViewModel.setLocationUID(uid.toString())
        detailLocationViewModel.getLocationDetailByLocationUID().observe(this, { response ->
            if (response != null) when (response) {
                is ResponseState.Error -> showErrorStateView() // error state
                is ResponseState.Loading -> showLoadingStateView() // loading state
                is ResponseState.Success -> {
                    // success state
                    showSuccessStateView()

                    // show basic data to view
                    response.data?.also { data ->
                        detailLocationBinding.apply {
                            tvNameLocation.text = data.name
                            tvAddressLocation.text = data.address
                            tvGooglePlaceUrl.text = data.googlePlace
                            tvPhoneLocation.text =
                                "${getString(R.string.phone)} : ${data.phone}"
                            tvTypeLocation.text =
                                getString(R.string.type_of) + when (data.type) {
                                    TYPE_OF_WOMAN -> getString(R.string.woman)
                                    TYPE_OF_MAN -> getString(R.string.man)
                                    TYPE_OF_MIX -> getString(R.string.mix)
                                    TYPE_OF_OTHER -> getString(R.string.call_admin_contact)
                                    else -> getString(R.string.call_admin_contact)
                                }

                            // need uid of location to access update location page
                            updateLocationActivityIntent = Intent(
                                this@DetailLocationActivity,
                                UpdateLocationActivity::class.java
                            ).apply {
                                putExtra(UID, data.uid)
                            }

                            // data for shared item
                            shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${getString(R.string.share_content_base)} ${data.name} ${data.address} ${data.phone} ${data.googlePlace}"
                                )
                                type = "text/plain"
                            }

                            // show image data
                            val slidePhoto = ArrayList<SlideModel>()

                            data.photo?.forEach {
                                slidePhoto.add(SlideModel(it))
                            }
                            imgLocation.setImageList(slidePhoto, ScaleTypes.CENTER_CROP)

                            // show location marker on map
                            mapLocation.getMapAsync { googleMap ->
                                // setup ui and camera position of map
                                googleMap.run {
                                    mapType = GoogleMap.MAP_TYPE_NORMAL

                                    uiSettings.isCompassEnabled = true
                                    uiSettings.isZoomControlsEnabled = true

                                    animateCamera(
                                        CameraUpdateFactory.newCameraPosition(
                                            CameraPosition.Builder().target(
                                                LatLng(
                                                    data.coordinate?.latitude as Double,
                                                    data.coordinate?.longitude as Double
                                                )
                                            ).zoom(15f).build()
                                        )
                                    )

                                    val marker = MarkerOptions().position(
                                        LatLng(
                                            data.coordinate?.latitude as Double,
                                            data.coordinate?.longitude as Double
                                        )
                                    ).title(data.name).snippet(data.address)

                                    // setup custom marker icon
                                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home_marker))

                                    // add marker on map
                                    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                                    addMarker(marker).tag = data.uid

                                    // enable marker click
                                    setOnMarkerClickListener { markerOnClick ->
                                        markerOnClick.showInfoWindow()
                                        false
                                    }

                                    // enable info window click
                                    setOnInfoWindowClickListener { infoWindow ->
                                        infoWindow.tag
                                        infoWindow.title
                                    }
                                }
                            }
                        }
                    }

                }
            }
        })
    }

    // handle success state of view
    private fun showSuccessStateView() {
        detailLocationBinding.animLoading.gone()
        detailLocationBinding.animError.gone()
        detailLocationBinding.detailLayout.visible()
    }

    // handle error state of view
    private fun showErrorStateView() {
        detailLocationBinding.animError.visible()
        detailLocationBinding.animLoading.gone()
        detailLocationBinding.detailLayout.invisible()
    }

    // handle loading state of view
    private fun showLoadingStateView() {
        detailLocationBinding.detailLayout.invisible()
        detailLocationBinding.animLoading.visible()
        detailLocationBinding.animError.gone()
    }

    // initialize menu option
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.detail_location_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_location -> {
                startActivity(updateLocationActivityIntent)
                true
            }
            R.id.share -> {
                startActivity(shareIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // activate back button arrow
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        detailLocationBinding.mapLocation.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        detailLocationBinding.mapLocation.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        detailLocationBinding.mapLocation.onDestroy()
    }
}