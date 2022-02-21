package com.dev.james.sayariproject.ui.iss

import android.animation.ValueAnimator
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentIssBinding
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.ui.iss.viewmodel.IssViewModel
import com.dev.james.sayariproject.utilities.NetworkResource
import com.google.android.material.chip.Chip
import com.ms.square.android.expandabletextview.ExpandableTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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
                        infoLayout.toggleOut(true)
                        expeditionLayout.toggleOut(false)
                        programLayout.toggleOut(false)
                        dockingLayout.toggleOut(false)
                        eventsLayout.toggleOut(false)
                    }
                    "Expedition" -> {
                        expeditionLayout.toggleOut(true)
                        programLayout.toggleOut(false)
                        dockingLayout.toggleOut(false)
                        eventsLayout.toggleOut(false)
                        infoLayout.toggleOut(false)
                    }
                    "Program" -> {
                        programLayout.toggleOut(true)
                        dockingLayout.toggleOut(false)
                        eventsLayout.toggleOut(false)
                        infoLayout.toggleOut(false)
                        expeditionLayout.toggleOut(false)
                    }
                    "Docked" -> {
                        dockingLayout.toggleOut(true)
                        eventsLayout.toggleOut(false)
                        infoLayout.toggleOut(false)
                        expeditionLayout.toggleOut(false)
                        programLayout.toggleOut(false)
                    }
                    "Events" -> {
                        eventsLayout.toggleOut(true)
                        infoLayout.toggleOut(false)
                        expeditionLayout.toggleOut(false)
                        programLayout.toggleOut(false)
                        dockingLayout.toggleOut(false)
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
                            setUpInfoSection(resource.value)
                            setUpExpeditionSection(resource.value)
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

    private fun setUpExpeditionSection(value: IntSpaceStation) {
        binding?.apply {
            val expStartDate = value.activeExpeditions[0].start
            val formattedDate = formatDateString(expStartDate)
            expStartDateTxt.text = "Started: $formattedDate"
            expeditionTxt.text = value.activeExpeditions[0].name

            val expeditionCommander = value.activeExpeditions[0].crew.filter {
                it.role.id == 1
            }
            commanderTxt.text = expeditionCommander[0].astronaut.name
            commanderAgency.text = expeditionCommander[0].astronaut.agency.name




        }
    }

    private fun formatDateString(expStartDate: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // only for OREO and newer versions
            val dateFormat = ZonedDateTime.parse(expStartDate)

            val API_TIME_STAMP_PATTERN = "dd-MM-yyyy hh:mm a"

            val dateTimeFormatter : DateTimeFormatter =
                DateTimeFormatter.ofPattern(API_TIME_STAMP_PATTERN, Locale.ROOT)

            val createdDateFormatted = dateFormat.withZoneSameInstant(ZoneId.of("Africa/Nairobi"))

            val formattedDate2 = createdDateFormatted.format(dateTimeFormatter)

            Log.d("IssFrag", "setUpDate: $formattedDate2 ")

            return formattedDate2

        }else {
            val dateFormat : SimpleDateFormat =
                SimpleDateFormat("dd-MM-yyyy'T'h:mm a")
            val eDate : Date = dateFormat.parse(expStartDate)
            Log.d("IssFrag", "eDate: ${eDate.toString()} ")

            return eDate.toString()
        }
    }

    private fun setUpInfoSection(spaceStation: IntSpaceStation) {
        //load space station description
        binding?.apply {
            descTxtExpText.text = spaceStation.description

            if(spaceStation.status.name == "Active"){
                activeIndicator.isVisible = true
                activeTxt.isVisible = true
                activeTxt.text = spaceStation.status.name
            }else {
                activeIndicator.isVisible = false
                activeTxt.isInvisible = true
                activeTxt.text = getString(R.string.de_orbited_txt)
            }
            launchDateTxt.isVisible = true
            launchDateTxt.text = "Launched: ${spaceStation.founded}"

            heightTxt.startCountAnimation(spaceStation.height.toInt())
            widthTxt2.startCountAnimation(spaceStation.width.toInt())
            weightTxt.startCountAnimation(spaceStation.mass.toInt())
            cubMtrsTxt.startCountAnimation(spaceStation.volume.toInt())

        }
    }

    //toggles layout in or out
    private fun View.toggleOut(show : Boolean){
        val transition : Transition = Slide(Gravity.RIGHT)
        transition.duration = 200
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup? , transition)
        this.isVisible = show
    }

    //number increase animation
    private fun TextView.startCountAnimation(count : Int) {
        val animator = ValueAnimator.ofInt(0 , count)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            if (animation != null) {
                this.text = animation.animatedValue.toString()
            }
        }
        animator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}