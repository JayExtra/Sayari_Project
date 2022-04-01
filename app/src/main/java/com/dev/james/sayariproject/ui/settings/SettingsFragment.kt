package com.dev.james.sayariproject.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentSettingsBinding
import com.dev.james.sayariproject.ui.settings.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding : FragmentSettingsBinding? = null
    private val binding get() = _binding

    private val settingsViewModel : SettingsViewModel by viewModels()

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
        //set up all common ui stuff here
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
        lightDarkModeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
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

       observePreferencesChanges()
    }
    //setup the checkboxes and switches
    private fun FragmentSettingsBinding.observePreferencesChanges(){
        settingsViewModel.settingsPreferencesFlow.observe(viewLifecycleOwner , {
            thrtyminCheckBox.isChecked = it.thirtyMinStatus
            fftnMinCheckBox.isChecked = it.fifteenMinStatus
            fiveminCheckbox.isChecked = it.fiveMinStatus
            favCheckBox.isChecked = it.favouriteAgencies
            lightDarkModeSwitch.isChecked = it.nightDarkStatus

            if(it.nightDarkStatus){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                lightDarkModeTxt.text = "Dark mode"
                lightDarkModeImg.setImageResource(R.drawable.ic_baseline_dark_mode)
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                lightDarkModeTxt.text = "Light mode"
                lightDarkModeImg.setImageResource(R.drawable.ic_baseline_light_mode)

            }

        })
    }

}