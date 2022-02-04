package com.dev.james.sayariproject.ui.events

import android.animation.ValueAnimator
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentEventsBinding
import com.dev.james.sayariproject.general_adapters.LoadingStateAdapter
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.ui.events.adapter.EventsRecyclerAdapter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventsFragment : Fragment() {
    private var _binding : FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private val eventsViewModel : EventsViewModel by viewModels()

    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val eventsAdapter = EventsRecyclerAdapter()

    private var hasSearched : Boolean? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventsBinding.inflate(layoutInflater, container , false)

        setUpUi()
        collectSearchState()
        collectChartDataStateFlow()
        binding.bindState(
            uiState = eventsViewModel.uiState,
            pagingData = eventsViewModel.pagingDataFlow,
            uiActions = eventsViewModel.accept
        )
        binding.bindToolbarValues()
        return binding.root
    }

    private fun collectChartDataStateFlow() {
        lifecycleScope.launchWhenStarted {
            eventsViewModel.chartDataState.collectLatest { event ->
                event.getContentIfNotHandled()?.let { chart_data ->
                    setUpChartWithData(chart_data)
                }
            }
        }
    }

    private fun setUpChartWithData(chartData: List<Float>) {
        val pieEntries = arrayListOf<PieEntry>()

        //setup entries
        chartData.forEach { data ->
            pieEntries.add(PieEntry(data))
        }

        //setup pie chart colors
        val pieDataSet = PieDataSet(pieEntries , "")
        pieDataSet.setColors(
            ContextCompat.getColor(requireContext() ,R.color.white),
            ContextCompat.getColor(requireContext() ,R.color.secondaryColor),
            ContextCompat.getColor(requireContext() ,R.color.purps),
            ContextCompat.getColor(requireContext() ,R.color.green),
            ContextCompat.getColor(requireContext() ,R.color.yellow),
            ContextCompat.getColor(requireContext() ,R.color.blue)
        )
        // setup Pie Data Set in PieData
        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(true)

        binding.apply{
            eventsDistChart.apply {
                animateXY(1000 , 1000)
                centerText = "overall events distribution"
                setCenterTextColor(ContextCompat.getColor(requireContext() , R.color.primaryColor))
                setCenterTextSize(15f)
                legend.isEnabled = false
                description.isEnabled = false
                holeRadius = 75f
                data = pieData
            }
        }


    }

    private fun FragmentEventsBinding.bindState(
        uiState : StateFlow<UiState>,
        pagingData : Flow<PagingData<Events>>,
        uiActions : (UiAction) -> Unit,
        ){


        bindList(
            uiState = uiState ,
            pagingData = pagingData
        )

        bindSearch(
            uiState = uiState ,
            onQueryChanged = uiActions
        )

    }

    private fun FragmentEventsBinding.bindToolbarValues(){
        //load count animation
        lifecycleScope.launchWhenStarted {
            eventsViewModel.eventCountStateFlow.collectLatest { event ->
                event.getContentIfNotHandled()?.let { count ->
                    val animator = ValueAnimator.ofInt(0 , count)
                    animator.duration = 1000
                    animator.addUpdateListener { animation ->
                        if (animation != null) {
                            eventsCount.text = animation.animatedValue.toString()
                        }
                    }
                    animator.start()
                }

            }
        }
    }

    private fun FragmentEventsBinding.bindList(
        uiState : StateFlow<UiState>,
        pagingData : Flow<PagingData<Events>>
    ){
        //submit data to events adapter
       lifecycleScope.launchWhenStarted {
           pagingData.collectLatest(eventsAdapter::submitData)
       }




        lifecycleScope.launchWhenStarted {
            eventsAdapter.loadStateFlow.collectLatest { loadState ->
                val isEmpty = loadState.refresh is LoadState.NotLoading && eventsAdapter.itemCount == 0

                if(isEmpty && hasSearched == true){
                    Log.d("EventsFrag", "bindList: search error has happened")
                    searchErrMessage.isVisible = true
                    hasSearched = false
                }else {
                    searchErrMessage.isInvisible = true
                }

                eventsProgressBar.isVisible = loadState.refresh is LoadState.Loading
                eventsRetryButton.isVisible = loadState.refresh is LoadState.Error && eventsAdapter.itemCount == 0

                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error

                eventsErrorMessage.isVisible = loadState.refresh is LoadState.Error && eventsAdapter.itemCount == 0

                errorState?.let {
                    Log.d("EventsFrag", "bindList: whoops! : ${it.error} ")
                }

            }
        }
    }

    private fun FragmentEventsBinding.bindSearch(
        uiState : StateFlow<UiState>,
        onQueryChanged : (UiAction.Search) -> Unit
    ){
        eventsSearchInput.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_GO){
                //PERFORM UPDATE
                updateRepoListFromInput(onQueryChanged)
                Log.d("EventsFrag", "bindSearch: editor action triggered ")

                true
            }else{
                false
            }
        }

        eventsSearchInput.setOnKeyListener { _, keyCode, keyEvent ->
            if(keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                updateRepoListFromInput(onQueryChanged)
                Log.d("EventsFrag", "bindSearch: on key listener triggered ")

                true
            }else{
                false
            }
        }

        eventsSearchInput.addTextChangedListener {
            if(it.toString().isEmpty()){
                updateRepoListFromInput(onQueryChanged)
                Log.d("EventsFrag", "bindSearch: text changed triggered ")
               //eventsSearchInput.isFocusable = false
            }
        }



        eventsSwipeToRefresh.setOnRefreshListener {
            refreshList(onQueryChanged)
            eventsSwipeToRefresh.isRefreshing = false
        }

        lifecycleScope.launch {
            uiState
                .map { it.query }
                .distinctUntilChanged()
                .collect(eventsSearchInput::setText)
        }


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

            //recyclerview
            eventsRecyclerView.adapter = eventsAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter{eventsAdapter.retry()}
            )

            eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext()
                , LinearLayoutManager.VERTICAL ,false )

            eventsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val scrolledPosition =
                        (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()

                    if (scrolledPosition != null){
                        if(scrolledPosition >= 1){
                            scrollUpFab.toggleOut(true)
                        } else {
                            scrollUpFab.toggleOut(false)
                        }
                    }
                }
            })


           scrollUpFab.setOnClickListener {
                lifecycleScope.launch {
                    binding.eventsRecyclerView.scrollToPosition(0)
                    delay(100)
                    binding.scrollUpFab.toggleOut(false)
                }
            }

        }
    }

    private fun refreshList(onQueryChanged: (UiAction.Search) -> Unit) {
        onQueryChanged(UiAction.Search(query = ""))
        hasSearched = false
        binding.eventsRecyclerView.scrollToPosition(0)
    }


    //toggles search view up or down
    fun View.toggle(show : Boolean){
        val transition : Transition = Slide(Gravity.TOP)
        transition.duration = 200
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup? , transition)
        this.isVisible = show
    }

    //toggles the scroll up or down button
    fun View.toggleOut(show : Boolean){
        val transition : Transition = Slide(Gravity.RIGHT)
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

    private fun FragmentEventsBinding.updateRepoListFromInput(onQueryChanged: (UiAction.Search) -> Unit){
        eventsSearchInput.text?.trim().let {
            if (it != null) {
                if(it.isNotEmpty()) {
                    hasSearched = true
                    eventsRecyclerView.scrollToPosition(0)
                    onQueryChanged(UiAction.Search(query = it.toString()))
                    hideSoftKeyBoard()
                }
            //     eventsSearchInputLayout.error = "search cannot be empty"
            }
        }
    }


}