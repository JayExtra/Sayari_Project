package com.dev.james.sayariproject.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentHomeBinding
import com.dev.james.sayariproject.ui.dialogs.InformationDialog
import com.dev.james.sayariproject.ui.dialogs.NotificationPermissionDialog
import com.dev.james.sayariproject.ui.home.adapters.HomeViewPagerAdapter
import com.dev.james.sayariproject.ui.home.adapters.LatestArticlesRecyclerAdapter
import com.dev.james.sayariproject.utilities.NetworkResource
import com.dev.james.sayariproject.utilities.showDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    private var dialogJob: Job? = null

    private var hasUserDeniedNotificationPermission: Boolean = false
    private var hasAskedNotificationPermissionFirst: Boolean = false

    private val articlesRecyclerAdapter = LatestArticlesRecyclerAdapter { url ->
        launchBrowser(url)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val rationaleDialog = NotificationPermissionDialog(
        onRelaunchPermissions = {
            //ask for permission again
            sendToSettingsScreen()
        },
        onCancelPermission = {
            hasUserDeniedNotificationPermission = true
        }
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            //User has chosen to completely deny the permission. Respect user's decision.
            //hasUserDeniedNotificationPermission = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setUpLatestNews()

        setupUi()

        startButtonToggleTimer()

        collectDialogFlow()

        return binding.root

    }

    /*override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
*/
    private fun launchBrowser(url: String?) {
        url?.let {
            Toast.makeText(requireContext(), "Opening browser...", Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } ?: Toast.makeText(requireContext(), "No news site available", Toast.LENGTH_LONG).show()
    }

    private fun setupUi() {
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
            latesNewsRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            latesNewsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val scrolledPosition =
                        (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()

                    if (scrolledPosition != null) {
                        if (scrolledPosition >= 1) {
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

    private fun collectDialogFlow() {
        dialogJob = lifecycleScope.launch {
            homeViewModel.hasShownWelcomeDialog.flowWithLifecycle(
                lifecycle,
                Lifecycle.State.CREATED
            )
                .collectLatest { hasShownDialog ->
                    Timber.tag("HomeScreen").d("has shown dialog => $hasShownDialog")
                    if (!hasShownDialog) {
                        requireContext().showDialog(
                            title = R.string.thank_you_ttl ,
                            message = R.string.thank_you_mssg ,
                            onClose = {
                                homeViewModel.hasShownDialog(true)
                                hasAskedNotificationPermissionFirst = true
                                askNotificationPermission()
                            }
                        )
                    }
                }

        }
    }

    override fun onResume() {
        super.onResume()
        Timber.tag("HomeScreen").d("on resume called here")
        if (!hasUserDeniedNotificationPermission && hasAskedNotificationPermissionFirst) {
            askNotificationPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        dialogJob?.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun sendToSettingsScreen() {
        //send user to settings screen and allow notifications
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        ).also { requireActivity().startActivity(it) }

        rationaleDialog.dismiss()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    // FCM SDK (and your app) can post notifications.
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {

                    // TODO: display an educational UI explaining to the user the features that will be enabled
                    //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                    //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                    //       If the user selects "No thanks," allow the user to continue without notifications.

                    rationaleDialog.show(
                        childFragmentManager,
                        NotificationPermissionDialog.TAG
                    )

                }
                /*   !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) &&
                           ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                           PackageManager.PERMISSION_DENIED -> {

                       Toast.makeText(requireContext(), "Sending to settings screen", Toast.LENGTH_SHORT).show()

                   }*/
                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher
                        .launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }


    //all top news slider operations occur here
    private fun setUpTopNewsViewPager() {
        //set up the adapter
        homeViewModel.topArticlesLiveData.observe(viewLifecycleOwner) { resource ->

            when (resource) {
                is NetworkResource.Loading -> {
                    Log.i("HomeFragment", "setUpTopNewsViewPager: loading content... ")
                }

                is NetworkResource.Success -> {
                    val topArticles = resource.value
                    val viewPagerAdapter = HomeViewPagerAdapter(topArticles) { url ->
                        launchBrowser(url)
                    }
                    Log.i(
                        "HomeFragment",
                        "setUpTopNewsViewPager: featured articles ${topArticles.toString()}... "
                    )

                    binding.topNewsViewPager.adapter = viewPagerAdapter
                    setUpPageIndicatorDots()
                }

                is NetworkResource.Failure -> {
                    val errorCode = resource.errorCode
                    val errorBody = resource.errorBody
                    Toast.makeText(
                        requireContext(),
                        "error ($errorCode): ${errorBody.toString()} ",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d(
                        "HomeFragment",
                        "setUpTopNewsViewPager: oh oh : $errorCode : ${errorBody.toString()}"
                    )
                }
            }


        }

    }

    private fun setUpLatestNews() {
        homeViewModel.latestArticlesLiveData.observe(viewLifecycleOwner) { resource ->

            when (resource) {
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
                    Log.d(
                        "HomeFrag",
                        "filtered list: $topArticles , listtSize: ${listSize.toString()} "
                    )
                    articlesRecyclerAdapter.submitList(latestArticles)
                }

                is NetworkResource.Failure -> {
                    binding.homeProgressBar.isVisible = false
                    val errorCode = resource.errorCode
                    val errorBody = resource.errorBody
                    Toast.makeText(
                        requireContext(),
                        "error ($errorCode): ${errorBody.toString()} ",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d(
                        "HomeFragment",
                        "setUpTopNewsViewPager: oh oh : $errorCode : ${errorBody.toString()}"
                    )

                    if (articlesRecyclerAdapter.itemCount == 0) {
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
                    } else {
                        binding.apply {
                            netErrorMess.isVisible = false
                            networkErrorImage.isVisible = false
                        }
                    }

                }
            }


        }
    }

    private fun makeVisible() {
        binding.apply {
            topNewsHeadline.isVisible = true
            topNewsCardView.isVisible = true
            latestNewsHeadline.isVisible = true
            //latesNewsRecyclerView.isVisible = true
        }
    }

    private fun makeInvisible() {
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
    fun View.toggle(show: Boolean) {
        val transition: Transition = Slide(Gravity.RIGHT)
        transition.duration = 200
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup?, transition)
        this.isVisible = show
    }

    private fun refresh() {
        Snackbar.make(binding.root, "Fetching new articles...", Snackbar.LENGTH_SHORT).show()
        makeInvisible()
        homeViewModel.getLatestArticles()
        homeViewModel.getTopArticles()
    }

    private fun startButtonToggleTimer() {
        lifecycleScope.launch {
            Handler().postDelayed({
                binding.btnRefresh.toggle(true)
            }, 300000)
        }
    }


}