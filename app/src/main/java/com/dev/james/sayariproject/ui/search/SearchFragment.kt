package com.dev.james.sayariproject.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dev.james.sayariproject.databinding.FragmentSearchBinding
import com.dev.james.sayariproject.models.articles.Article
import com.dev.james.sayariproject.ui.home.adapters.HomeViewPagerAdapter
import com.dev.james.sayariproject.ui.search.adapter.DiscoverViewPagerAdapter
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater , container , false)
        setUpUi()
        startObservingFilters()
        startObservingNewsList()
        return binding?.root
    }

    private fun startObservingNewsList() {
        discoverViewModel.newsList.observe(viewLifecycleOwner , { event ->
            event.getContentIfNotHandled()?.let { resource ->

                when(resource){
                    is NetworkResource.Loading -> {
                        Log.d("DiscFrag", "startObservingNewsList: loading... ")
                    }
                    is NetworkResource.Success -> {
                        binding?.apply {
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

        }
    }

    private fun getInitialChipSelected() {
        binding?.apply {
            val initialChipSelection = parentChipGroup.findViewById<Chip>(parentChipGroup.checkedChipId)
                .text.toString()
            discoverViewModel.updateStringParameter(initialChipSelection)
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
}