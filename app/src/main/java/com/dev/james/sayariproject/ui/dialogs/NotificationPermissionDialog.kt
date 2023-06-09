package com.dev.james.sayariproject.ui.dialogs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.DialogPermissionWarningBinding
import dagger.hilt.android.AndroidEntryPoint


class NotificationPermissionDialog(
  val onRelaunchPermissions : () -> Unit ,
  val onCancelPermission : () -> Unit
) : DialogFragment(R.layout.dialog_permission_warning) {

    private lateinit var binding : DialogPermissionWarningBinding

    companion object {
        const val TAG = "NotificationPermission"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogPermissionWarningBinding.bind(view)

        binding.apply {
            relaunchPermissionBtn.setOnClickListener {
                dismiss()
                onRelaunchPermissions()
            }
            dismissButton.setOnClickListener {
                dismiss()
                onCancelPermission()
            }
        }
    }
}