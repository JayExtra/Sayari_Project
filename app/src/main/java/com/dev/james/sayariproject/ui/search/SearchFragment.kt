package com.dev.james.sayariproject.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.dev.james.sayariproject.databinding.FragmentSearchBinding
import com.dev.james.sayariproject.ui.base.BaseFragment
import com.dev.james.sayariproject.ui.search.viewmodel.DiscoverViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var _binding : FragmentSearchBinding? = null
    private val binding get() = _binding

    private val discoverViewModel : DiscoverViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater , container , false)
        setUpUi()
        startObservingFilters()
        return binding?.root
    }

    //have observable to listen to filter changes
    private fun startObservingFilters() {
        discoverViewModel.stringParameter?.observe(viewLifecycleOwner , { event ->

            event.getContentIfNotHandled()?.let {
                //trigger ui refresh and change the data
                Toast.makeText(requireContext(), "filter param : $it" , Toast.LENGTH_LONG).show()
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
  //          Toast.makeText(requireContext(), initialChipSelection, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}