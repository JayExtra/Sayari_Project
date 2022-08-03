package com.dev.james.sayariproject.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.dev.james.sayariproject.databinding.FragmentNewsBinding
import com.dev.james.sayariproject.general_adapters.LoadingStateAdapter
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.ui.home.adapters.ArticlesRecyclerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewsFragment : Fragment() {
    private var _binding : FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private val newsViewModel : HomeViewModel by activityViewModels()

    private var hasSearched : Boolean? = null

    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    val adapter = ArticlesRecyclerAdapter { url ->
        //launch browser here
        launchBrowser(url)
    }

    private fun launchBrowser(url: String?) {
        url?.let {
            Toast.makeText(requireContext() , "Opening browser..." , Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }?: Toast.makeText(requireContext() , "No news site available" , Toast.LENGTH_LONG).show()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewsBinding.inflate(inflater , container , false)

        newsViewModel.getAllNews(null)
        binding.bindState(
            uiState = newsViewModel.uiState,
            pagingData = newsViewModel.pagingDataFlow,
            uiActions = newsViewModel.accept
        )

        setUpUi()
        return binding.root
    }

    private fun setUpUi() {

        //setup controller and navHostFragment
        navController = findNavController()
        appBarConfiguration = AppBarConfiguration(
            navController.graph
        )
        binding.newsTopAppBar.title = getString(R.string.application_name)
        binding.newsTopAppBar.setNavigationOnClickListener {
            navController.popBackStack()
        }
        binding.upcomingSwipeToRefresh.setOnRefreshListener {
            refreshList()
            binding.upcomingSwipeToRefresh.isRefreshing = false
        }

        binding.moveUpButton.setOnClickListener {
            lifecycleScope.launch {
                binding.newsRecyclerView.scrollToPosition(0)
                delay(100)
                binding.moveUpButton.toggle(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }



    private fun FragmentNewsBinding.bindState(
        uiState : StateFlow<UiState>,
        pagingData: Flow<PagingData<Article>>,
        uiActions : (UiAction) -> Unit
    ){

        newsRecyclerView.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter{ adapter.retry()}
        )
        newsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL , false)

        newsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val scrolledPosition =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()

                if (scrolledPosition != null){
                    if(scrolledPosition >= 1){
                        moveUpButton.toggle(true)
                    } else {
                        moveUpButton.toggle(false)
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
            onQueryChanged = uiActions
        )
    }

    private fun FragmentNewsBinding.bindSearch(
        uiState : StateFlow<UiState>,
        onQueryChanged : (UiAction.Fetch) -> Unit
    ){

        newsSearchTextInput.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_GO){
                //PERFORM UPDATE
                    updateRepoListFromInput(onQueryChanged)
                true
            }else{
                false
            }
        }

        newsSearchTextInput.setOnKeyListener { _, keyCode, keyEvent ->
            if(keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
               updateRepoListFromInput(onQueryChanged)
               true
            }else{
                false
            }
        }

        newsSearchTextInput.addTextChangedListener {
            if(it.toString().isEmpty()){
                updateRepoListFromInput(onQueryChanged)
                newsSearchTextViewLayout.isFocusable = false

            }
        }

        lifecycleScope.launch {
            uiState
                .map { it.query }
                .distinctUntilChanged()
                .collect(newsSearchTextInput::setText)
        }

    }

    private fun FragmentNewsBinding.bindList(
        uiState : StateFlow<UiState>,
        pagingData: Flow<PagingData<Article>>
    ){
        lifecycleScope.launchWhenStarted {
            pagingData.collectLatest(adapter::submitData)
        }

        lifecycleScope.launchWhenStarted {
            adapter.loadStateFlow.collectLatest { loadState ->
                val isEmpty = loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0



                if(isEmpty && hasSearched == true) {
                    newsSearchErrorTxt.isVisible = true
                    hasSearched = false
                }else {
                    newsSearchErrorTxt.isVisible = false
                }



                newsProgressBar.isVisible = loadState.refresh is LoadState.Loading
                retryNewsButton.isVisible = loadState.refresh is LoadState.Error && adapter.itemCount == 0

                retryNewsButton.setOnClickListener {
                    adapter.retry()
                }

                netErrorMessage.isVisible = loadState.refresh is LoadState.Error && adapter.itemCount == 0
                netErrorImage.isVisible = loadState.refresh is LoadState.Error && adapter.itemCount == 0

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

    //toggles the scroll up or down button
    fun View.toggle(show : Boolean){
        val transition : Transition = Slide(Gravity.RIGHT)
        transition.duration = 200
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup? , transition)
        this.isVisible = show
    }

    private fun FragmentNewsBinding.updateRepoListFromInput(onQueryChanged: (UiAction.Fetch) -> Unit){
        newsSearchTextInput.text?.trim().let {
            if (it != null) {
                if(it.isNotEmpty()) {
                    hasSearched = true
                    newsRecyclerView.scrollToPosition(0)
                    onQueryChanged(UiAction.Fetch(query = it.toString()))

                }
                  //  newsSearchTextViewLayout.error = "search cannot be empty"

            }
        }
    }

    fun refreshList(){
        lifecycleScope.launch {
            adapter.submitData(PagingData.empty())
            newsViewModel.getAllNews(null)
            newsViewModel.pagingDataFlow.collectLatest {
                adapter.submitData(it)
            }
            binding.newsRecyclerView.scrollToPosition(0)
            hasSearched = false
        }
    }


}