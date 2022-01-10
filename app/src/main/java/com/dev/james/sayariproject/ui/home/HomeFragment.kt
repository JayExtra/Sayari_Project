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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentHomeBinding
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.ui.base.BaseFragment
import com.dev.james.sayariproject.ui.home.adapters.ArticlesRecyclerAdapter
import com.dev.james.sayariproject.ui.home.adapters.HomeViewPagerAdapter
import com.dev.james.sayariproject.ui.home.adapters.LatestArticlesRecyclerAdapter
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
class HomeFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel : HomeViewModel by viewModels()

    private val articlesRecyclerAdapter = LatestArticlesRecyclerAdapter { url ->
        launchBrowser(url)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater , container , false)

        setUpTopNewsViewPager()

        setUpLatestNews()

        setupUi()

        startButtonToggleTimer()

        return binding.root

    }

    private fun launchBrowser(url: String?) {
        url?.let {
            Toast.makeText(requireContext() , "Opening browser..." , Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }?: Toast.makeText(requireContext() , "No news site available" , Toast.LENGTH_LONG).show()
    }

    fun setupUi(){
        binding.apply {
            buttonReadMore.setOnClickListener {
                /**lifecycleScope.launch {
                    latesNewsRecyclerView.scrollToPosition(0)
                    delay(100)
                    buttonMoveUp.toggle(false)
                }
                **/
                //navigate to news fragment
                findNavController().navigate(R.id.action_homeFragment_to_newsFragment)
            }

            btnRefresh.setOnClickListener {
                refresh()
                it.toggle(false)
                startButtonToggleTimer()
            }

            latesNewsRecyclerView.adapter = articlesRecyclerAdapter
            latesNewsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL , false)

            latesNewsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val scrolledPosition =
                        (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()

                    if (scrolledPosition != null){
                        if(scrolledPosition >= 1){
                            buttonReadMore.toggle(true)
                        } else {
                            buttonReadMore.toggle(false)
                        }
                    }
                }
            })
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

    private fun setUpLatestNews(){
        homeViewModel.latestArticlesLiveData.observe(viewLifecycleOwner , { event ->
            event.getContentIfNotHandled()?.let { resource ->

                when(resource){
                    is NetworkResource.Loading -> {
                        binding.homeProgressBar.isVisible = true
                        makeInvisible()
                    }

                    is NetworkResource.Success -> {
                        binding.homeProgressBar.isVisible = false
                        binding.retryButton.isInvisible = true
                        Log.d("HomeFrag", "setUpLatestNews: ${resource.value}")
                        val latestArticles = resource.value
                        articlesRecyclerAdapter.submitList(latestArticles)
                    }

                    is NetworkResource.Failure -> {
                        binding.homeProgressBar.isVisible = false
                        val errorCode = resource.errorCode
                        val errorBody = resource.errorBody
                        Toast.makeText(requireContext(), "error ($errorCode): ${errorBody.toString()} ", Toast.LENGTH_LONG).show()
                        Log.d("HomeFragment", "setUpTopNewsViewPager: oh oh : $errorCode : ${errorBody.toString()}")

                        if(articlesRecyclerAdapter.itemCount == 0 ){
                            binding.apply {
                                netErrorMess.isVisible = true
                                networkErrorImage.isVisible = true
                                retryButton.isVisible = true

                                retryButton.setOnClickListener {
                                    makeInvisible()
                                    homeViewModel.getLatestArticles()
                                    homeViewModel.getTopArticles()
                                }
                            }
                        }else{
                            binding.apply {
                                netErrorMess.isVisible = false
                                networkErrorImage.isVisible = false
                            }
                        }

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
        homeViewModel.getLatestArticles()
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