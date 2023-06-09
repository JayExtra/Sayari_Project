package com.dev.james.sayariproject.ui.events

import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import com.dev.james.sayariproject.utilities.isDateFuture
import com.dev.james.sayariproject.utilities.toDateObject
import com.dev.james.sayariproject.utilities.toDateStringDateOnly
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.snackbar.Snackbar
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

    @RequiresApi(Build.VERSION_CODES.O)
    private val eventsAdapter = EventsRecyclerAdapter { shareUrl, videoUrl, snackBarMessage, event  ->
        when {
            shareUrl!=null -> {
                shareNewsOrVideoUrl(shareUrl)
            }
            videoUrl!=null -> {
                event?.let {
                    if(it.date.toDateStringDateOnly().toDateObject().isDateFuture()){
                        showSnackBar("No live stream available yet for this event.")
                    }else{
                        goToWebCast(videoUrl)
                    }
                }

            }
            snackBarMessage!=null -> {
                showSnackBar(snackBarMessage)
            }
            event!=null -> {
                Log.d("EventsFragment", "event selected => $event")
                val action = EventsFragmentDirections.actionEventsFragment2ToEventsDetailsFragment(event)
                findNavController().navigate(action)
            }
            else -> {
                Log.d("EventsFrag", "No action invoked from adapter")
            }
        }
    }
    private fun shareNewsOrVideoUrl(shareUrl: String?) {
        Log.d("EventsFrag", "shareNewsOrVideoUrl: share url triggered ")
        shareUrl?.let {
            val shareIntent = Intent().apply{
                this.action = Intent.ACTION_SEND
                this.putExtra(Intent.EXTRA_TEXT , it)
                this.type = "text/plain"
            }
            startActivity(shareIntent)
        }
    }

    private fun goToWebCast(videoUrl: String?) {
        Log.d("EventsFrag", "shareNewsOrVideoUrl: video triggered ")
        videoUrl?.let {
            val vidId = extractVideoId(videoUrl)
            launchYoutubeIntent(videoUrl)
            Log.d("EventsFragment", "goToWebCast: video array : $vidId")
        }?: Log.d("EventsFragment", "goToWebCast: no webcast available")

    }

    private fun launchYoutubeIntent(c: String?) {
        val appIntent = Intent(Intent.ACTION_VIEW).apply {
            this.data = Uri.parse(c)
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val webIntent = Intent( Intent.ACTION_VIEW).apply {
            this.data =  Uri.parse(c)
        }

        try {
            requireContext().startActivity(appIntent)
        }catch (e : ActivityNotFoundException){
            Log.d("EventFrag", "launchYoutubeIntent: ${e.localizedMessage} ")
            requireContext().startActivity(webIntent)
        }
    }

    private fun extractVideoId(videoUrl: String): String {
        val videoArray = videoUrl.split("=")
        return videoArray.toString()
    }

    private fun showSnackBar(snackBarMessage: String?) {
        snackBarMessage?.let {
            Snackbar.make(binding.root , it , Snackbar.LENGTH_SHORT).show()
        }
    }


    private var hasSearched : Boolean? = null


    @RequiresApi(Build.VERSION_CODES.O)
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
            eventsViewModel.chartDataState.collect { chart_data ->
                    setUpChartWithData(chart_data)
            }
        }
    }

    private fun setUpChartWithData(chartData: List<Float>) {
        val pieEntries = arrayListOf<PieEntry>()

        //setup entries

        if(chartData.size!=1 && chartData.isNotEmpty()){
            pieEntries.add(PieEntry(chartData[0], "Docking"))
            pieEntries.add(PieEntry(chartData[1] , "Undocking"))
            pieEntries.add(PieEntry(chartData[2] , "EVA"))
            pieEntries.add(PieEntry(chartData[3] , "Berthing"))
            pieEntries.add(PieEntry(chartData[4] , "Landing"))
            pieEntries.add(PieEntry(chartData[5] , "Other"))
        }

        //setup pie chart colors
        val pieDataSet = PieDataSet(pieEntries , "")
        pieDataSet.setColors(
            ContextCompat.getColor(requireContext() ,R.color.teal_700),
            ContextCompat.getColor(requireContext() ,R.color.secondaryColor),
            ContextCompat.getColor(requireContext() ,R.color.purps),
            ContextCompat.getColor(requireContext() ,R.color.green),
            ContextCompat.getColor(requireContext() ,R.color.yellow),
            ContextCompat.getColor(requireContext() ,R.color.blue)
        )
        pieDataSet.valueFormatter = PercentFormatter()
        // setup Pie Data Set in PieData
        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(true)
        pieData.setValueTextSize(12f)

        binding.apply{
            eventsDistChart.apply {
                animateXY(1000 , 1000)
                centerText = "overall events distribution in the next 30"
                setCenterTextColor(ContextCompat.getColor(requireContext() , R.color.secondaryColor))
                setCenterTextSize(12f)
                legend.isEnabled = false
                setEntryLabelColor(ContextCompat.getColor(requireContext() , R.color.primaryColor))
                setEntryLabelTextSize(12f)
                setDrawEntryLabels(true)
                description.isEnabled = false
                holeRadius = 55f
                setHoleColor(Color.TRANSPARENT)
                data = pieData
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
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
        eventsToolbar
        //load count animation
        lifecycleScope.launchWhenStarted {
            eventsViewModel.eventCountStateFlow.collectLatest { count ->
                    val animator = ValueAnimator.ofInt(0 , count.thisMonth)
                    animator.duration = 1000
                    animator.addUpdateListener { animation ->
                        if (animation != null) {
                            eventsCount.text = animation.animatedValue.toString()
                        }
                    }
                    val animator2 = ValueAnimator.ofInt(0 , count.favouriteAgencies)
                    animator2.duration = 1000
                    animator.addUpdateListener { animation ->
                        if (animation != null) {
                            favouritesCount.text = animation.animatedValue.toString()
                        }
                    }

                    animator.start()
                    animator2.start()

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
               // eventsRetryButton.isVisible = loadState.refresh is LoadState.Error && eventsAdapter.itemCount == 0

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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpUi() {
        //ui setup here
        binding.apply {
            //setup controller and navHostFragment
            (activity as AppCompatActivity).setSupportActionBar(eventsToolbar)
            (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(true)
            (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.application_name)


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
        eventsViewModel.getEventsForAppBar()
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

    private fun hideSoftKeyBoard(){
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