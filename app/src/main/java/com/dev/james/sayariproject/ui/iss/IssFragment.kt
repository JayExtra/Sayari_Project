package com.dev.james.sayariproject.ui.iss

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.dev.james.sayariproject.databinding.FragmentIssBinding
import com.dev.james.sayariproject.ui.iss.viewmodel.IssViewModel
import com.dev.james.sayariproject.utilities.NetworkResource
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class IssFragment : Fragment() {

    private var _binding : FragmentIssBinding? = null
    private val binding get() = _binding

    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val issViewModel : IssViewModel by viewModels()

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
        binding?.loadData()
        binding?.observeChipSelection()
        return binding?.root
    }

    private fun FragmentIssBinding.observeChipSelection() {
        issViewModel.selectedChip.observe(viewLifecycleOwner , { event ->
            event.getContentIfNotHandled()?.let { selectedChipString ->
                Log.d("IssFrag", "observeChipSelection: $selectedChipString ")

                when(selectedChipString){
                    "Information" -> {
                        infoLayout.isVisible = true
                        expeditionLayout.isGone = true
                        programLayout.isGone = true
                        dockingLayout.isGone = true
                        eventsLayout.isGone = true
                    }
                    "Expedition" -> {
                        expeditionLayout.isVisible = true
                        programLayout.isGone = true
                        dockingLayout.isGone = true
                        eventsLayout.isGone = true
                        infoLayout.isGone = true
                    }
                    "Program" -> {
                        programLayout.isVisible = true
                        dockingLayout.isGone = true
                        eventsLayout.isGone = true
                        infoLayout.isGone = true
                        expeditionLayout.isGone = true
                    }
                    "Docked" -> {
                        dockingLayout.isVisible = true
                        eventsLayout.isGone = true
                        infoLayout.isGone = true
                        expeditionLayout.isGone = true
                        programLayout.isGone = true
                    }
                    "Events" -> {
                        eventsLayout.isVisible = true
                        infoLayout.isGone = true
                        expeditionLayout.isGone = true
                        programLayout.isGone = true
                        dockingLayout.isGone = true
                    }
                    else -> {
                        Log.d("FragId", "observeChipSelection: no selection ")
                    }
                }
            }
        })
    }

    private fun FragmentIssBinding.setUpUi(){

        //get first selected chip on fragment launch
        getInitialSelectedChip()
        //setup controller and navHostFragment
        navController = findNavController()
        appBarConfiguration = AppBarConfiguration(
            navController.graph
        )
        issToolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        //handle chip clicks
        val chipList = listOf(
            infoChip , dockedChip , progChip , eventsChip , expChip
        )

        chipList.forEach { chip ->
            chip.setOnCheckedChangeListener { chipButton, isChecked ->
                if(isChecked) issViewModel.getSelectedChip(chipButton.text.toString())
            }
        }

    }

    private fun FragmentIssBinding.getInitialSelectedChip() {
        val initialChipSelection = parentChipGroup.findViewById<Chip>(parentChipGroup.checkedChipId)
            .text.toString()
        issViewModel.getSelectedChip(initialChipSelection)
    }

    private fun FragmentIssBinding.loadData(){
        //fetch data from repository then do necessary UI updates
        lifecycleScope.launchWhenStarted {
            issViewModel.spaceStation.collectLatest { event ->
                event.getContentIfNotHandled()?.let { resource ->
                    when(resource){
                        is NetworkResource.Loading -> {
                            Log.d("IssFrag", "loadData: loading data... ")
                        }
                        is NetworkResource.Success -> {
                            Log.d("IssFrag", "loadData: ISS DATA => ${resource.value} ")
    //                        val result = resource.value
  //                          textView8.text = result.description
                        }
                        is NetworkResource.Failure -> {
                            Log.d("IssFrag", "loadData: ISS DATA => ${resource.errorBody} ")
                        }
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