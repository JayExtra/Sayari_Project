package com.dev.james.sayariproject.ui.launches

import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.james.sayariproject.databinding.FragmentPreviousLaunchesBinding
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.ui.launches.adapters.PreviousLaunchesRecyclerAdapter
import com.dev.james.sayariproject.ui.launches.viewmodel.PreviousLaunchesViewModel
import com.dev.james.sayariproject.ui.launches.viewmodel.UiAction
import com.dev.james.sayariproject.ui.launches.viewmodel.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class PreviousLaunchesFragment : Fragment() {
    private var _binding: FragmentPreviousLaunchesBinding? = null
    private val binding get() = _binding!!
    private val launchesViewModel : PreviousLaunchesViewModel by viewModels()
    private val adapter = PreviousLaunchesRecyclerAdapter()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreviousLaunchesBinding.inflate(inflater , container , false)
        binding.bindState(
            uiState = launchesViewModel.uiState,
            pagingData = launchesViewModel.pagingDataFlow,
            uiAction = launchesViewModel.accept
        )

        binding.scrollUpBtn.setOnClickListener { btnMoveUp ->
            lifecycleScope.launch {
                binding.upcomingPreviousRv.scrollToPosition(0)
                delay(100)
                btnMoveUp.toggle(false)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun FragmentPreviousLaunchesBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<LaunchList>>,
        uiAction: (UiAction) -> Unit
    ){

        upcomingPreviousRv.adapter = adapter
        upcomingPreviousRv.layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.VERTICAL,false)

        upcomingPreviousRv.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val scrolledPosition =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()

                if (scrolledPosition != null){
                    if(scrolledPosition >= 1){
                        scrollUpBtn.toggle(true)
                    } else {
                        scrollUpBtn.toggle(false)
                    }
                }
            }
        })

        bindList(
            uiState = uiState,
            pagingData = pagingData
        )

    }

    private fun FragmentPreviousLaunchesBinding.bindList(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<LaunchList>>
    ){
        lifecycleScope.launchWhenStarted {
            pagingData.collectLatest(adapter::submitData)
        }

        lifecycleScope.launchWhenStarted {
            adapter.loadStateFlow.collect { loadState->
                val isEmpty = loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0


                launchedProgress.isVisible = loadState.refresh is LoadState.Loading
                retryBtn.isVisible = loadState.refresh is LoadState.Error && adapter.itemCount == 0

                retryBtn.setOnClickListener {
                    adapter.retry()
                }

                netErrMess.isVisible = loadState.refresh is LoadState.Error && adapter.itemCount == 0
                netErrImage.isVisible = loadState.refresh is LoadState.Error && adapter.itemCount == 0

                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error

                errorState?.let {
                    Log.d("HomeFragment", "bindList: whoops! : ${it.error} ")
                }

            }

        }
    }

    fun View.toggle(show : Boolean){
        val transition : Transition = Slide(Gravity.BOTTOM)
        transition.duration = 200
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup? , transition)
        this.isVisible = show
    }

    fun receiveQuery(query : String){
        Log.d("PreviousLaunches", "query RECEIVED: $query ")

    }

}