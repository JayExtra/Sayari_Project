package com.dev.james.sayariproject.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentSendEmailBinding
import com.dev.james.sayariproject.utilities.DEV_EMAIL
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
        emailToolbar.title = getString(R.string.compsose_txt)

        sendEmailButton.setOnClickListener {
            //start email sending process
            val subject = subjectTextInput.text.toString().trim()
            val message = messageTextInput.text.toString().trim()

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
                        startEmailIntent(it.subject , it.mail)

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

    private fun startEmailIntent(subject: String?, mail: String?) {
        Log.d("StartEmail", "startEmailIntent: subject: $subject , mail: $mail")
        val mailIntent = Intent().apply{
            this.data = Uri.parse("mailto:")
            this.putExtra(Intent.EXTRA_EMAIL , arrayOf(DEV_EMAIL.trim()) )
            this.putExtra(Intent.EXTRA_SUBJECT , subject)
            this.putExtra(Intent.EXTRA_TEXT , mail)
        }
        try {
            startActivity(mailIntent)
        }catch (e : ActivityNotFoundException){
            Toast.makeText(requireContext(), "no email application installed", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}