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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentHomeBinding
import com.dev.james.sayariproject.ui.home.adapters.HomeViewPagerAdapter
import com.dev.james.sayariproject.ui.home.adapters.LatestArticlesRecyclerAdapter
import com.dev.james.sayariproject.utilities.NetworkResource
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel : HomeViewModel by activityViewModels()

    private val articlesRecyclerAdapter = LatestArticlesRecyclerAdapter { url ->
        launchBrowser(url)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater , container , false)

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
        initMaterialTransitions()
        homeViewModel.getTopArticles()
        homeViewModel.getLatestArticles()

        binding.apply {
            buttonReadMore.setOnClickListener {
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

    private fun initMaterialTransitions() {
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }

    override fun onResume() {
        super.onResume()

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
                        val topArticles =  resource.value
                        val viewPagerAdapter = HomeViewPagerAdapter(topArticles) { url ->
                            launchBrowser(url)
                        }
                        Log.i("HomeFragment", "setUpTopNewsViewPager: featured articles ${topArticles.toString()}... ")

                        binding.topNewsViewPager.adapter = viewPagerAdapter
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
                        makeVisible()
                        setUpTopNewsViewPager()
                        binding.homeProgressBar.isVisible = false
                        binding.retryButton.isInvisible = true
                        Log.d("HomeFrag", "setUpLatestNews: ${resource.value}")
                        val latestArticles = resource.value
                        val listSize = latestArticles.size
                        val topArticles = latestArticles.filter { it.featured }
                        Log.d("HomeFrag", "filtered list: $topArticles , listtSize: ${listSize.toString()} ")
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