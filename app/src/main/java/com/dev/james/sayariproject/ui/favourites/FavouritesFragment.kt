package com.dev.james.sayariproject.ui.favourites

import android.os.Bundle
import android.os.NetworkOnMainThreadException
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentFavouritesBinding
import com.dev.james.sayariproject.ui.favourites.adapters.FavouriteAgenciesRecyclerAdapter
import com.dev.james.sayariproject.ui.favourites.viewmodel.FavouritesViewModel
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FavouritesFragment : Fragment(R.layout.fragment_favourites) {

    private var _binding : FragmentFavouritesBinding? = null
    private val binding get() = _binding

    private val favViewModel : FavouritesViewModel by viewModels()


    private val favAgenciesRcAdapter = FavouriteAgenciesRecyclerAdapter { agency ->
        //trigger save agency to db
        Log.d("FavFragment", "Agency selected: $agency ")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavouritesBinding.inflate(inflater , container , false)
        binding?.setUpUi()
        binding?.collectFlows()
        return binding?.root
    }

    private fun FragmentFavouritesBinding.setUpUi(){
        //common ui attributes initialisation

        favouritesToolBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        newsSearchTextInput.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_GO){
                //PERFORM UPDATE
                searchAgency()
                true
            }else{
                false
            }
        }

        newsSearchTextInput.setOnKeyListener { _, keyCode, keyEvent ->
            if(keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                searchAgency()
                true
            }else{
                false
            }
        }

        newsSearchTextInput.addTextChangedListener {
            if(it.toString().isEmpty()){
               searchAgency()
                agencySearchTextViewLayout.isFocusable = false

            }
        }



        favAgenciesRv.adapter = favAgenciesRcAdapter
        favAgenciesRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL , false)




    }



    private fun FragmentFavouritesBinding.collectFlows(){
        lifecycleScope.launchWhenStarted {
            favViewModel.agencySearchResult.collectLatest { resource ->
                when(resource){
                    is NetworkResource.Loading -> {
                        networkErrImg.isVisible = false
                        netErrorMessageFav.isVisible = false
                        favRetryButton.isVisible = false
                    }
                    is NetworkResource.Success -> {
                        if(resource.value.results.isNotEmpty()){
                            favAgenciesRcAdapter.submitList(resource.value.results)
                            favouritesProgressBar.isVisible = false
                            networkErrImg.isVisible = false
                            netErrorMessageFav.isVisible = false
                            favRetryButton.isVisible = false
                        }else {
                            favAgenciesRcAdapter.submitList(emptyList())
                            favouritesProgressBar.isVisible = false
                            networkErrImg.isVisible = false
                            netErrorMessageFav.isVisible = true
                            favRetryButton.isVisible = false
                            netErrorMessageFav.text = getString(R.string.agency_search_error)
                        }
                    }
                    is NetworkResource.Failure ->{
                        val errorBody = resource.errorBody.toString()
                        val errorCode = resource.errorCode
                        Log.d("FavFrag", "collectFlows: error => $errorCode : $errorBody")
                        favouritesProgressBar.isVisible = false
                        networkErrImg.isVisible = true
                        netErrorMessageFav.isVisible = true
                        favAgenciesRcAdapter.submitList(emptyList())
                        netErrorMessageFav.text = getString(R.string.net_err_mess)
                        favRetryButton.isVisible = true

                        favRetryButton.setOnClickListener {
                            searchAgency()
                        }
                    }
                }
            }
        }


    }

    private fun FragmentFavouritesBinding.searchAgency(){
        newsSearchTextInput.text?.trim().let {
            if(it!=null){
                if(it.isNotEmpty()){
                    favouritesProgressBar.isVisible = true
                    agencySearchTextViewLayout.isErrorEnabled = false
                    favAgenciesRv.scrollToPosition(0)
                    favViewModel.searchAgencyFromApi(it.toString())

                } else {
                    agencySearchTextViewLayout.isErrorEnabled = true
                    agencySearchTextViewLayout.error = "please key in a correct search phrase"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}