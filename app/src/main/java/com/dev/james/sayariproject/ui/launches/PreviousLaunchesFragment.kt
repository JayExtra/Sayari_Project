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
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentPreviousLaunchesBinding
import com.dev.james.sayariproject.general_adapters.LoadingStateAdapter
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.ui.launches.adapters.PreviousLaunchesRecyclerAdapter
import com.dev.james.sayariproject.ui.launches.viewmodel.LaunchesViewModel
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
    private val launchesViewModel : LaunchesViewModel by  viewModels({requireParentFragment()})
    private var hasSearched : Boolean? = null
    private val adapter = PreviousLaunchesRecyclerAdapter { launch ->
        navigateToLaunchDetailsScreen(launch)
    }

    companion object {
        const val FRAGMENT_ID = 2
    }

    private fun navigateToLaunchDetailsScreen(launch: LaunchList) {
       val action = LaunchesFragmentDirections.actionLaunchesFragmentToLaunchDetailsFragment(launch , FRAGMENT_ID)
        findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentPreviousLaunchesBinding.inflate(inflater , container , false)

        makeInitialLaunch()
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
        binding.previousSwipeToRefresh.setOnRefreshListener {

            refreshList()

            binding.previousSwipeToRefresh.isRefreshing = false

        }
        return binding.root
    }

    private fun makeInitialLaunch() {
        launchesViewModel.getLaunches(null , 1)
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

        upcomingPreviousRv.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter{adapter.retry()}
        )
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
        bindSearch(
            uiState = uiState,
            onQueryChanged = uiAction
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

                if(isEmpty && hasSearched == true) {
                    Log.d("UpcomigFrag", "bindList: isEmpty : $isEmpty")
                    searchErrorMessage.isVisible = true
                    hasSearched = false
                }else{
                    searchErrorMessage.isVisible = false
                }


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
                    Log.d("PreviousFrag", "bindList: whoops! : ${it.error} ")
                }

            }

        }
    }

    private fun bindSearch(
        uiState: StateFlow<UiState>,
        onQueryChanged: (UiAction.Search) -> Unit
    ){
        Log.d("PreviousFrag", "bindSearch: called")
        launchesViewModel.queryPreviousPassed.observe(viewLifecycleOwner , { event ->
            event.getContentIfNotHandled()?.let { query ->
                Log.d("PreviousFrag", "bindSearch: query received : $query")
                updateLaunchListFromInput(onQueryChanged , query)
                hasSearched = true
            }
        })

    }

    fun View.toggle(show : Boolean){
        val transition : Transition = Slide(Gravity.BOTTOM)
        transition.duration = 200
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup? , transition)
        this.isVisible = show
    }

    private fun updateLaunchListFromInput(
        onQueryChanged : (UiAction.Search) -> Unit,
        query : String
    ){
        if(query.isNotEmpty()){
            binding.upcomingPreviousRv.scrollToPosition(0)
            onQueryChanged(UiAction.Search(query = query))
        }
    }

    fun refreshList(){
        lifecycleScope.launch {
            adapter.submitData(PagingData.empty())
            launchesViewModel.getLaunches(null , 1)
            launchesViewModel.pagingDataFlow.collectLatest {
                adapter.submitData(it)
            }
            binding.upcomingPreviousRv.scrollToPosition(0)
            hasSearched = false
        }
    }

}