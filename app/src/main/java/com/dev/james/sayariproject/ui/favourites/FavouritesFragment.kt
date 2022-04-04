package com.dev.james.sayariproject.ui.favourites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
        return binding?.root
    }

    private fun FragmentFavouritesBinding.setUpUi(){
        //common ui attributes initialisation
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}