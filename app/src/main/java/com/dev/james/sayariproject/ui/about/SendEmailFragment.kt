package com.dev.james.sayariproject.ui.about

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dev.james.sayariproject.databinding.FragmentSendEmailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SendEmailFragment : Fragment(){
    private var _binding : FragmentSendEmailBinding? = null
    private val binding get() = _binding
    private lateinit var navController: NavController

    private val emailViewModel : EmailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSendEmailBinding.inflate(inflater , container , false)
        binding?.setUpUi()
        binding?.collectValidationStatus()
        return binding?.root
    }

    private fun FragmentSendEmailBinding.setUpUi(){
        navController = findNavController()
        emailToolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        sendEmailButton.setOnClickListener {
            //start email sending process
            val subject = subjectTextInput.text.toString()
            val message = messageTextInput.text.toString()

            emailViewModel.validateAndSendEmail(subject , message)
        }

    }

    private fun FragmentSendEmailBinding.collectValidationStatus(){
        lifecycleScope.launchWhenStarted {
            emailViewModel.validationSharedFlow.collectLatest {
                when (it.code) {
                    0 -> {
                        Log.d("EmailFrag", "collectValidationStatus: validation good ")
                        subjectInputLayout.isErrorEnabled = false
                        messageInputLayout.isErrorEnabled = false

                        //begin email sending process
                    }
                    1 -> {
                        subjectInputLayout.isErrorEnabled = true
                        subjectInputLayout.error = it.message
                    }
                    2 -> {
                        messageInputLayout.isErrorEnabled = true
                        messageInputLayout.error = it.message
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}