package com.dev.james.sayariproject.ui.dialogs.rating

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.RatingDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RatingDialog @Inject constructor() : DialogFragment(R.layout.rating_dialog) {

    private lateinit var binding : RatingDialogBinding

    private val ratingViewModel : RatingsDialogViewModel by viewModels()

    private var finalRating : Float? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false
        binding = RatingDialogBinding.bind(view)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogTheme

        binding.listenToObservables()

        binding.apply {
            //close dialog
            maybeLaterButton.setOnClickListener {
                dialog?.dismiss()
            }
            //submit rating
            rateAppButton.setOnClickListener {
                finalRating?.let {
                    Log.d("RatingDialog", "final rating: ${it.toString()}")
                }
            }

            ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                ratingViewModel.updateRatingEmoji(rating)
            }
        }
    }

    private fun RatingDialogBinding.listenToObservables(){
        ratingViewModel.ratingCount.observe(viewLifecycleOwner , { value ->
            finalRating = value

            when (value) {

                in 0.0..2.5 -> {
                    emojiImageView.setImageResource(R.drawable.ic_sad_emoji)
                }
                in 2.6..3.5 -> {
                    emojiImageView.setImageResource(R.drawable.ic_neutral_emoji)
                }
                in 3.6..5.0 -> {
                    emojiImageView.setImageResource(R.drawable.ic_happy)
                }
            }
        })
    }
}