package com.dev.james.sayariproject.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dev.james.sayariproject.databinding.FragmentHomeBinding
import com.dev.james.sayariproject.models.Article
import com.dev.james.sayariproject.ui.base.BaseFragment
import com.dev.james.sayariproject.ui.home.adapters.ArticlesRecyclerAdapter
import com.dev.james.sayariproject.ui.home.adapters.HomeViewPagerAdapter
import com.dev.james.sayariproject.utilities.NetworkResource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentHomeBinding::inflate

    private val homeViewModel : HomeViewModel by viewModels()

    private val articlesRecyclerAdapter = ArticlesRecyclerAdapter { url ->
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpTopNewsViewPager()

        setupUi()

        startButtonToggleTimer()

        binding.bindState(
            uiState = homeViewModel.uiState,
            pagingData = homeViewModel.pagingDataFlow,
            uiActions = homeViewModel.accept
        )

    }

    fun setupUi(){
        binding.apply {
            buttonMoveUp.setOnClickListener {
                lifecycleScope.launch {
                    latesNewsRecyclerView.scrollToPosition(0)
                    delay(100)
                    buttonMoveUp.toggle(false)
                }
            }

            btnRefresh.setOnClickListener {
                refresh()
                it.toggle(false)
                startButtonToggleTimer()
            }
        }
    }

    private fun FragmentHomeBinding.bindState(
        uiState : StateFlow<UiState>,
        pagingData: Flow<PagingData<Article>>,
        uiActions : (UiAction) -> Unit
    ){

        latesNewsRecyclerView.adapter = articlesRecyclerAdapter
        latesNewsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL , false)

        latesNewsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val scrolledPosition =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()

                if (scrolledPosition != null){
                    if(scrolledPosition >= 1){
                        buttonMoveUp.toggle(true)
                    } else {
                        buttonMoveUp.toggle(false)
                    }
                }
            }
        })

        bindList(
            uiState = uiState,
            pagingData = pagingData
        )
    }

    private fun FragmentHomeBinding.bindList(
        uiState : StateFlow<UiState>,
        pagingData: Flow<PagingData<Article>>
    ){
        lifecycleScope.launchWhenStarted {
            pagingData.collectLatest(articlesRecyclerAdapter::submitData)
        }

        lifecycleScope.launchWhenStarted {
            articlesRecyclerAdapter.loadStateFlow.collect { loadState ->
                val isEmpty = loadState.refresh is LoadState.NotLoading && articlesRecyclerAdapter.itemCount == 0

                if(isEmpty) makeInvisible()

                if(loadState.refresh is LoadState.Error && articlesRecyclerAdapter.itemCount == 0) makeInvisible()


                homeProgressBar.isVisible = loadState.refresh is LoadState.Loading
                retryButton.isVisible = loadState.refresh is LoadState.Error && articlesRecyclerAdapter.itemCount == 0

                retryButton.setOnClickListener {
                    articlesRecyclerAdapter.retry()
                    homeViewModel.getTopArticles()

                }

                netErrorMess.isVisible = loadState.refresh is LoadState.Error && articlesRecyclerAdapter.itemCount == 0
                networkErrorImage.isVisible = loadState.refresh is LoadState.Error && articlesRecyclerAdapter.itemCount == 0

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


    //all top news slider operations occur here
    private fun setUpTopNewsViewPager() {
        //set up the adapter
        homeViewModel.topArticlesLiveData.observe(viewLifecycleOwner , { event ->
            event.getContentIfNotHandled()?.let { resource ->

                when(resource){
                    is NetworkResource.Loading -> {
                        Log.i("HomeFragment", "setUpTopNewsViewPager: loading content... ")
                    }
                    is NetworkResource.Success -> {
                        makeVisible()
                        val topArticles =  resource.value
                        val adapter = HomeViewPagerAdapter(topArticles) { url ->
                            launchBrowser(url)
                        }
                        binding.topNewsViewPager.adapter = adapter
                        setUpPageIndicatorDots()
                    }

                    is NetworkResource.Failure -> {
                        val errorCode = resource.errorCode
                        val errorBody = resource.errorBody
                        Toast.makeText(requireContext(), "error ($errorCode): ${errorBody.toString()} ", Toast.LENGTH_LONG).show()
                        Log.d("HomeFragment", "setUpTopNewsViewPager: oh oh : $errorCode : ${errorBody.toString()}")
                    }
                }

            }
        })

    }

    private fun makeVisible(){
        binding.apply {
            topNewsHeadline.isVisible = true
            topNewsCardView.isVisible = true
            latestNewsHeadline.isVisible = true
            //latesNewsRecyclerView.isVisible = true
        }
    }

    private fun makeInvisible(){
        binding.apply {
            topNewsHeadline.isInvisible = true
            topNewsCardView.isInvisible = true
            latestNewsHeadline.isInvisible = true
            //latesNewsRecyclerView.isInvisible = true
        }

    }



    private fun setUpPageIndicatorDots() {
        binding.apply {
            dotsIndicator.setViewPager2(topNewsViewPager)
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

    private fun refresh(){
        Snackbar.make(binding.root , "Fetching new articles...", Snackbar.LENGTH_SHORT).show()
        makeInvisible()
        articlesRecyclerAdapter.refresh()
        homeViewModel.getTopArticles()
    }

    private fun startButtonToggleTimer(){
        lifecycleScope.launch {
            Handler().postDelayed({
                binding.btnRefresh.toggle(true)
            }
                ,300000)
        }
    }
}