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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.james.sayariproject.databinding.FragmentSearchBinding
import com.dev.james.sayariproject.ui.search.adapter.DiscoverViewPagerAdapter
import com.dev.james.sayariproject.ui.search.adapter.GalleryRecyclerAdapter
import com.dev.james.sayariproject.ui.search.adapter.MissionsRecyclerAdapter
import com.dev.james.sayariproject.ui.search.viewmodel.DiscoverViewModel
import com.dev.james.sayariproject.utilities.NetworkResource
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var _binding : FragmentSearchBinding? = null
    private val binding get() = _binding

    private var hasMadeInitialCall : Boolean? = null

    private val discoverViewModel : DiscoverViewModel by viewModels()

    private val missionsAdapter = MissionsRecyclerAdapter { message ->
        showSnackBar(message)
    }
    private val galleryAdapter = GalleryRecyclerAdapter{ message ->
        showSnackBar(message)
    }
    private fun showSnackBar(message: String) {
        binding?.root?.let { Snackbar.make(it, message , Snackbar.LENGTH_SHORT ).show() }
    }

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
        startCollectingImagesFlow()
        observeMissions()
        initMaterialTransitions()
        return binding?.root
    }
    private fun initMaterialTransitions() {
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }

    private fun startCollectingImagesFlow() {

            discoverViewModel.myArticlesListForImages.observe(viewLifecycleOwner) { resource ->


                    when (resource) {
                        is NetworkResource.Loading -> {
                            Log.d("DiscFrag", "startCollectingImagesFlow: getting images..")
                        }
                        is NetworkResource.Success -> {
                            Log.d(
                                "DiscFrag",
                                "startCollectingImagesFlow: results : ${resource.value} "
                            )
                            val images = resource.value.take(10)
                            galleryAdapter.submitList(images)
                        }
                        is NetworkResource.Failure -> {
                            val error = resource.errorBody
                            error?.let {
                                Log.d(
                                    "DiscFrag",
                                    "startCollectingImagesFlow: failure: ${it.toString()} "
                                )
                            }
                        }
                    }



            }

    }

    private fun startObservingNewsList() {
        discoverViewModel.newsList.observe(viewLifecycleOwner) { resource ->

                when (resource) {
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
                            discoverDotsIndicator.isVisible = true
                            searchProgressBar.isInvisible = true
                            netErrMessage.isInvisible = true
                            buttonRetry.isInvisible = true
                            val filteredArticles = resource.value
                            val viewPagerAdapter =
                                DiscoverViewPagerAdapter(filteredArticles) { url ->
                                    launchBrowser(url)
                                }
                            Log.i(
                                "DiscFrag",
                                "startObservingNewsList: featured articles ${filteredArticles.toString()}... "
                            )

                            discoverViewPager?.let {
                                it.adapter = viewPagerAdapter
                            }
                            setUpPageIndicatorDots()
                        }
                    }
                    is NetworkResource.Failure -> {
                        Log.d(
                            "DiscFrag",
                            "startObservingNewsList: error: ${resource.errorBody.toString()} "
                        )
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
        discoverViewModel.stringParameter.observe(viewLifecycleOwner) { event ->

            event.getContentIfNotHandled()?.let { query ->
                //trigger ui refresh and change the data
                discoverViewModel.getFilteredResults(query)
                discoverViewModel.getArticlesForImages(query)
                collectMissions(query)
                observedQuery = query
                //           Toast.makeText(requireContext(), "filter param : $query" , Toast.LENGTH_LONG).show()
            }

        }
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

            moreNewsTxt.setOnClickListener {
                val action = SearchFragmentDirections.actionSearchFragmentToNewsFragment()
                findNavController().navigate(action)
            }



            //missions rv
            missionsRv.adapter = missionsAdapter
            missionsRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL , false)

            //gallery rv
            galleryRv.adapter = galleryAdapter
            galleryRv.layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.HORIZONTAL , false)


        }
    }

    private fun collectMissions(category : String){
        discoverViewModel.getMissionsByCategory(category)
    }

    private fun observeMissions(){
        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
                discoverViewModel.missionsList.collect {
                    missionsAdapter.submitList(it)
                }
            }
        }
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