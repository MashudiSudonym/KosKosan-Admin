package c.m.koskosanadmin.ui.location.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.FragmentLocationBinding
import c.m.koskosanadmin.ui.form.add.location.AddLocationActivity
import c.m.koskosanadmin.ui.location.detail.DetailLocationActivity
import c.m.koskosanadmin.util.Constants.UID
import c.m.koskosanadmin.util.ViewUtilities.gone
import c.m.koskosanadmin.util.ViewUtilities.invisible
import c.m.koskosanadmin.util.ViewUtilities.visible
import c.m.koskosanadmin.vo.ResponseState
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocationFragment : Fragment() {

    private val locationViewModel: LocationViewModel by viewModel()
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding as FragmentLocationBinding
    private lateinit var locationAdapter: LocationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // app bar setup
        (activity as AppCompatActivity?)?.setSupportActionBar(binding.toolbarLocation)
        (activity as AppCompatActivity?)?.supportActionBar?.apply {
            title = getString(R.string.title_location)
        }

        // initialize recyclerview adapter
        locationAdapter = LocationAdapter { locationResponse ->
            val detailLocationActivityIntent =
                Intent(requireActivity(), DetailLocationActivity::class.java).apply {
                    putExtra(UID, locationResponse.uid)
                }

            startActivity(detailLocationActivityIntent)
        }

        // initialize get location data
        initializeGetLocationData()

        // swipe refresh function
        binding.locationSwipeRefreshView.setOnRefreshListener {
            binding.locationSwipeRefreshView.isRefreshing = false
            initializeGetLocationData()
        }

        // fab button function
        binding.fabAddNewLocation.setOnClickListener {
            val addLocationActivityIntent = Intent(requireActivity(), AddLocationActivity::class.java)

            startActivity(addLocationActivityIntent)
        }
    }

    // initialize get location data
    private fun initializeGetLocationData() {
        locationViewModel.getLocationByOwnerUID().observe(viewLifecycleOwner, { response ->
            if (response != null) when (response) {
                is ResponseState.Error -> showErrorStateView() // error state
                is ResponseState.Loading -> showLoadingStateView() // loading state
                is ResponseState.Success -> {
                    // success state
                    showSuccessStateView()

                    // add data to recyclerview adapter
                    locationAdapter.submitList(response.data)
                    binding.rvLocation.adapter = locationAdapter
                    binding.rvLocation.setHasFixedSize(true)
                }
            }
        })
    }

    // handle success state of view
    private fun showSuccessStateView() {
        binding.animLoading.gone()
        binding.animError.gone()
        binding.locationLayout.visible()
    }

    // handle error state of view
    private fun showErrorStateView() {
        binding.animError.visible()
        binding.animLoading.gone()
        binding.locationLayout.invisible()
    }

    // handle loading state of view
    private fun showLoadingStateView() {
        binding.locationLayout.invisible()
        binding.animLoading.visible()
        binding.animError.gone()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}