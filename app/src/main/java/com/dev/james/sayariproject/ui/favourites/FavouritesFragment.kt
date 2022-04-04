package com.dev.james.sayariproject.ui.favourites

import android.os.Bundle
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
            favViewModel.agencySearchResult.collectLatest { agencyList ->
                favAgenciesRcAdapter.submitList(agencyList)
            }

            favViewModel.uiActions.collectLatest { uiActions ->
                favouritesProgressBar.isVisible = uiActions.showProgressBar
                netErrorMessageFav.isVisible = uiActions.showNetErrMessage
                networkErrImg.isVisible = uiActions.showNetImage
                favRetryButton.isVisible = uiActions.showRetryButton
                if(uiActions.errorMessage!=null){
                    netErrorMessageFav.text = uiActions.errorMessage
                }
            }

        }
    }

    private fun FragmentFavouritesBinding.searchAgency(){
        newsSearchTextInput.text?.trim().let {
            if(it!=null){
                if(it.isNotEmpty()){
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