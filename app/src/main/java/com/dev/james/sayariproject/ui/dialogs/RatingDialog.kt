package com.dev.james.sayariproject.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.RatingDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RatingDialog @Inject constructor() : DialogFragment(R.layout.rating_dialog) {

    private lateinit var binding : RatingDialogBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false
        binding = RatingDialogBinding.bind(view)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogTheme

        binding.apply {
            //close dialog
            maybeLaterButton.setOnClickListener {
                dialog?.dismiss()
            }
            //submit rating
            rateAppButton.setOnClickListener {
                Log.d("RatingDialog", "submit rating clicked: true")
            }
        }
    }
}