package com.dev.james.sayariproject.ui.launches

import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.james.sayariproject.databinding.FragmentUpcomingLaunchesBinding
import com.dev.james.sayariproject.models.launch.LaunchList
import com.dev.james.sayariproject.ui.launches.adapters.LaunchesRecyclerAdapter
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

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class UpcomingLaunchesFragment : Fragment() {
    private var _binding: FragmentUpcomingLaunchesBinding? = null
    private val binding get() = _binding!!
    private val mLaunchesViewModel : LaunchesViewModel by viewModels({requireParentFragment()})
    private val adapter = LaunchesRecyclerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        executeInitialLoad()

        setHasOptionsMenu(true)
        _binding = FragmentUpcomingLaunchesBinding.inflate(inflater , container , false)

        binding.bindState(
            uiState = mLaunchesViewModel.uiState,
            pagingData = mLaunchesViewModel.pagingDataFlow,
            uiAction = mLaunchesViewModel.accept
        )

        binding.scrollUpBtn.setOnClickListener { btnMoveUp ->
            lifecycleScope.launch {
                binding.upcomingPreviousRv.scrollToPosition(0)
                delay(100)
                btnMoveUp.toggle(false)
            }
        }

        binding.upcomingSwipeToRefresh.setOnRefreshListener {

            refreshList()

            binding.upcomingSwipeToRefresh.isRefreshing = false

        }

        return binding.root
    }

    private fun executeInitialLoad() {
        mLaunchesViewModel.getLaunches(null , 0)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun FragmentUpcomingLaunchesBinding.bindState(
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
        bindSearch(
            uiState = uiState,
            onQueryChanged = uiAction
        )
    }

    private fun bindSearch(
        uiState: StateFlow<UiState>,
        onQueryChanged: (UiAction.Search) -> Unit
    ){
        mLaunchesViewModel.queryPassed.observe(viewLifecycleOwner , { event ->
            event.getContentIfNotHandled()?.let { query ->
                Log.d("UpcomingFrag", "bindSearch: query received : $query")
                updateLaunchListFromInput(onQueryChanged , query)
            }
        })

    }

    private fun FragmentUpcomingLaunchesBinding.bindList(
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
            mLaunchesViewModel.getLaunches(null , 0)
            mLaunchesViewModel.pagingDataFlow.collectLatest {
                adapter.submitData(it)
            }
            binding.upcomingPreviousRv.scrollToPosition(0)
        }
    }


}