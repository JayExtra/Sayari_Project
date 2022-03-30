package com.dev.james.sayariproject.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dev.james.sayariproject.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding : FragmentSettingsBinding? = null
    private val binding get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(
            inflater ,
            container,
            false
        )
        binding?.setUpUi()
        return _binding?.root
    }

    private fun FragmentSettingsBinding.setUpUi(){
        settingsTopAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}