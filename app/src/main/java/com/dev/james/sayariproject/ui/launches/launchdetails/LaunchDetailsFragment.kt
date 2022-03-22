package com.dev.james.sayariproject.ui.launches.launchdetails

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.ConfigurationCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentLaunchDetailsBinding
import com.dev.james.sayariproject.models.launch.LaunchList
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LaunchDetailsFragment : Fragment(R.layout.fragment_launch_details) {

    private var _binding : FragmentLaunchDetailsBinding? = null
    private val binding get() = _binding

    private val arguments : LaunchDetailsFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var countDownTimer: CountDownTimer? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLaunchDetailsBinding.inflate(inflater , container , false)
        val args = arguments.launch
        binding?.setUpUi(args)
        return binding?.root
    }

    private fun FragmentLaunchDetailsBinding.setUpUi(args: LaunchList) {
        //setup controller and navHostFragment
        navController = findNavController()
        appBarConfiguration = AppBarConfiguration(
            navController.graph
        )

        binding?.setUpToolbar(args.slug , args.image)
        binding?.setUpTimerCard(args)

        launchToolbar.setNavigationOnClickListener {
            Log.d("LaunchDetails", "navigation click listener invoked ")
            navController.popBackStack()
        }

        Log.d("LaunchDetails", "setUpUi: argument: ${args.toString()} ")
    }

    private fun FragmentLaunchDetailsBinding.setUpTimerCard(args : LaunchList){
        val orbit = args.mission?.orbit?.name ?: "No orbit provided yet"
        launchOrbitTxt.text = orbit

        launchLocationTxt.text = args.pad.location.name

        livestreamImageIndicator.isVisible = args.stream

        livestreamTextIndicator.isVisible = args.stream

        launchStatusText.text = args.status?.name

        launchDetailsDateTxt.text = args.startWindow.convertDate(requireContext())

        this.changeStatusColor(args.status?.id)

       // val date = args.startWindow.convertDate(requireContext())

       // this.startTimer(date , requireContext())

        this.setUpProbabilityBar(args.probability)


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
            probabilityProgress.progress = 10
            probabilityTxt.text = "10%"
        }
    }

    private fun FragmentLaunchDetailsBinding.setUpToolbar(launchName: String, image: String){


        (activity as AppCompatActivity).setSupportActionBar(launchToolbar)
        (activity as AppCompatActivity).supportActionBar!!.title = launchName
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(true)


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
        val timeDiff = launchWindowDate?.time?.minus(currentDate)

        Log.d("LaunchDetails", "startTimer: timediff : ${timeDiff.toString()}")

        //start timer
     /**   countDownTimer = object : CountDownTimer(timeDiff!!, 1000){
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

**/


    }

    fun String.convertDate(context: Context): String {
        return try {
            val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'z'", Locale.getDefault())
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

    private fun FragmentLaunchDetailsBinding.changeStatusColor(id: Int?) {
        when(id){
            1 -> launchStatusText.setTextColor(Color.parseColor("#0af225"))

            2 -> launchStatusText.setTextColor(Color.parseColor("#5b0675"))

            8 -> launchStatusText.setTextColor(Color.parseColor("#c43e00"))
        }
    }



}


