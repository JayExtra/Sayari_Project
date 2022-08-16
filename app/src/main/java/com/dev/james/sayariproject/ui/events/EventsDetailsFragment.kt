package com.dev.james.sayariproject.ui.events

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.ConfigurationCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentEventDetailsBinding
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.ui.iss.adapters.PartnersRecyclerView
import com.dev.james.sayariproject.ui.launches.launchdetails.AgencyListRecyclerAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EventsDetailsFragment : Fragment() {

    private var _binding : FragmentEventDetailsBinding? = null
    private val binding get() = _binding!!
    private val eventsArguments : EventsDetailsFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val partnerAdapter = AgencyListRecyclerAdapter()
    private val eventDetailsViewModel : EventDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailsBinding.inflate(
            inflater ,
            container,
            false

        )

        val eventItem = eventsArguments.event
        val eventId = eventsArguments.eventId

        if(eventId != 0 && eventItem == null){
            //we know that we are coming from intent
            // load details from server and show on screen
        }else {
            //we are not coming from intent
            eventItem?.let { binding.setUpUi(it) }

        }

        collectEventsFromChannel()
        return binding.root
    }

    private fun collectEventsFromChannel() {
        viewLifecycleOwner.lifecycleScope.launch {
            eventDetailsViewModel.eventDetailsScreenChannel
                .flowWithLifecycle(lifecycle , Lifecycle.State.STARTED).collect { event ->
                    when(event){
                        is EventDetailsViewModel.EventDetailsScreenEvents.SuccessFullAlertScheduling -> {
                            Snackbar.make(
                                binding.root ,
                                "Alert set for ${event.eventName} event",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        is EventDetailsViewModel.EventDetailsScreenEvents.FailedAlertScheduling -> {
                            Snackbar.make(
                                binding.root ,
                                event.errorMess,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }


            }
        }
    }

    private fun FragmentEventDetailsBinding.setUpUi(event : Events){

        //setup controller and navHostFragment
        navController = findNavController()
        appBarConfiguration = AppBarConfiguration(
            navController.graph
        )
        (activity as AppCompatActivity).setSupportActionBar(eventToolbar)
        (activity as AppCompatActivity).supportActionBar!!.title = event.name
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(true)

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.collapsingToolbarLayoutTitleColorOpen)
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsingToolbarLayoutTitleCollapsed)

        eventToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        setUpEventsDetailsCard(event)
        setUpEventsDescCard(event)
        setUpPartnersRv(event)
        setUpImage(event)
        setUpButtons(event)

    }

    private fun FragmentEventDetailsBinding.setUpButtons(event : Events){

        watchSteamBtn.setOnClickListener{
            val message = getString(R.string.vid_err_message)
            event.videoUrl?.let {
                goToWebCast(it)
            } ?: showSnackBarMessage(message)

        }

        shareEventBtn.setOnClickListener {
            val message = getString(R.string.share_err_mess)
        event.newsUrl?.let {
            shareNewsOrVideoUrl(it)
        } ?: showSnackBarMessage(message)
        }

        setAlertFab.setOnClickListener {

            eventDetailsViewModel.scheduleEventAlarm(event)

        }




    }

    private fun showSnackBarMessage(message : String){
        Snackbar.make(
            binding.root ,
            message ,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun FragmentEventDetailsBinding.setUpEventsDetailsCard(event : Events){

        eventTitleTxt.text = event.slug
        eventLocationTxt.text = event.location
        eventTypeTxt.text = event.type.name
        eventDateTxt.text = event.date.convertDate(context = requireContext())

        if(event.program.isNotEmpty()){
            swipeTxt.isVisible = event.program[0].agencies.size > 1
        }


        livestreamImageIndicator.isVisible = event.videoUrl != null
        livestreamImageIndicator.startBlinkingAnimation()
        livestreamTextIndicator.isVisible = event.videoUrl != null


    }

    private fun FragmentEventDetailsBinding.setUpEventsDescCard(event : Events){
        eventDescriptionTxt.text = event.description
        eventProgram.isVisible = event.program.isNotEmpty()
        eventsProgramRv.isVisible = event.program.isNotEmpty()
        eventProgramName.isVisible = event.program.isNotEmpty()
        eventProgramName.text = if(event.program.isNotEmpty()) event.program[0].name else "No program name available"
    }

    private fun FragmentEventDetailsBinding.setUpPartnersRv(event: Events){
        eventsProgramRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = partnerAdapter
        }
        if(event.program.isNotEmpty()){
            partnerAdapter.submitList(event.program[0].agencies)
        }
    }

    private fun FragmentEventDetailsBinding.setUpImage(events : Events){
        Glide.with(binding.root)
            .load(events.imageUrl)
            .placeholder(R.drawable.ic_image_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(eventImageView)
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

    private fun ImageView.startBlinkingAnimation(){
        val anim = AnimationUtils.loadAnimation(requireContext() , R.anim.blink_anim)
        this.startAnimation(anim)
    }

    private fun goToWebCast(videoUrl: String?) {
        Log.d("EventsFrag", "shareNewsOrVideoUrl: video triggered ")
        videoUrl?.let {
            launchYoutubeIntent(videoUrl)
        }?: Log.d("EventsFragment", "goToWebCast: no webcast available")

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

    private fun shareNewsOrVideoUrl(shareUrl: String?) {
        Log.d("EventsFrag", "shareNewsOrVideoUrl: share url triggered ")
        shareUrl?.let {
            val shareIntent = Intent().apply{
                this.action = Intent.ACTION_SEND
                this.putExtra(Intent.EXTRA_TEXT , it)
                this.type = "text/plain"
            }
            startActivity(shareIntent)
        }
    }


}