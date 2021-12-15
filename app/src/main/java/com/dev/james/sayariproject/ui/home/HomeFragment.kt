package com.dev.james.sayariproject.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dev.james.sayariproject.databinding.FragmentHomeBinding
import com.dev.james.sayariproject.models.Article
import com.dev.james.sayariproject.ui.base.BaseFragment
import com.dev.james.sayariproject.ui.home.adapters.ArticlesRecyclerAdapter
import com.dev.james.sayariproject.ui.home.adapters.HomeViewPagerAdapter
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentHomeBinding::inflate

    private val homeViewModel : HomeViewModel by viewModels()

    private val articlesRecyclerAdapter : ArticlesRecyclerAdapter = ArticlesRecyclerAdapter()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpTopNewsViewPager()

        binding.bindState(
            uiState = homeViewModel.uiState,
            pagingData = homeViewModel.pagingDataFlow,
            uiActions = homeViewModel.accept
        )

    }

    private fun FragmentHomeBinding.bindState(
        uiState : StateFlow<UiState>,
        pagingData: Flow<PagingData<Article>>,
        uiActions : (UiAction) -> Unit
    ){

        latesNewsRecyclerView.adapter = articlesRecyclerAdapter
        latesNewsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL , false)

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
                }

                netErrorMess.isVisible = loadState.refresh is LoadState.Error && articlesRecyclerAdapter.itemCount == 0
                networkErrorImage.isVisible = loadState.refresh is LoadState.Error && articlesRecyclerAdapter.itemCount == 0

                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error

                errorState?.let {

                    Toast.makeText(requireContext(),
                        "Woops: ${it.error}",
                        Toast.LENGTH_SHORT)
                        .show()

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
                        val adapter = HomeViewPagerAdapter(topArticles)
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
}