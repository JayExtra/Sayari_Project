package com.dev.james.sayariproject.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentSettingsBinding
import com.dev.james.sayariproject.ui.settings.viewmodel.SettingsViewModel
import com.dev.james.sayariproject.utilities.PRIVACY_POLICY_LINK
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding : FragmentSettingsBinding? = null
    private val binding get() = _binding

    private val settingsViewModel : SettingsViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun FragmentSettingsBinding.setUpUi(){
        //set up all common ui stuff here
        settingsTopAppBar.title = getString(R.string.application_name)
        settingsTopAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        favCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                settingsViewModel.setFavouritesNotificationValue(true)
            }else{
                settingsViewModel.setFavouritesNotificationValue(false)
            }
        }

        fiveminCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                settingsViewModel.setFiveMinuteValue(true)
            }else{
                settingsViewModel.setFiveMinuteValue(false)
            }
        }
        thrtyminCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                settingsViewModel.setThirtyMinValue(true)
            }else{
                settingsViewModel.setThirtyMinValue(false)
            }
        }
        fftnMinCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                settingsViewModel.setFifteenMinuteStatus(true)
            }else{
                settingsViewModel.setFifteenMinuteStatus(false)
            }
        }
        lightDarkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                settingsViewModel.setLightDarkModeValue(true)
                lightDarkModeTxt.text = "Dark mode"
                lightDarkModeImg.setImageResource(R.drawable.ic_baseline_dark_mode)
            }else{
              //  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                settingsViewModel.setLightDarkModeValue(false)
                lightDarkModeTxt.text = "Light mode"
                lightDarkModeImg.setImageResource(R.drawable.ic_baseline_light_mode)
            }
        }

        privacyPolicyBtn.setOnClickListener {
            val intent = Intent().apply {
                this.action = Intent.ACTION_VIEW
                this.data = Uri.parse(PRIVACY_POLICY_LINK)
            }
            startActivity(intent)
        }

        notificationsSwitch.setOnCheckedChangeListener { switch, isChecked ->
            if(
                ContextCompat.checkSelfPermission(requireContext() , Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
            ){
                if(isChecked){
                    settingsViewModel.setNotificationStatus(true)
                }else {
                    settingsViewModel.setNotificationStatus(false)
                }
            }else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Enable notifications")
                    .setMessage("Seems that you have not yet enabled notifications. You will be redirected to the settings screen to enable.")
                    .setPositiveButton("Okay") { dialog , _ ->
                        //send user to settings screen and allow notifications
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS ,
                            Uri.fromParts("package" , requireContext().packageName , null)
                        ).also { requireActivity().startActivity(it) }
                        dialog.dismiss()
                    }
                    .show()

            }
        }

        setupFavouritesCard.setOnClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragment2ToFavouritesFragment()
            findNavController().navigate(action)
        }

       observePreferencesChanges()
    }
    //setup the checkboxes and switches
    private fun FragmentSettingsBinding.observePreferencesChanges(){
        settingsViewModel.settingsPreferencesFlow.observe(viewLifecycleOwner) {
            thrtyminCheckBox.isChecked = it.thirtyMinStatus
            fftnMinCheckBox.isChecked = it.fifteenMinStatus
            fiveminCheckbox.isChecked = it.fiveMinStatus
            favCheckBox.isChecked = it.favouriteAgencies
            lightDarkModeSwitch.isChecked = it.nightDarkStatus
            notificationsSwitch.isChecked = it.showNotifications
            notificationsSwitch.text = if(it.showNotifications) "Yes" else "No"

            if (it.nightDarkStatus) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                lightDarkModeTxt.text = "Dark mode"
                lightDarkModeImg.setImageResource(R.drawable.ic_baseline_dark_mode)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                lightDarkModeTxt.text = "Light mode"
                lightDarkModeImg.setImageResource(R.drawable.ic_baseline_light_mode)
            }



        }
    }

}