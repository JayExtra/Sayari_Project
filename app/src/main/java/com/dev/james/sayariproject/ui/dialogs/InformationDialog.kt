package com.dev.james.sayariproject.ui.dialogs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.DialogAppreciationBinding
import com.dev.james.sayariproject.databinding.DialogPermissionWarningBinding
import dagger.hilt.android.AndroidEntryPoint


class InformationDialog(
  val onCloseDialog : () -> Unit
) : DialogFragment(R.layout.dialog_appreciation) {

    private lateinit var binding : DialogAppreciationBinding

    companion object {

        const val TAG = "InformationDialog"

        fun newInstance(title : Int , message : Int , onCloseDialog : () -> Unit) : InformationDialog {
            val args = Bundle()
            args.putInt("title" , title)
            args.putInt("body" , message)
            val dialog = InformationDialog(
                onCloseDialog
            )
            dialog.arguments = args
            return dialog
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogAppreciationBinding.bind(view)

        val title = arguments?.getInt("title") ?: R.string.thank_you_ttl
        val message = arguments?.getInt("body") ?: R.string.thank_you_mssg


        binding.apply {
            dialogTitle.text = requireContext().getString(title)
            dialogBody.text = requireContext().getString(message)
            dismissDialogBtn.setOnClickListener {
                dismiss()
               onCloseDialog()
            }
        }
    }
}