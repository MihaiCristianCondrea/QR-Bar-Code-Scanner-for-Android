package com.d4rk.qrcodescanner.plus.ui.screens.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.SparseIntArray
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.BuildConfig
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.data.onboarding.OnboardingPreferences
import com.d4rk.qrcodescanner.plus.databinding.ActivityMainBinding
import com.d4rk.qrcodescanner.plus.databinding.LayoutPreferencesBottomSheetBinding
import com.d4rk.qrcodescanner.plus.domain.engagement.AppEngagementRepository
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferencesRepository
import com.d4rk.qrcodescanner.plus.ui.screens.help.HelpActivity
import com.d4rk.qrcodescanner.plus.ui.screens.settings.GeneralPreferenceActivity
import com.d4rk.qrcodescanner.plus.ui.screens.startup.StartupActivity
import com.d4rk.qrcodescanner.plus.ui.screens.support.SupportActivity
import com.d4rk.qrcodescanner.plus.utils.helpers.FirebaseConsentHelper
import com.d4rk.qrcodescanner.plus.utils.helpers.InAppUpdateHelper
import com.d4rk.qrcodescanner.plus.utils.helpers.ReviewHelper
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val STATE_NAV_GRAPH_INITIALIZED = "state_nav_graph_initialized"
        private const val STATE_LAST_PREFERRED_DESTINATION = "state_last_preferred_destination"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val preferencesRepository: MainPreferencesRepository by inject()
    private val engagementRepository: AppEngagementRepository by inject()
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(preferencesRepository)
    }
    private val navOrder = SparseIntArray()
    private val topLevelDestinations = setOf(
        R.id.navigation_scan,
        R.id.navigation_create,
        R.id.navigation_history
    )
    private val adRequest: AdRequest by lazy { buildAdRequest() }
    private val updateResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { }

    private var currentNavIndex = 0
    private var navGraphInitialized = false
    private var lastPreferredStartDestination = 0

    private val changelogUrl =
        "https://raw.githubusercontent.com/D4rK7355608/com.d4rk.qrcodescanner.plus/master/CHANGELOG.md"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        navGraphInitialized =
            savedInstanceState?.getBoolean(STATE_NAV_GRAPH_INITIALIZED, false) ?: false
        lastPreferredStartDestination =
            savedInstanceState?.getInt(STATE_LAST_PREFERRED_DESTINATION, 0) ?: 0

        if (OnboardingPreferences.isFreshInstall(this)) {
            startActivity(Intent(this, StartupActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        //EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)

        setupToolbar()

        MobileAds.initialize(this)
        initNavigationController()
        observeViewModel()
        observeLifecycleEvents()

    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.support -> {
                startActivity(Intent(this, SupportActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        if (menu is MenuBuilder) {
            @Suppress("UsePropertyAccessSyntax")
            menu.setOptionalIconsVisible(true)
        }
        return super.onMenuOpened(featureId, menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_NAV_GRAPH_INITIALIZED, navGraphInitialized)
        outState.putInt(STATE_LAST_PREFERRED_DESTINATION, lastPreferredStartDestination)
    }

    private fun initNavigationController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        navOrder.put(R.id.navigation_scan, 0)
        navOrder.put(R.id.navigation_create, 1)
        navOrder.put(R.id.navigation_history, 2)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentNavIndex = navOrder.get(destination.id, currentNavIndex)
            binding.toolbar.title = destination.label
            updateToolbarForDestination(destination.id)
        }

        binding.navRail.setupWithNavController(navController)
        binding.navRail.setOnItemSelectedListener { item -> handleNavigationSelection(item.itemId) }

        binding.navView.setupWithNavController(navController)
        binding.navView.setOnItemSelectedListener { item -> handleNavigationSelection(item.itemId) }
    }

    private fun updateToolbarForDestination(destId: Int) {
        if (destId in topLevelDestinations) {
            binding.toolbar.navigationIcon =
                AppCompatResources.getDrawable(this, R.drawable.ic_menu)
            binding.toolbar.setNavigationContentDescription(R.string.menu)
            binding.toolbar.setNavigationOnClickListener { showPreferencesBottomSheet() }
        } else {
            val up = AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back)
            binding.toolbar.navigationIcon = up
            binding.toolbar.setNavigationContentDescription(R.string.back)
            binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState.collect { uiState ->
                    applyThemeAndLanguage(uiState)
                    configureNavigation(uiState)
                }
            }
        }
    }

    private fun observeLifecycleEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val analyticsEnabled = fetchAnalyticsEnabled()
                updateAnalyticsCollection(analyticsEnabled)
                refreshAdIfVisible()
                checkForUpdates()
                checkInAppReview()
                showStartupScreenIfNeeded()
            }
        }
    }

    private fun applyThemeAndLanguage(uiState: MainUiState) {
        AppCompatDelegate.setDefaultNightMode(uiState.themeMode)
        val languageTag = uiState.languageTag ?: getString(R.string.default_value_language)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
        if (uiState.requiresRecreation) {
            recreate()
        }
    }

    private fun configureNavigation(uiState: MainUiState) {
        ensureNavGraphConfigured(uiState)

        val useNavigationRail = shouldUseNavigationRail()
        val navigationRail = binding.navRail
        val navigationBar = binding.navView
        val adBanner = binding.adView

        navigationRail.labelVisibilityMode = uiState.bottomNavVisibility
        navigationBar.labelVisibilityMode = uiState.bottomNavVisibility

        if (useNavigationRail) {
            navigationRail.isVisible = true
            navigationBar.isVisible = false
            adBanner.isVisible = false
        } else {
            navigationRail.isVisible = false
            navigationBar.isVisible = true
            adBanner.isVisible = true
            applyBottomBarInsets(navigationBar)
            adBanner.loadAd(adRequest)
        }

        navigationRail.setupWithNavController(navController)
        navigationRail.setOnItemSelectedListener { item ->
            handleNavigationSelection(item.itemId)
        }

        navigationBar.setupWithNavController(navController)
        navigationBar.setOnItemSelectedListener { item ->
            handleNavigationSelection(item.itemId)
        }
    }

    private fun handleNavigationSelection(itemId: Int): Boolean {
        if (navOrder.indexOfKey(itemId) < 0) {
            return false
        }
        val destination = navController.currentDestination
        if (destination != null && destination.id == itemId) {
            return true
        }
        val newIndex = navOrder.get(itemId)
        val forwardOptions = NavOptions.Builder().setEnterAnim(R.anim.fragment_spring_enter)
            .setExitAnim(R.anim.fragment_spring_exit)
            .setPopEnterAnim(R.anim.fragment_spring_pop_enter)
            .setPopExitAnim(R.anim.fragment_spring_pop_exit).build()
        val backwardOptions = NavOptions.Builder().setEnterAnim(R.anim.fragment_spring_pop_enter)
            .setExitAnim(R.anim.fragment_spring_pop_exit)
            .setPopEnterAnim(R.anim.fragment_spring_enter)
            .setPopExitAnim(R.anim.fragment_spring_exit).build()
        val options = if (newIndex > currentNavIndex) forwardOptions else backwardOptions
        navController.navigate(itemId, null, options)
        currentNavIndex = newIndex
        return true
    }

    private fun ensureNavGraphConfigured(uiState: MainUiState) {
        if (!navGraphInitialized) {
            val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
            navGraph.setStartDestination(uiState.defaultNavDestination)
            navController.setGraph(navGraph, null)
            navGraphInitialized = true
            lastPreferredStartDestination = uiState.defaultNavDestination
        } else if (lastPreferredStartDestination == 0) {
            lastPreferredStartDestination = uiState.defaultNavDestination
        } else if (uiState.defaultNavDestination != lastPreferredStartDestination) {
            navigateToPreferredDestination(uiState.defaultNavDestination)
        }
    }

    private fun navigateToPreferredDestination(preferredDestination: Int) {
        val graph: NavGraph = navController.graph
        graph.setStartDestination(preferredDestination)
        val currentDestination = navController.currentDestination
        if (currentDestination != null && currentDestination.id == preferredDestination) {
            lastPreferredStartDestination = preferredDestination
            return
        }
        val options =
            NavOptions.Builder().setPopUpTo(graph.startDestinationId, true).setLaunchSingleTop(true)
                .build()
        navController.navigate(preferredDestination, null, options)
        lastPreferredStartDestination = preferredDestination
    }

    private fun applyBottomBarInsets(bottomBar: NavigationBarView) {
        ViewCompat.setOnApplyWindowInsetsListener(bottomBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(bottomBar)
    }

    private fun shouldUseNavigationRail(): Boolean {
        return resources.configuration.smallestScreenWidthDp >= 600
    }

    private suspend fun checkForUpdates() {
        InAppUpdateHelper.performUpdate(
            appUpdateManager = AppUpdateManagerFactory.create(this@MainActivity),
            updateResultLauncher = updateResultLauncher
        )
    }

    private suspend fun checkInAppReview() {
        val (sessionCount, hasPrompted) = withContext(Dispatchers.IO) {
            val sessionCount = engagementRepository.sessionCount.first()
            val hasPrompted = engagementRepository.hasPromptedReview.first()
            sessionCount to hasPrompted
        }

        ReviewHelper.launchInAppReviewIfEligible(
            activity = this,
            sessionCount = sessionCount,
            hasPromptedBefore = hasPrompted,
            scope = lifecycleScope
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                engagementRepository.setHasPromptedReview(true)
            }
        }

        withContext(Dispatchers.IO) {
            engagementRepository.incrementSessionCount()
        }
    }

    private fun showPreferencesBottomSheet() {
        val bottomSheetBinding = LayoutPreferencesBottomSheetBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.menuSettings.setOnClickListener {
            startActivity(Intent(this, GeneralPreferenceActivity::class.java))
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.menuHelpFeedback.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.menuUpdates.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, changelogUrl.toUri()))
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.menuShare.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                )
                putExtra(Intent.EXTRA_SUBJECT, R.string.share_subject)
            }
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private suspend fun fetchAnalyticsEnabled(): Boolean {
        return withContext(Dispatchers.IO) {
            PreferenceManager.getDefaultSharedPreferences(this@MainActivity).getBoolean(
                getString(R.string.key_firebase), true
            )
        }
    }

    private fun updateAnalyticsCollection(isEnabled: Boolean) {
        FirebaseConsentHelper.setAnalyticsAndCrashlyticsCollectionEnabled(this, isEnabled)
    }

    private fun refreshAdIfVisible() {
        if (binding.adView.isVisible) {
            binding.adView.loadAd(adRequest)
        }
    }

    private fun buildAdRequest(): AdRequest {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val personalizedAds = preferences.getBoolean(getString(R.string.key_personalized_ads), true)
        val builder = AdRequest.Builder()
        if (!personalizedAds) {
            val extras = Bundle().apply { putString("npa", "1") }
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        return builder.build()
    }

    private suspend fun showStartupScreenIfNeeded() {
        val shouldShowStartup = withContext(Dispatchers.IO) {
            val startupPreference = getSharedPreferences("startup", MODE_PRIVATE)
            if (!startupPreference.getBoolean("value", true)) {
                return@withContext false
            }
            startupPreference.edit { putBoolean("value", false) }
            true
        }
        if (shouldShowStartup) {
            startActivity(Intent(this, StartupActivity::class.java))
        }
    }
}
