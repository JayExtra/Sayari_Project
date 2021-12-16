package com.dev.james.sayariproject.ui.welcome.fragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentWelcomeScreenBinding
import com.dev.james.sayariproject.ui.base.BaseFragment
import com.dev.james.sayariproject.ui.welcome.adapter.ViewPagerAdapter
import com.dev.james.sayariproject.ui.welcome.viewmodel.WelcomeScreenViewModel
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class WelcomeScreenFragment : BaseFragment<FragmentWelcomeScreenBinding>(){
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentWelcomeScreenBinding::inflate

    private var pagePosition : Int = 0

    private val viewModel : WelcomeScreenViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
        setUpButtonListeners()
        initMaterialTransitions()
    }


    private fun setUpUi() {
        //1.create the list of fragments required to passed into the viewpager
        val fragmentList = arrayListOf<Fragment>(
            Screen1Fragment(),
            Screen2Fragment()
        )

        //2.create the viewpager adapter for the view pager passing in
        //the fragment list
        val vpAdapter = ViewPagerAdapter(
            fragmentList ,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        binding.apply {
            //3.assign the adapter to the viewpager
            welcomeScreenViewPager.adapter = vpAdapter
        }

        //setup buttons
        setUpButtonsVisibility()


    }

    private fun setUpButtonsVisibility(){
        binding.apply {
            welcomeScreenViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    pagePosition = position

                    if(position == 1){
                        nexBtn.isVisible = true
                        nexBtn.text = "finish"
                        prevBtn.isVisible = true

                        progressLeft.setImageResource(R.drawable.page_indicator_selected)
                        progressRight.setImageResource(R.drawable.page_indicator_unselected)

                    }else{
                        prevBtn.isInvisible = true
                        nexBtn.text = "next"
                        nexBtn.isVisible = true

                        progressLeft.setImageResource(R.drawable.page_indicator_unselected)
                        progressRight.setImageResource(R.drawable.page_indicator_selected)
                    }


                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                }

            })
        }
    }

    private fun setUpButtonListeners(){
        binding.apply {

                prevBtn.setOnClickListener {
                    welcomeScreenViewPager.currentItem = pagePosition-1
                }
                nexBtn.setOnClickListener {
                    if(pagePosition == 1){
                        viewModel.setOnBoardingValue(true)
                        Log.d("WelcomeFrag", "setUpButtonListeners: on boarding result set to true ")
                        findNavController().navigate(R.id.action_welcomeScreenFragment_to_homeFragment)
                    }else{
                        welcomeScreenViewPager.currentItem = pagePosition+1
                    }
                }
        }
    }

    private fun initMaterialTransitions() {
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }
}