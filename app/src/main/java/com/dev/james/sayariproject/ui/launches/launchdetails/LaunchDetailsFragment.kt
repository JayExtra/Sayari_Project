package com.dev.james.sayariproject.ui.launches.launchdetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentLaunchDetailsBinding
import com.dev.james.sayariproject.models.launch.LaunchList
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LaunchDetailsFragment : Fragment(R.layout.fragment_launch_details) {

    private var _binding : FragmentLaunchDetailsBinding? = null
    private val binding get() = _binding

    private val arguments : LaunchDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLaunchDetailsBinding.inflate(inflater , container , false)
        val args = arguments.launch
        binding?.setUpUi(args)
        return binding?.root
    }

    private fun FragmentLaunchDetailsBinding.setUpUi(args: LaunchList) {
        launchToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        Log.d("LaunchDetails", "setUpUi: argument: ${args.toString()} ")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}