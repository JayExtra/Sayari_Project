package com.dev.james.sayariproject.ui.events

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.ConfigurationCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
import dagger.hilt.android.AndroidEntryPoint
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
        return binding.root
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

    }

    private fun FragmentEventDetailsBinding.setUpEventsDetailsCard(event : Events){

        eventTitleTxt.text = event.slug
        eventLocationTxt.text = event.location
        eventTypeTxt.text = event.type.name
        eventDateTxt.text = event.date.convertDate(context = requireContext())

        swipeTxt.isVisible = event.program[0].agencies.size > 1


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
        partnerAdapter.submitList(event.program[0].agencies)
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

}