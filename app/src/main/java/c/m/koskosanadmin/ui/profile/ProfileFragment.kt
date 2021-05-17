package c.m.koskosanadmin.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.FragmentProfileBinding
import c.m.koskosanadmin.ui.login.LoginActivity
import com.firebase.ui.auth.AuthUI

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
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

        binding.tvTitle.setOnClickListener {
            AuthUI.getInstance().signOut(requireActivity())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val loginActivityIntent =
                            Intent(requireActivity(), LoginActivity::class.java)

                        requireActivity().finish()
                        startActivity(loginActivityIntent)
                    }
                }
        }
    }
}