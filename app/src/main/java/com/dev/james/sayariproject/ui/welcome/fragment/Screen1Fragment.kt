package com.dev.james.sayariproject.ui.welcome.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.dev.james.sayariproject.databinding.WelcomeScreen1Binding
import com.dev.james.sayariproject.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Screen1Fragment : BaseFragment<WelcomeScreen1Binding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = WelcomeScreen1Binding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}