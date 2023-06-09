package com.dev.james.sayariproject.ui.support

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dev.james.sayariproject.databinding.FragmentSupportUsBinding
import com.dev.james.sayariproject.utilities.BUY_ME_COFFEE_PAGE_LINK
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportUsFragment : Fragment() {

    private var _binding : FragmentSupportUsBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSupportUsBinding.inflate(
            inflater ,
            container ,
            false
        )

        binding?.setUpUi()
        return binding?.root
    }

    private fun FragmentSupportUsBinding.setUpUi(){
        supportUsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        buyMeCoffeBtn.setOnClickListener {
            openBuyMeCoffeePage()
        }
    }

    private fun openBuyMeCoffeePage(){
        val pageIntent = Intent().apply {
            this.action = Intent.ACTION_VIEW
            this.data = Uri.parse(BUY_ME_COFFEE_PAGE_LINK)
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        requireContext().startActivity(pageIntent)
    }
}