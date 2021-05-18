package c.m.koskosanadmin.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import c.m.koskosanadmin.R
import c.m.koskosanadmin.data.model.UserResponse
import c.m.koskosanadmin.databinding.FragmentProfileBinding
import c.m.koskosanadmin.ui.about.AboutActivity
import c.m.koskosanadmin.ui.login.LoginActivity
import c.m.koskosanadmin.util.ViewUtilities.gone
import c.m.koskosanadmin.util.ViewUtilities.invisible
import c.m.koskosanadmin.util.ViewUtilities.loadImageWithCoil
import c.m.koskosanadmin.util.ViewUtilities.visible
import c.m.koskosanadmin.vo.ResponseState
import com.firebase.ui.auth.AuthUI
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModel()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // appbar title setup
        (activity as AppCompatActivity?)?.setSupportActionBar(binding.toolbarProfile)
        (activity as AppCompatActivity?)?.supportActionBar?.apply {
            title = getString(R.string.title_profile)
        }
        setHasOptionsMenu(true)

        // show user profile data to view
        initializeGetUserProfileData()

        // swipe refresh function
        binding.profileSwipeRefreshView.setOnRefreshListener {
            binding.profileSwipeRefreshView.isRefreshing = false
            initializeGetUserProfileData()
        }

        // button for open about this application activity
        binding.btnInfoApplication.setOnClickListener {
            val aboutActivityIntent = Intent(requireActivity(), AboutActivity::class.java)
            startActivity(aboutActivityIntent)
        }
    }

    // function for get user profile data and observing the state of data
    private fun initializeGetUserProfileData() {
        profileViewModel.getUserProfile().observe(viewLifecycleOwner, { response ->
            if (response != null) when (response) {
                is ResponseState.Error -> showErrorStateView() // show error state view
                is ResponseState.Loading -> showLoadingStateView() // show loading state view
                is ResponseState.Success -> {
                    // show success load data state view
                    showSuccessStateView()

                    // set response data to view
                    initializeDataToView(response)
                }
            }
        })
    }

    // parsing data to widget view
    @SuppressLint("SetTextI18n")
    private fun initializeDataToView(response: ResponseState<UserResponse>) {
        loadImageWithCoil(binding.imgProfile, response.data?.imageProfile.toString())
        binding.tvName.text = response.data?.name
        binding.tvEmail.text = response.data?.email
        binding.tvAddress.text = getString(R.string.address) + ": " + response.data?.address
        binding.tvPhone.text = getString(R.string.phone) + ": " + response.data?.phoneNumber
    }

    // handle success state of view
    private fun showSuccessStateView() {
        binding.animLoading.gone()
        binding.animError.gone()
        binding.profileLayout.visible()
    }

    // handle error state of view
    private fun showErrorStateView() {
        binding.animError.visible()
        binding.animLoading.gone()
        binding.profileLayout.invisible()
    }

    // handle loading state of view
    private fun showLoadingStateView() {
        binding.profileLayout.invisible()
        binding.animLoading.visible()
        binding.animError.gone()
    }

    // initialize option menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
    }

    // give a action for menu items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_profile -> {
//                val intentUpdateUserProfileActivity =
//                    Intent(requireContext(), UpdateUserProfileActivity::class.java)
//                startActivity(intentUpdateUserProfileActivity)
                true
            }
            R.id.log_out -> {
                // user Log Out
                AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Finish this activity
                            requireActivity().finish()

                            // return to login in activity
                            val loginActivityIntent =
                                Intent(requireContext(), LoginActivity::class.java)
                            startActivity(loginActivityIntent)
                        }
                    }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}