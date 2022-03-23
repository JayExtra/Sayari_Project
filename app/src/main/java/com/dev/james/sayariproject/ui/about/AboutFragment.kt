package com.dev.james.sayariproject.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.dev.james.sayariproject.BuildConfig
import com.dev.james.sayariproject.databinding.FragmentAboutBinding
import com.dev.james.sayariproject.utilities.TWITTER_PROFILE
import com.dev.james.sayariproject.utilities.TWITTER_PROFILE_WEB
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : Fragment() {
    private var _binding : FragmentAboutBinding? = null
    private val binding get() = _binding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutBinding.inflate(inflater , container , false)
        binding?.setupUi()
        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun FragmentAboutBinding.setupUi(){
        navController = findNavController()
        aboutToolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        appVersionNumberTxt.text = BuildConfig.VERSION_NAME


        twitterButton.setOnClickListener {
         //launch twitter app showing profile
            //if no app present launch browser to twitter web app
            launchTwitter()
        }

        gmailButton.setOnClickListener {
            //navigate to email fragment
            val action = AboutFragmentDirections.actionAboutFragmentToSendEmailFragment()
            navController.navigate(action)
        }

    }
    private fun launchTwitter(){
        val twitterAppIntent = Intent().apply {
            this.putExtra(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=$TWITTER_PROFILE"))
        }
        val twitterWebIntent = Intent().apply {
            this.data = Uri.parse(TWITTER_PROFILE_WEB)
        }

        try {
            requireContext().startActivity(twitterAppIntent)
        }catch (e : ActivityNotFoundException){
            Log.d("AboutFragment", "launchTwiterIntent: ${e.localizedMessage} ")
            requireContext().startActivity(twitterWebIntent)
        }
    }
}