package com.dev.james.sayariproject.ui.iss

import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentIssBinding
import com.dev.james.sayariproject.models.astronaut.Astronaut
import com.dev.james.sayariproject.models.iss.FlightVehicle
import com.dev.james.sayariproject.models.iss.IntSpaceStation
import com.dev.james.sayariproject.ui.iss.adapters.CrewRecyclerAdapter
import com.dev.james.sayariproject.ui.iss.adapters.DockedVehiclesAdapter
import com.dev.james.sayariproject.ui.iss.adapters.IssEventsRecyclerAdapter
import com.dev.james.sayariproject.ui.iss.adapters.PartnersRecyclerView
import com.dev.james.sayariproject.ui.iss.viewmodel.IssViewModel
import com.dev.james.sayariproject.utilities.NetworkResource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class IssFragment : Fragment() {

    private var _binding: FragmentIssBinding? = null
    private val binding get() = _binding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val issViewModel: IssViewModel by viewModels()

    private val crewRcAdapter = CrewRecyclerAdapter{ astroId ->
        getAstronaut(astroId)
    }

    private fun getAstronaut(astroId: Int) {
        issViewModel.getAstronaut(astroId)
        Toast.makeText(requireContext(), "showing astronaut details..", Toast.LENGTH_LONG).show()
    }

    private val partnerRcAdapter = PartnersRecyclerView()
    private val dockedVehiclesAdapter = DockedVehiclesAdapter { vehicle ->
        vehicle?.let {
          //  Log.d("IssFragment", "vehicle selected: ${it.launch.program[1]}")
           navigateToVehicleFragment(vehicle)
        } ?: Log.d("IssFrag" , "No vehicle detected")
    }

    private val eventsAdapter = IssEventsRecyclerAdapter { shareUrl, videoUrl, snackBarMessage ,event ->
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
            event != null -> {
                val action  = IssFragmentDirections.actionIssFragment2ToEventsDetailsFragment(event)
                findNavController().navigate(action)
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
            launchYoutubeIntent(videoUrl)
            Log.d("IssFrag", "goToWebCast: video array : $vidId")
        } ?: Log.d("IssFrag", "goToWebCast: no webcast available")

    }

    private fun launchYoutubeIntent(c: String?) {
        val appIntent = Intent(Intent.ACTION_VIEW).apply {
            this.data = Uri.parse(c)
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val webIntent = Intent( Intent.ACTION_VIEW).apply {
            this.data =  Uri.parse(c)
        }

        try {
            requireContext().startActivity(appIntent)
        }catch (e : ActivityNotFoundException){
            Log.d("EventFrag", "launchYoutubeIntent: ${e.localizedMessage} ")
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
        binding?.setUpObservables()
        loadData()
        binding?.observeChipSelection()
        return binding?.root
    }

    private fun FragmentIssBinding.setUpObservables() {
        //observable for our docked vehicles statistics
        issViewModel.dockedVehiclesStats.observe(viewLifecycleOwner) { dockedVehiclesStats ->
            //feed ui
            dockingPrcntgCapProg.progress = dockedVehiclesStats.percentageCapacity
            capacityTxtPrcntg.text = "${dockedVehiclesStats.percentageCapacity}% capacity"

            freeCapProgress.progress = dockedVehiclesStats.percentageFreePorts
            freePortsCapacityTxt.text = dockedVehiclesStats.freePorts.toString()

            dockedCapProgress.progress = dockedVehiclesStats.percentageDockedVehicle
            dockedCapTxt.text = dockedVehiclesStats.totalDockedVehicles.toString()

        }

        collectAstronautSharedFlow()
    }

    private fun FragmentIssBinding.observeChipSelection() {
        issViewModel.selectedChip.observe(viewLifecycleOwner) { event ->
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
        }
    }

    private fun FragmentIssBinding.setUpUi() {

        //get first selected chip on fragment launch
        getInitialSelectedChip()
        //setup controller and navHostFragment
        (activity as AppCompatActivity).setSupportActionBar(issToolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.title= getString(R.string.application_name)
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
            issViewModel.spaceStation.collectLatest { resource ->
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
    /**    // load up progress
        //calculate available
        val allDockingPorts = value.dockingLocation.size
        val dockedVehiclesCount = value.dockingLocation.filter { it.docked != null }.size
        val freePorts = allDockingPorts - dockedVehiclesCount

        val percentageCapacity =
            calculateCapacityPercentage((abs(dockedVehiclesCount - freePorts)), allDockingPorts)
        val percentageDocked = calculateCapacityPercentage(dockedVehiclesCount, allDockingPorts)
        val percentageFree = calculateCapacityPercentage(freePorts, allDockingPorts)

    binding?.apply {
    dockingPrcntgCapProg.progress = percentageCapacity
    capacityTxtPrcntg.text = "$percentageCapacity% capacity"

    freeCapProgress.progress = percentageFree
    freePortsCapacityTxt.text = freePorts.toString()

    dockedCapProgress.progress = percentageDocked
    dockedCapTxt.text = dockedVehiclesCount.toString()
    }

    **/
    issViewModel.getDockedVehiclesStatistics(value)

        //submit list of docking locations
        val dockedVehicles = value.dockingLocation.filter { it.docked != null }
        dockedVehiclesAdapter.submitList(dockedVehicles)

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

    private fun collectAstronautSharedFlow(){
        //collect astronaut data and show dialog
        lifecycleScope.launchWhenStarted {
            issViewModel.showAstronaut.collectLatest { resource ->
                when(resource){
                    is NetworkResource.Loading -> {
                        //show some loading bar but for now no need
                        Log.d("IssFrag", "collectAstronautSharedFlow: fetching astronaut data... ")
                    }
                    is NetworkResource.Success -> {
                        //show dialog
                        showAstronautDialog(resource.value)
                    }
                    is NetworkResource.Failure -> {
                        val error = resource.errorBody
                        error?.let {
                            binding?.root?.let { it1 ->
                                Snackbar.make(
                                    it1,
                                    "could not get astronaut data : $error",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

            }
        }

    }

    private fun showAstronautDialog(astronaut: Astronaut) {

        //launch dialog
        val bottomSheetDialog = BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialogTheme
        )
        val bottomSheetView = LayoutInflater.from(activity?.applicationContext).inflate(
            R.layout.dialog_astronaut,
            binding?.root ,
            false
        )

        val astroImageView = bottomSheetView.findViewById<ImageView>(R.id.astronautImage)
        val astroNameTv = bottomSheetView.findViewById<TextView>(R.id.astronautNameTxt)
        val astroAgencyTv = bottomSheetView.findViewById<TextView>(R.id.astroAgencyTxt)
        val astroStatusTv = bottomSheetView.findViewById<TextView>(R.id.astroStatusTxt)
        val astroDobTv = bottomSheetView.findViewById<TextView>(R.id.astroDobTxt)
        val astroCountryTv = bottomSheetView.findViewById<TextView>(R.id.astroCountryTxt)
        val astroBioTv = bottomSheetView.findViewById<TextView>(R.id.astroBioTxt)
        val astroTwitter = bottomSheetView.findViewById<ImageView>(R.id.twitterIcon)
        val astroInsta = bottomSheetView.findViewById<ImageView>(R.id.instagramIcon)
        val astroWiki = bottomSheetView.findViewById<ImageView>(R.id.astroWikipediaImgView)
        val closeDialog = bottomSheetView.findViewById<Button>(R.id.closeDialogTxt)
        val totalFlightsTxt = bottomSheetView.findViewById<TextView>(R.id.totalFlightsCountTxt)
        val totalLandingsTxt = bottomSheetView.findViewById<TextView>(R.id.totalLandingsCountTxt)
        val astroLearnMoreBtn = bottomSheetView.findViewById<Button>(R.id.astroLearnMore)

        //total count
        totalFlightsTxt.text = astronaut.flights.size.toString()
        //total landings
        totalLandingsTxt.text = astronaut.landings.size.toString()

   //     Log.d("IssFrag", "showAstronautDialog: ${astronaut.landings}")
        Log.d("IssFrag", "showAstronautDialog: ${astronaut.instagram}")

        //setup the rest of the views
        astroNameTv.text = astronaut.name
        astroAgencyTv.text = astronaut.agency.name
        astroStatusTv.text = astronaut.status.name
        astroDobTv.text = "DoB: ${astronaut.date_of_birth}"
        astroCountryTv.text = "Nationality: ${astronaut.nationality}"
        astroBioTv.text = astronaut.bio
        setImage(astronaut , astroImageView)

        closeDialog.setOnClickListener {
            //close dialog
            bottomSheetDialog.dismiss()
        }
        astroLearnMoreBtn.setOnClickListener{
            goToWikiPage(astronaut.wiki)
        }

        astroInsta.setOnClickListener{
            val profileUrl = astronaut.instagram
            if(profileUrl!=null){
                goToInstagram(profileUrl)
            }else{
                Toast.makeText(requireContext(), "${astronaut.name} does not have an instagram account", Toast.LENGTH_SHORT).show()

            }
        }

        astroTwitter.setOnClickListener {
            //show twitter profile
            if(astronaut.twitter != null){
                goToTwitter(astronaut.twitter)
                Log.d("IssFrag", "showAstronautDialog: twitter profile: ${astronaut.twitter}")
            }else{
                Toast.makeText(requireContext(), "${astronaut.name} does not have a twitter account", Toast.LENGTH_SHORT).show()
            }
        }

        astroWiki.setOnClickListener {
            //go to astronaut wiki page
            goToWikiPage(astronaut.wiki)
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

    }

    private fun goToTwitter(profileUrl: String) {
        //launch twitter web or app if installed
      val username = profileUrl.substring(profileUrl.lastIndexOf("/") + 1)
        Log.d("IssFrag", "goToTwitter: username = $username  ")
        try{
            val twitterIntent = Intent(Intent.ACTION_VIEW).apply {
                this.data = Uri.parse("https://twitter.com/$username")
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(twitterIntent)
        }catch (e : ActivityNotFoundException) {
            Log.d("IssFrag", "goToTwitter: no twitter app found. $e")
            val twitterWebIntent = Intent().apply {
                this.data = Uri.parse(profileUrl)
            }
            Toast.makeText(requireContext(), "opening twitter web", Toast.LENGTH_SHORT).show()
            startActivity(twitterWebIntent)
        }
    }

    private fun goToWikiPage(wikiUrl : String){
        val wikiWebIntent = Intent().apply {
            this.data = Uri.parse(wikiUrl)
        }
        Toast.makeText(requireContext(), "opening wikipedia page", Toast.LENGTH_SHORT).show()
        startActivity(wikiWebIntent)
    }

    private fun setImage(astronaut: Astronaut, astroImageView: ImageView?) {

        astroImageView?.let {
            Glide.with(requireContext())
                .load(astronaut.profile_image)
                .centerCrop()
                .placeholder(R.drawable.sayari_logo2)
                .error(R.color.grey)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(it)
        }


    }

    private fun goToInstagram(url : String){
        //start intent to instagram app , if not installed go to browser web app
        try{
                var newUrl = url
                if(newUrl.endsWith("/")){
                    newUrl = url.substring(0,url.length - 1)
                }
                val username = newUrl.substring(newUrl.lastIndexOf("/") + 1)
                Log.d("IssFrag", "goToInstagram: $username")
                val instaIntent = Intent(Intent.ACTION_VIEW).apply{
                    this.data = Uri.parse("http://instagram.com/_u/$username")
                    this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                Toast.makeText(requireContext(), "opening instagram app", Toast.LENGTH_SHORT).show()
                startActivity(instaIntent)
            
        }catch ( e : ActivityNotFoundException) {
            val instaWebIntent = Intent().apply {
                this.data = Uri.parse(url)
            }
            Toast.makeText(requireContext(), "opening instagram web", Toast.LENGTH_SHORT).show()
            startActivity(instaWebIntent)
        }

    }

}