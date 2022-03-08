package com.dev.james.sayariproject.ui.iss

import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentSpacecraftBinding
import com.dev.james.sayariproject.models.iss.FlightVehicle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpaceCraftFragment : Fragment() {
    private var _binding : FragmentSpacecraftBinding? = null
    private val binding get() = _binding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val arguments : SpaceCraftFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSpacecraftBinding.inflate(inflater
            , container ,
            false)
        val flightVehicle = arguments.flightVehicle
        if (flightVehicle != null) {
            setUpUi(flightVehicle)
        }
        return binding?.root
    }

    private fun setUpUi(vehicle: FlightVehicle) {
        binding?.apply {

            //setup controller and navHostFragment
            navController = findNavController()
            appBarConfiguration = AppBarConfiguration(
                navController.graph
            )
            spaceCraftToolbar.setNavigationOnClickListener {
                val action = SpaceCraftFragmentDirections.actionSpaceCraftFragmentToIssFragment2()
                navController.navigate(action)
            }


            loadImage(vehicle.spacecraft.vehicleConfig.imageUrl)
            vehicleNameTxt.text = vehicle.spacecraft.name

            vehicle.spacecraft.serialNumber?.let {
                vehicleSerialTxt.text = "SN: $it"

            }

            if (vehicle.spacecraft.vehicleConfig.humanRated) {
                humanRatedTxt.isVisible = true
                humanRatedImg.isVisible = true
                humanRatedImg.setImageResource(R.drawable.ic_baseline_check_circle_24)
            } else {
                humanRatedTxt.isVisible = true
                humanRatedImg.isVisible = true
                humanRatedImg.setImageResource(R.drawable.ic_no)
            }

            vehicle.spacecraft.vehicleConfig.height?.let { craftHeightTxt.startCountAnimation(it.toInt()) }
            vehicle.spacecraft.vehicleConfig.diameter?.let { craftWidthTxt.startCountAnimation(it.toInt()) }
            vehicle.spacecraft.vehicleConfig.crewCapacity?.let {
                craftCrewCapTxt.startCountAnimation(
                    it
                )
            }
            vehicle.spacecraft.vehicleConfig.payloadCapacity?.let{
                craftPayloadCapTxt.text = "${it.toString()}kg"
            }

            maidenFlightTxt.text = vehicle.spacecraft.vehicleConfig.maidenFlight
            flightLifeTxt.text = vehicle.spacecraft.vehicleConfig.flightLife
            launcherTxt.text = vehicle.launch.rocket?.configuration?.name

            aboutCrftTxt.text = vehicle.spacecraft.vehicleConfig.details

            loadAgencyLogo(vehicle.spacecraft.vehicleConfig.agency.imageUrl)
            agencyNameTxt.text = vehicle.spacecraft.vehicleConfig.agency.name
            agencyCountryTxt.text = vehicle.spacecraft.vehicleConfig.agency.countryCode
            agencyTypeTxt.text = vehicle.spacecraft.vehicleConfig.agency.type


        }
    }

    private fun loadAgencyLogo(imageUrl: String) {
        binding?.let {
            Glide.with(it.root)
                .load(imageUrl)
                .centerCrop()
                .error(R.drawable.ic_broken_image)
                .into(it.agencyLogoImg)
        }
    }

    private fun loadImage(imageUrl: String) {
        binding?.let {
            Glide.with(it.root)
                .load(imageUrl)
                .centerCrop()
                .error(R.drawable.ic_broken_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        it.craftImgProgress.isInvisible = true
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        it.craftImgProgress.isInvisible = true
                        return false
                    }
                })
                .into(it.craftImage)
        }
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
}