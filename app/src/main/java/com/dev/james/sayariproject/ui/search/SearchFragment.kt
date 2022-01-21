package com.dev.james.sayariproject.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.james.sayariproject.databinding.FragmentSearchBinding
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.ui.home.adapters.HomeViewPagerAdapter
import com.dev.james.sayariproject.ui.search.adapter.DiscoverViewPagerAdapter
import com.dev.james.sayariproject.ui.search.adapter.MissionsRecyclerAdapter
import com.dev.james.sayariproject.ui.search.viewmodel.DiscoverViewModel
import com.dev.james.sayariproject.utilities.NetworkResource
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var _binding : FragmentSearchBinding? = null
    private val binding get() = _binding

    private var hasMadeInitialCall : Boolean? = null

    private val discoverViewModel : DiscoverViewModel by viewModels()

    private val missionsAdapter = MissionsRecyclerAdapter()

    private lateinit var observedQuery : String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater , container , false)
        setUpUi()
        startObservingFilters()
        startObservingNewsList()
        observeMissions()
        return binding?.root
    }

    private fun startObservingNewsList() {
        discoverViewModel.newsList.observe(viewLifecycleOwner , { event ->
            event.getContentIfNotHandled()?.let { resource ->

                when(resource){
                    is NetworkResource.Loading -> {
                        Log.d("DiscFrag", "startObservingNewsList: loading... ")
                        binding?.apply {
                            netErrMessage.isInvisible = true
                            buttonRetry.isInvisible = true
                            searchProgressBar.isVisible = true
                        }
                    }
                    is NetworkResource.Success -> {
                        binding?.apply {
                            discoverViewPager.isVisible = true
                            discoverDotsIndicator.isVisible=true
                            searchProgressBar.isInvisible = true
                            netErrMessage.isInvisible = true
                            buttonRetry.isInvisible = true
                            val filteredArticles =  resource.value
                            val viewPagerAdapter = DiscoverViewPagerAdapter(filteredArticles) { url ->
                                launchBrowser(url)
                            }
                            Log.i("DiscFrag", "startObservingNewsList: featured articles ${filteredArticles.toString()}... ")

                            discoverViewPager?.let {
                                it.adapter = viewPagerAdapter
                            }
                            setUpPageIndicatorDots()
                        }
                    }
                    is NetworkResource.Failure -> {
                        Log.d("DiscFrag", "startObservingNewsList: error: ${resource.errorBody.toString()} ")
                       binding?.apply {
                           discoverViewPager.isInvisible = true
                           discoverDotsIndicator.isInvisible = true
                           searchProgressBar.isInvisible = true
                           netErrMessage.isVisible = true
                           val error = resource.errorBody
                           error?.let {
                               netErrMessage.text = resource.errorBody.toString()
                           }
                           buttonRetry.isVisible = true

                       }
                    }

                }
            }
        })
    }

    private fun setUpPageIndicatorDots() {
        binding?.apply {
            discoverDotsIndicator?.let {
                it.setViewPager2(discoverViewPager)
            }
        }
    }

    //have observable to listen to filter changes
    private fun startObservingFilters() {
        discoverViewModel.stringParameter?.observe(viewLifecycleOwner , { event ->

            event.getContentIfNotHandled()?.let { query ->
                //trigger ui refresh and change the data
                discoverViewModel.getFilteredResults(query)
                collectMissions(query)
                observedQuery = query
                Toast.makeText(requireContext(), "filter param : $query" , Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun setUpUi() {
        getInitialChipSelected()
        binding?.apply {

            val chipList = listOf(moonChip , marsChip ,universeChip ,
            solarSystChip , exoplntChip , sunChip)

            chipList.forEach{ chip ->

            chip.setOnCheckedChangeListener { chipButton, isChecked ->
                if(isChecked){
                    val filter = chipButton.text.toString()
                    //trigger the viemodel to update the observable
                    discoverViewModel.updateStringParameter(filter)
 //                   Toast.makeText(requireContext(), filter, Toast.LENGTH_SHORT).show()
                }
            }
            }

            buttonRetry.setOnClickListener {
                Log.d("SearchFrag", "setUpUi: retry btn clicked ")
                retryArticles()
            }

            missionsRv.adapter = missionsAdapter
            missionsRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL , false)


        }
    }

    private fun collectMissions(category : String){
        discoverViewModel.getMissionsByCategory(category)
    }

    private fun observeMissions(){
        discoverViewModel.missionsList.observe(viewLifecycleOwner , { event ->
            event.getContentIfNotHandled()?.let { missions ->
                Log.d("SearchFrag", "observeMissions: $missions ")
                missionsAdapter.submitList(missions)
            }
        })
    }

    private fun getInitialChipSelected() {
        binding?.apply {
            val initialChipSelection = parentChipGroup.findViewById<Chip>(parentChipGroup.checkedChipId)
                .text.toString()
            discoverViewModel.updateStringParameter(initialChipSelection)
            binding?.searchProgressBar?.isVisible = true
            hasMadeInitialCall = true
  //          Toast.makeText(requireContext(), initialChipSelection, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun launchBrowser(url: String?) {
        url?.let {
            Toast.makeText(requireContext() , "Opening browser..." , Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }?: Toast.makeText(requireContext() , "No news site available" , Toast.LENGTH_LONG).show()
    }

    private fun retryArticles(){
        Log.d("SearchFrag", "retryArticles: retry triggered with query: $observedQuery")
        binding?.searchProgressBar?.isVisible = true
        discoverViewModel.getFilteredResults(observedQuery)
    }
}