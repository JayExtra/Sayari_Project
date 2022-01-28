package com.dev.james.sayariproject.ui.events

import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentEventsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class EventsFragment : Fragment() {
    private var _binding : FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private val eventsViewModel : EventsViewModel by viewModels()

    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventsBinding.inflate(layoutInflater, container , false)

        setUpUi()
        collectSearchState()
        //observeSearchState()
        return binding.root
    }

    private fun collectSearchState(){
        lifecycleScope.launchWhenStarted {
            eventsViewModel.sState.collectLatest { event ->
                event.getContentIfNotHandled()?.let { state ->
                    if(state) {
                        binding.apply {
                            searchToggle.setImageResource(R.drawable.ic_round_cancel)
                            eventsSearchInputLayout.toggle(true)
                            searchToggle.setOnClickListener {
                                eventsViewModel.updateSearchState(false)
                                hideSoftKeyBoard()
                            }
                        }
                    }else{

                        binding.apply {
                            searchToggle.setImageResource(R.drawable.ic_search)
                            eventsSearchInputLayout.toggle(false)
                            searchToggle.setOnClickListener {
                                eventsViewModel.updateSearchState(true)
                            }
                        }

                    }
                }

            }
        }
    }


    private fun setUpUi() {
        //ui setup here
        binding.apply {
            //setup controller and navHostFragment
            navController = findNavController()
            appBarConfiguration = AppBarConfiguration(
                navController.graph
            )
            eventsToolbar.setNavigationOnClickListener {
                navController.popBackStack()
            }
        }
    }

    //toggles the scroll up or down button
    fun View.toggle(show : Boolean){
        val transition : Transition = Slide(Gravity.TOP)
        transition.duration = 200
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup? , transition)
        this.isVisible = show
    }

    fun hideSoftKeyBoard(){
        val imm = view?.let {
            ContextCompat.getSystemService(
                it.context ,
            InputMethodManager::class.java)
        }
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}