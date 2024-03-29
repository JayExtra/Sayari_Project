package com.dev.james.sayariproject.ui.launches.launchdetails

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentLaunchDetailsBinding
import com.dev.james.sayariproject.models.launch.Agency
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.models.launch.Mission
import com.dev.james.sayariproject.utilities.NetworkResource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LaunchDetailsFragment : Fragment(R.layout.fragment_launch_details) {

    private var _binding : FragmentLaunchDetailsBinding? = null
    private val binding get() = _binding

    private val mLaunchesViewModel : LaunchDetailsViewModel by viewModels()

    private val arguments : LaunchDetailsFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val partnerRcAdapter = AgencyListRecyclerAdapter()

    private var countDownTimer: CountDownTimer? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLaunchDetailsBinding.inflate(inflater , container , false)
        val args = arguments.launch
        val fragId = arguments.fragmentId
        binding?.setUpUi(args , fragId)
        getRocketConfiguration(args)
        getAgencyDetails(args)
        return binding?.root
    }

    private fun getAgencyDetails(args: LaunchList) {
        //get agency details
        args.serviceProvider?.id?.let { mLaunchesViewModel.getAgencyResponse(it) }
    }

    private fun getRocketConfiguration(args: LaunchList) {
        //get rocket instance
        args.rocket?.configuration?.let { mLaunchesViewModel.getRocketInstance(it.id) }
    }

    private fun FragmentLaunchDetailsBinding.setUpUi(args: LaunchList, fragId: Int) {

        //setup controller and navHostFragment
        navController = findNavController()
        appBarConfiguration = AppBarConfiguration(
            navController.graph
        )

        binding?.setUpToolbar(args.name , args.image)
        binding?.setUpTimerCard(args , fragId)

        launchToolbar.setNavigationOnClickListener {
            Log.d("LaunchDetails", "navigation click listener invoked ")
            navController.popBackStack()
        }

        Log.d("LaunchDetails", "setUpUi: argument: ${args.toString()} ")

        programParticipantsRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = partnerRcAdapter
        }
    }

    private fun FragmentLaunchDetailsBinding.setUpTimerCard(args: LaunchList, fragId: Int){
        val orbit = args.mission?.orbit?.name ?: "No orbit provided yet"
        launchOrbitTxt.text = orbit

        launchLocationTxt.text = "${args.pad.location.name} | ${args.pad.name}"

        livestreamImageIndicator.isVisible = args.stream

        livestreamTextIndicator.isVisible = args.stream

        if(args.stream) livestreamImageIndicator.startBlinkingAnimation()

        if(args.status?.id == 4 && fragId == 2 ){
            launchStatusText.text = args.status.name
            failReasonTxt.isVisible = true
            launchStatusText.setTextColor(ContextCompat.getColor(requireContext() , R.color.error_red))
            failReasonTxt.text = args.fail
        } else if(args.status?.id == 3 && fragId == 2 ) {
            failReasonTxt.isVisible = false
            launchStatusText.setTextColor(ContextCompat.getColor(requireContext() , R.color.active_green))
            launchStatusText.text = args.status.name
        }else {
            failReasonTxt.isVisible = false
            launchStatusText.text = args.status?.name
        }


        launchDetailsDateTxt.text = args.startWindow.convertDate(requireContext())

        this.changeStatusColor(args.status?.id)


        val date = args.startWindow.getDateToCurrentTimezone(requireContext())

        this.startTimer(date , requireContext())




        this.setUpProbabilityBar(args.probability)

        this.setUpMissionCard(args.mission)

        this.setUpLauncherCard()

        this.setUpAgencyCard()

        this.setUpProgramCard(args)



        watchStreamButton.setOnClickListener {
            //check if stream is available
            if(args.stream){
                //launch intent to youtube app or open chrome to youtube with video id
                Log.d("LaunchDetails", "setUpTimerCard: opening streaming channel")
            }else{
                binding?.root?.let { view -> Snackbar.make(view, "No stream available now" , Snackbar.LENGTH_SHORT).show() }
            }
        }

        shareLaunchBtn.setOnClickListener {
            if(args.stream){
                Log.d("LaunchDetails", "setUpTimerCard: sharing video url")
            }else{
                binding?.root?.let { view -> Snackbar.make(view, "No video or article to share now" , Snackbar.LENGTH_SHORT).show() }
            }
        }


    }

    private fun FragmentLaunchDetailsBinding.setUpProgramCard(args: LaunchList){
        //setup card
        val programs = args.program

        when {
            programs.isEmpty() -> {
                programCard.isGone = true
            }
            programs.size > 1 -> {
                programTitleTxt.text = "${programs[0].name} | ${programs[1].name}"

                val programDescription = programs[0].description + programs[1].description
                programDescTxt.text = programDescription

                val agenciesList = programs[0].agencies

                partnerRcAdapter.submitList(agenciesList)

            }
            else -> {
                programTitleTxt.text = programs[0].name

                val programDescription = programs[0].description
                programDescTxt.text = programDescription
            }
        }




    }


    private fun FragmentLaunchDetailsBinding.setUpLauncherCard(){
        lifecycleScope.launchWhenStarted {
            mLaunchesViewModel.rocketInstanceResponse.collectLatest { resource ->
                when(resource){
                    is NetworkResource.Loading -> {
                        launchDetailsProgressBar.isVisible = true
                    }
                    is NetworkResource.Success -> {
                        launchDetailsProgressBar.isInvisible = true
                        val rocket = resource.value

                        //load ui components
                        launcherNameTxt.text = rocket.fullName
                        vehicleHeightTxt.text = "Height : ${rocket.length}m "
                        vehicleDiameterTxt.text = "Diameter : ${rocket.diameter}m"
                        maxStagesTxt.text = "Max stages : ${rocket.maxStage}"
                        liftOffThrustTxt.text = "Liftoff Thrust : ${rocket.toThrust}KN"
                        liftOffMassTxt.text = "Liftoff mass : ${rocket.launchMass}t"
                        mass2LeoTxt.text = "Mass to LEO : ${rocket.leoCapacity}kg"
                        mass2GtoTxt.text = "Mass to GTO : ${rocket.gtoCapacity}kg"
                        launchSuccessTxt.text = "Successful: ${rocket.successfulLaunches}"
                        launchFailuresTxt.text = "Failed: ${rocket.failedLaunches}"
                        consecSuccessTxt.text = "Consecutive Success: ${rocket.consecLaunches}"
                        maidenFlightDateTxt.text = "Maiden launch : ${rocket.maidenLaunch}"

                        //load image
                        binding?.let { Glide.with(it.root)
                            .load(rocket.image)
                            .placeholder(R.color.grey)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(it.launcherImageView)

                        }

                        learnMoreRocketBtn.setOnClickListener {
                            //open wikipedia page of rocket in browser
                            launchBrowser(rocket.wikiUrl)
                        }


                    }

                    is NetworkResource.Failure -> {
                        val error = resource.errorBody
                        error?.let {
                            Toast.makeText(requireContext(), "could not fetch rocket details. Exited with error: ${it.toString()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        }
    }


    private fun FragmentLaunchDetailsBinding.setUpAgencyCard(){
        lifecycleScope.launchWhenStarted {
            mLaunchesViewModel.agencyInstanceResponse.collectLatest { response ->
                when(response){
                    is NetworkResource.Loading -> {
                        launchDetailsProgressBar.isVisible = true
                    }
                    is NetworkResource.Success -> {
                        launchDetailsProgressBar.isVisible = false

                        val agency = response.value

                        launchAgencyNameTxt.text = agency.name
                        agencyServiceTypeTxt.text = agency.type
                        agencyDescriptionTxt.text = agency.description

                        loadAgencyImage(agency)
                    }

                    is NetworkResource.Failure -> {
                        val error = response.errorBody
                        error?.let {
                            Toast.makeText(requireContext(), "could not fetch agency details. Exited with error: ${it.toString()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    }

    private fun loadAgencyImage(agency: Agency) {
        binding?.let {
            Glide.with(it.root)
                .load(agency.logo)
                .centerCrop()
                .placeholder(R.drawable.sayari_logo2)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(it.agencyImage)
        }
    }

    private fun FragmentLaunchDetailsBinding.setUpProbabilityBar(probability: Int?){
        probability?.let {
            if(it <= 0){
                probabilityProgress.progress = 0
                probabilityTxt.text = "0%"
            }else {
                probabilityProgress.progress = it
                probabilityTxt.text = "$it%"
            }
        } ?: alternateProbability()
    }

    private fun alternateProbability(){
        binding?.apply {
            probabilityProgress.progress = 50
            probabilityTxt.text = "50%"
        }
    }

    private fun FragmentLaunchDetailsBinding.setUpMissionCard(mission: Mission?) {
        val missionName = mission?.name ?: "No name yet"
        val missionDesc = mission?.description ?: "No description yet"
        missionNameTxt.text = missionName
        missionTxt.text = missionDesc

    }

    private fun FragmentLaunchDetailsBinding.setUpToolbar(launchName: String, image: String){


        (activity as AppCompatActivity).setSupportActionBar(launchToolbar)
        (activity as AppCompatActivity).supportActionBar!!.title = launchName
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(true)

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.collapsingToolbarLayoutTitleColorOpen)
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsingToolbarLayoutTitleCollapsed)


        //load launch image
        binding?.root?.let {
            Glide.with(it)
                .load(image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(launchImageView)
        }


    }




    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun FragmentLaunchDetailsBinding.startTimer(
        launchWindowDate: Date?,
        context: Context
    ){
        val currentDate = Calendar.getInstance().timeInMillis

        launchWindowDate?.let{
            val timeDiff = it.time.minus(currentDate)

            Log.d("LaunchDetails", "startTimer: timediff : $timeDiff")

            //start timer
            countDownTimer = object : CountDownTimer(timeDiff, 1000){
                override fun onTick(millscUntilFinish: Long) {
                    countdwnTimerTxt.text = context.getString(R.string.updated_timer,
                        TimeUnit.MILLISECONDS.toDays(millscUntilFinish) , TimeUnit.MILLISECONDS.toHours(millscUntilFinish) %24 ,
                        TimeUnit.MILLISECONDS.toMinutes(millscUntilFinish)%60 , TimeUnit.MILLISECONDS.toSeconds(millscUntilFinish)%60)
                }

                override fun onFinish() {
                    Log.d("LaunchDetailsFrag", "onFinish: timer has finished its work")
                }

            }

            (countDownTimer as CountDownTimer).start()
        } ?: binding?.root?.let {
            Snackbar.make(
                it,
                "Launch window not availble yet",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

   private fun String.convertDate(context: Context): String {
        return try {
            val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("GMT")
            val passedDate: Date = inputFormat.parse(this)

            //Here you put how you want your date to be, this looks like this Tue,Nov 2, 2021, 12:23 pm
            val outputFormatDay = SimpleDateFormat("EEE, MMM d, yyyy, hh:mm aaa", currentLocale)
            outputFormatDay.timeZone = TimeZone.getDefault()
            val newDateString = outputFormatDay.format(passedDate)
            newDateString
        } catch (_: Exception) {
            "00:00:00"
        }
    }

    private fun String.getDateToCurrentTimezone(context : Context) : Date? {
        return try {
            val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("GMT")
            val passedDate: Date? = inputFormat.parse(this)
            passedDate
        }catch (_ : Exception){
            val date : Date? = null
            date
        }

    }



    private fun FragmentLaunchDetailsBinding.changeStatusColor(id: Int?) {
        when(id){
            1 -> launchStatusText.setTextColor(Color.parseColor("#0af225"))

            2 -> launchStatusText.setTextColor(Color.parseColor("#5b0675"))

            8 -> launchStatusText.setTextColor(Color.parseColor("#c43e00"))
        }
    }


    private fun ImageView.startBlinkingAnimation(){
        val anim = AnimationUtils.loadAnimation(requireContext() , R.anim.blink_anim)
        this.startAnimation(anim)
    }

    private fun launchBrowser(url: String?) {
        url?.let {
            Toast.makeText(requireContext() , "Opening browser..." , Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }?: Toast.makeText(requireContext() , "No news site available" , Toast.LENGTH_LONG).show()
    }



}


