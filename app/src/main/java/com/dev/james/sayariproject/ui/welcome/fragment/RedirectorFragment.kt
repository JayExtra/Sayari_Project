package com.dev.james.sayariproject.ui.welcome.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmntRedirectorBinding
import com.dev.james.sayariproject.ui.base.BaseFragment
import com.dev.james.sayariproject.ui.welcome.viewmodel.WelcomeScreenViewModel
import kotlinx.coroutines.flow.collectLatest

class RedirectorFragment : BaseFragment<FragmntRedirectorBinding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmntRedirectorBinding::inflate

    private val viewModel : WelcomeScreenViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkOnBoardingStatusTest()
    }

    private fun checkOnBoardingStatus() {
        lifecycleScope.launchWhenStarted {
            viewModel.onBoardingValue.collectLatest { value ->
                if (value){
                    findNavController().navigate(R.id.action_redirectorFragment_to_homeFragment)
                    Log.d("RedirectFrag", "checkOnBoardingStatus: result ${value.toString()} should go to home fragment ")
                }else{
                    findNavController().navigate(R.id.action_redirectorFragment_to_welcomeScreenFragment)
                    Log.d("RedirectFrag", "checkOnBoardingStatus: result ${value.toString()} should go to on boarding ")

                }
            }
        }
    }

    private fun checkOnBoardingStatusTest(){
        viewModel.onBrdingValue.observe(viewLifecycleOwner , { value ->
            if (value){
                findNavController().navigate(R.id.action_redirectorFragment_to_homeFragment)
                Log.d("RedirectFrag", "checkOnBoardingStatus: result ${value.toString()} should go to home fragment ")
            }else{
                findNavController().navigate(R.id.action_redirectorFragment_to_welcomeScreenFragment)
                Log.d("RedirectFrag", "checkOnBoardingStatus: result ${value.toString()} should go to on boarding ")

            }
        })
    }


}