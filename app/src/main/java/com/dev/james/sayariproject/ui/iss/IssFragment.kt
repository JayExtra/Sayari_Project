package com.dev.james.sayariproject.ui.iss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.dev.james.sayariproject.databinding.FragmentIssBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IssFragment : Fragment() {

    private var _binding : FragmentIssBinding? = null
    private val binding get() = _binding

    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIssBinding.inflate(
            inflater ,
            container ,
            false
        )

        binding?.setUpUi()
        return binding?.root
    }

    private fun FragmentIssBinding.setUpUi(){
        //setup controller and navHostFragment
        navController = findNavController()
        appBarConfiguration = AppBarConfiguration(
            navController.graph
        )
        issToolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}