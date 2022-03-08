package com.dev.james.sayariproject.ui.iss

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentIssBinding
import com.dev.james.sayariproject.models.iss.FlightVehicle
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.ui.iss.adapters.CrewRecyclerAdapter
import com.dev.james.sayariproject.ui.iss.adapters.DockedVehiclesAdapter
import com.dev.james.sayariproject.ui.iss.adapters.IssEventsRecyclerAdapter
import com.dev.james.sayariproject.ui.iss.adapters.PartnersRecyclerView
import com.dev.james.sayariproject.ui.iss.viewmodel.IssViewModel
import com.dev.james.sayariproject.utilities.NetworkResource
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

@AndroidEntryPoint
class IssFragment : Fragment() {

    private var _binding: FragmentIssBinding? = null
    private val binding get() = _binding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val issViewModel: IssViewModel by viewModels()

    private val crewRcAdapter = CrewRecyclerAdapter()
    private val partnerRcAdapter = PartnersRecyclerView()
    private val dockedVehiclesAdapter = DockedVehiclesAdapter { vehicle ->
        vehicle?.let {
            navigateToVehicleFragment(vehicle)
        } ?: Log.d("IssFrag" , "No vehicle detected")
    }

    private val eventsAdapter = IssEventsRecyclerAdapter { shareUrl, videoUrl, snackBarMessage ->
        when {
            shareUrl != null -> {
                shareNewsOrVideoUrl(shareUrl)
            }
            videoUrl != null -> {
                goToWebCast(videoUrl)
            }
            snackBarMessage != null -> {
                showSnackBar(snackBarMessage)
            }
            else -> {
                Log.d("IssFrag", "No action invoked from adapter")
            }
        }
    }

    private fun shareNewsOrVideoUrl(shareUrl: String?) {
        Log.d("EventsFrag", "shareNewsOrVideoUrl: share url triggered ")
        shareUrl?.let {
            val shareIntent = Intent().apply {
                this.action = Intent.ACTION_SEND
                this.putExtra(Intent.EXTRA_TEXT, it)
                this.type = "text/plain"
            }
            startActivity(shareIntent)
        }
    }

    private fun goToWebCast(videoUrl: String?) {
        Log.d("EventsFrag", "shareNewsOrVideoUrl: video triggered ")
        videoUrl?.let {
            val vidId = extractVideoId(videoUrl)
            launchYoutubeIntent(vidId[1])
            Log.d("IssFrag", "goToWebCast: video array : $vidId")
        } ?: Log.d("IssFrag", "goToWebCast: no webcast available")

    }

    private fun launchYoutubeIntent(c: Char) {
        val appIntent = Intent().apply {
            this.action = Intent.ACTION_VIEW
            this.putExtra(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$c"))
        }

        val webIntent = Intent().apply {
            this.action = Intent.ACTION_VIEW
            this.putExtra(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=$c"))
        }

        try {
            requireContext().startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            Log.d("IssFrag", "launchYoutubeIntent: ${e.localizedMessage} ")
            requireContext().startActivity(webIntent)
        }
    }

    private fun extractVideoId(videoUrl: String): String {
        val videoArray = videoUrl.split("=")
        return videoArray.toString()
    }

    private fun showSnackBar(snackBarMessage: String?) {
        snackBarMessage?.let { message ->
            binding?.let { Snackbar.make(it.root, message, Snackbar.LENGTH_SHORT).show() }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIssBinding.inflate(
            inflater,
            container,
            false
        )

        binding?.setUpUi()
        loadData()
        binding?.observeChipSelection()
        return binding?.root
    }

    private fun FragmentIssBinding.observeChipSelection() {
        issViewModel.selectedChip.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let { selectedChipString ->
                Log.d("IssFrag", "observeChipSelection: $selectedChipString ")

                when (selectedChipString) {
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

    private fun FragmentIssBinding.setUpUi() {

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
            infoChip, dockedChip, progChip, eventsChip, expChip
        )

        chipList.forEach { chip ->
            chip.setOnCheckedChangeListener { chipButton, isChecked ->
                if (isChecked) issViewModel.getSelectedChip(chipButton.text.toString())
            }
        }

        crewRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = crewRcAdapter
        }
        partnersRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = partnerRcAdapter
        }
        dockedVehiclesRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = dockedVehiclesAdapter
        }

        upcomingEventsRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = eventsAdapter
        }

    }

    private fun FragmentIssBinding.getInitialSelectedChip() {
        val initialChipSelection = parentChipGroup.findViewById<Chip>(parentChipGroup.checkedChipId)
            .text.toString()
        issViewModel.getSelectedChip(initialChipSelection)
    }

    private fun loadData() {
        //fetch data from repository then do necessary UI updates
        lifecycleScope.launchWhenStarted {
            issViewModel.spaceStation.collectLatest { event ->
                event.getContentIfNotHandled()?.let { resource ->
                    when (resource) {
                        is NetworkResource.Loading -> {
                            Log.d("IssFrag", "loadData: loading data... ")
                            binding?.progressBarLoading?.isVisible = true
                        }
                        is NetworkResource.Success -> {
                            Log.d("IssFrag", "loadData: ISS DATA => ${resource.value} ")
                            binding?.progressBarLoading?.isGone = true

                            setUpInfoSection(resource.value)
                            setUpExpeditionSection(resource.value)
                            setUpPartnerSection(resource.value)
                            setUpDockingSection(resource.value)
                            //                        val result = resource.value
                            //                          textView8.text = result.description
                        }
                        is NetworkResource.Failure -> {
                            binding?.progressBarLoading?.isGone = true
                            Log.d("IssFrag", "loadData: ISS DATA => ${resource.errorBody} ")
                        }
                    }

                }
            }
        }

        lifecycleScope.launchWhenStarted {
            issViewModel.spaceStationEvents.collectLatest { event ->
                event.getContentIfNotHandled()?.let { resource ->
                    when (resource) {
                        is NetworkResource.Loading -> {
                            binding?.apply {
                                issEventsProgressBar.isVisible = true
                                netErrorMessageIss.isInvisible = true
                            }
                        }

                        is NetworkResource.Success -> {
                            binding?.apply {
                                issEventsProgressBar.isInvisible = true
                                netErrorMessageIss.isInvisible = true
                                eventsAdapter.submitList(resource.value.results)
                            }
                        }

                        is NetworkResource.Failure -> {
                            binding?.apply {
                                issEventsProgressBar.isInvisible = true
                                netErrorMessageIss.isVisible = true
                            }
                        }
                    }
                }

            }
        }
    }

    private fun setUpDockingSection(value: IntSpaceStation) {
        // load up progress
        //calculate available
        val allDockingPorts = value.dockingLocation.size
        val dockedVehiclesCount = value.dockingLocation.filter { it.docked != null }.size
        val freePorts = allDockingPorts - dockedVehiclesCount

        val percentageCapacity =
            calculateCapacityPercentage((abs(dockedVehiclesCount - freePorts)), allDockingPorts)
        val percentageDocked = calculateCapacityPercentage(dockedVehiclesCount, allDockingPorts)
        val percentageFree = calculateCapacityPercentage(freePorts, allDockingPorts)

        //submit list of docking locations
        val dockedVehicles = value.dockingLocation.filter { it.docked != null }
        dockedVehiclesAdapter.submitList(dockedVehicles)


        binding?.apply {
            dockingPrcntgCapProg.progress = percentageCapacity
            capacityTxtPrcntg.text = "$percentageCapacity% capacity"

            freeCapProgress.progress = percentageFree
            freePortsCapacityTxt.text = freePorts.toString()

            dockedCapProgress.progress = percentageDocked
            dockedCapTxt.text = dockedVehiclesCount.toString()
        }
    }

    private fun setUpPartnerSection(value: IntSpaceStation) {
        partnerRcAdapter.submitList(value.owners)
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

            loadCommanderImage(expeditionCommander[0].astronaut.profile_image)
            crewCountTxt.text = "Crew on-board: ${value.onboardCrew.toString()}"

            activeTxtCommander.isVisible = expeditionCommander[0].astronaut.status.name == "Active"
            activeIndicatorCommander.isVisible =
                expeditionCommander[0].astronaut.status.name == "Active"

            val crewList = value.activeExpeditions[0].crew
            crewRcAdapter.submitList(crewList)

        }
    }

    private fun FragmentIssBinding.loadCommanderImage(image: String) {
        Glide.with(this.root)
            .load(image)
            .centerCrop()
            .error(R.drawable.ic_broken_image)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    commandProgress.isInvisible = true
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    commandProgress.isVisible = false
                    return false
                }
            })
            .into(commanderImg)

    }

    private fun formatDateString(expStartDate: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // only for OREO and newer versions
            val dateFormat = ZonedDateTime.parse(expStartDate)

            val API_TIME_STAMP_PATTERN = "dd-MM-yyyy hh:mm a"

            val dateTimeFormatter: DateTimeFormatter =
                DateTimeFormatter.ofPattern(API_TIME_STAMP_PATTERN, Locale.ROOT)

            val createdDateFormatted = dateFormat.withZoneSameInstant(ZoneId.of("Africa/Nairobi"))

            val formattedDate2 = createdDateFormatted.format(dateTimeFormatter)

            Log.d("IssFrag", "setUpDate: $formattedDate2 ")

            return formattedDate2

        } else {
            val dateFormat: SimpleDateFormat =
                SimpleDateFormat("dd-MM-yyyy'T'h:mm a")
            val eDate: Date = dateFormat.parse(expStartDate)
            Log.d("IssFrag", "eDate: ${eDate.toString()} ")

            return eDate.toString()
        }
    }

    private fun setUpInfoSection(spaceStation: IntSpaceStation) {
        //load space station description
        binding?.apply {
            descTxtExpText.text = spaceStation.description

            if (spaceStation.status.name == "Active") {
                activeIndicator.isVisible = true
                activeTxt.isVisible = true
                activeTxt.text = spaceStation.status.name
            } else {
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
    private fun View.toggleOut(show: Boolean) {
        val transition: Transition = Slide(Gravity.RIGHT)
        transition.duration = 200
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup?, transition)
        this.isVisible = show
    }

    //number increase animation
    private fun TextView.startCountAnimation(count: Int) {
        val animator = ValueAnimator.ofInt(0, count)
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

    private fun calculateCapacityPercentage(d: Int, t: Int): Int {
        return ((d.toDouble() / t) * 100).roundToInt()
    }


    private fun navigateToVehicleFragment(vehicle: FlightVehicle) {
        //navigate to spacecraft fragment
        Log.d("IssFrag", "navigateToVehicleFragment: vehicle => ${vehicle.toString()} ")
        val action = IssFragmentDirections.actionIssFragment2ToSpaceCraftFragment(vehicle)
        findNavController().navigate(action)

    }


}