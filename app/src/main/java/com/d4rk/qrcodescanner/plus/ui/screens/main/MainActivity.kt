package com.d4rk.qrcodescanner.plus.ui.screens.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.SparseIntArray
import android.view.Menu
import android.view.MenuItem
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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.BuildConfig
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityMainBinding
import com.d4rk.qrcodescanner.plus.databinding.LayoutPreferencesBottomSheetBinding
import com.d4rk.qrcodescanner.plus.di.mainPreferencesRepository
import com.d4rk.qrcodescanner.plus.ui.screens.settings.SettingsActivity
import com.d4rk.qrcodescanner.plus.ui.screens.settings.help.HelpActivity
import com.d4rk.qrcodescanner.plus.ui.screens.startup.StartupActivity
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val STATE_NAV_GRAPH_INITIALIZED = "state_nav_graph_initialized"
        private const val STATE_LAST_PREFERRED_DESTINATION = "state_last_preferred_destination"
    }

    private lateinit var binding : ActivityMainBinding
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var appUpdateManager : AppUpdateManager

    private val preferencesRepository by lazy { mainPreferencesRepository }
    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory(preferencesRepository)
    }
    private val navOrder = SparseIntArray()
    private val requestUpdateCode = 1
    private val adRequest by lazy { AdRequest.Builder().build() }

    private var currentNavIndex = 0
    private var navGraphInitialized = false
    private var lastPreferredStartDestination = 0

    private val changelogUrl = "https://raw.githubusercontent.com/D4rK7355608/com.d4rk.qrcodescanner.plus/master/CHANGELOG.md"

    override fun onCreate(savedInstanceState : Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        navGraphInitialized = savedInstanceState?.getBoolean(STATE_NAV_GRAPH_INITIALIZED , false) ?: false
        lastPreferredStartDestination = savedInstanceState?.getInt(STATE_LAST_PREFERRED_DESTINATION , 0) ?: 0

        binding = ActivityMainBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)

        setupEdgeToEdge()

        setSupportActionBar(binding.toolbar)
        configureToolbarNavigation()

        MobileAds.initialize(this)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        initNavigationController()
        observeViewModel()

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner : LifecycleOwner) {
                val analyticsEnabled = PreferenceManager.getDefaultSharedPreferences(this@MainActivity).getBoolean(
                    getString(R.string.key_firebase) , true
                )
                FirebaseAnalytics.getInstance(this@MainActivity).setAnalyticsCollectionEnabled(analyticsEnabled)
                FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = analyticsEnabled

                appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        @Suppress("DEPRECATION") appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo , AppUpdateType.FLEXIBLE , this@MainActivity , requestUpdateCode
                        )
                    }
                }

                if (binding.adView.isVisible) {
                    binding.adView.loadAd(adRequest)
                }

                startupScreen()
            }
        })

    }

    override fun onCreateOptionsMenu(menu : Menu) : Boolean {
        menuInflater.inflate(R.menu.menu_main , menu)
        return true
    }

    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        return when (item.itemId) {
            R.id.support -> {
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

    override fun onSupportNavigateUp() : Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onSaveInstanceState(outState : Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_NAV_GRAPH_INITIALIZED , navGraphInitialized)
        outState.putInt(STATE_LAST_PREFERRED_DESTINATION , lastPreferredStartDestination)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        if (requestCode == requestUpdateCode) {
            when (resultCode) {
                RESULT_OK -> Unit
                RESULT_CANCELED -> Unit
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> Unit
            }
        }
    }

    private fun initNavigationController() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        navOrder.put(R.id.navigation_scan , 0)
        navOrder.put(R.id.navigation_create , 1)
        navOrder.put(R.id.navigation_history , 2)

        navController.addOnDestinationChangedListener { _ , destination , _ ->
            currentNavIndex = navOrder.get(destination.id , currentNavIndex)
            binding.toolbar.post { configureToolbarNavigation() }
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_scan , R.id.navigation_create , R.id.navigation_history)
        )
        setupActionBarWithNavController(navController , appBarConfiguration)
        binding.toolbar.post { configureToolbarNavigation() }
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

    private fun applyThemeAndLanguage(uiState : MainUiState) {
        AppCompatDelegate.setDefaultNightMode(uiState.themeMode)
        val languageTag = uiState.languageTag ?: getString(R.string.default_value_language)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
        if (uiState.themeChanged) {
            recreate()
        }
    }

    private fun configureNavigation(uiState : MainUiState) {
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
        }
        else {
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

    private fun handleNavigationSelection(itemId : Int) : Boolean {
        if (navOrder.indexOfKey(itemId) < 0) {
            return false
        }
        val destination = navController.currentDestination
        if (destination != null && destination.id == itemId) {
            return true
        }
        val newIndex = navOrder.get(itemId)
        val forwardOptions = NavOptions.Builder().setEnterAnim(R.anim.fragment_spring_enter).setExitAnim(R.anim.fragment_spring_exit).setPopEnterAnim(R.anim.fragment_spring_pop_enter).setPopExitAnim(R.anim.fragment_spring_pop_exit).build()
        val backwardOptions = NavOptions.Builder().setEnterAnim(R.anim.fragment_spring_pop_enter).setExitAnim(R.anim.fragment_spring_pop_exit).setPopEnterAnim(R.anim.fragment_spring_enter).setPopExitAnim(R.anim.fragment_spring_exit).build()
        val options = if (newIndex > currentNavIndex) forwardOptions else backwardOptions
        navController.navigate(itemId , null , options)
        currentNavIndex = newIndex
        return true
    }

    private fun ensureNavGraphConfigured(uiState : MainUiState) {
        if (! navGraphInitialized) {
            val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
            navGraph.setStartDestination(uiState.defaultNavDestination)
            navController.setGraph(navGraph , null)
            navGraphInitialized = true
            lastPreferredStartDestination = uiState.defaultNavDestination
        }
        else if (lastPreferredStartDestination == 0) {
            lastPreferredStartDestination = uiState.defaultNavDestination
        }
        else if (uiState.defaultNavDestination != lastPreferredStartDestination) {
            navigateToPreferredDestination(uiState.defaultNavDestination)
        }
    }

    private fun navigateToPreferredDestination(preferredDestination : Int) {
        val graph : NavGraph = navController.graph
        graph.setStartDestination(preferredDestination)
        val currentDestination = navController.currentDestination
        if (currentDestination != null && currentDestination.id == preferredDestination) {
            lastPreferredStartDestination = preferredDestination
            return
        }
        val options = NavOptions.Builder().setPopUpTo(graph.startDestinationId , true).setLaunchSingleTop(true).build()
        navController.navigate(preferredDestination , null , options)
        lastPreferredStartDestination = preferredDestination
    }

    private fun applyBottomBarInsets(bottomBar : NavigationBarView) {
        ViewCompat.setOnApplyWindowInsetsListener(bottomBar) { view , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(bottomBar)
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view , insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBars.top)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragmentActivityMain) { view , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(left = systemBars.left , right = systemBars.right)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun shouldUseNavigationRail() : Boolean {
        return resources.configuration.smallestScreenWidthDp >= 600
    }

    private fun configureToolbarNavigation() {
        val navigationIcon = AppCompatResources.getDrawable(this , R.drawable.ic_menu)
        binding.toolbar.navigationIcon = navigationIcon
        binding.toolbar.setNavigationContentDescription(R.string.menu)
        binding.toolbar.setNavigationOnClickListener { showPreferencesBottomSheet() }
    }

    private fun showPreferencesBottomSheet() {
        val bottomSheetBinding = LayoutPreferencesBottomSheetBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.menuSettings.setOnClickListener {
            startActivity(Intent(this , SettingsActivity::class.java))
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.menuHelpFeedback.setOnClickListener {
            startActivity(Intent(this , HelpActivity::class.java))
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.menuUpdates.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW , changelogUrl.toUri()))
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.menuShare.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT , "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                )
                putExtra(Intent.EXTRA_SUBJECT , R.string.share_subject)
            }
            startActivity(Intent.createChooser(sharingIntent , getString(R.string.share_using)))
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun startupScreen() {
        val startupPreference = getSharedPreferences("startup" , MODE_PRIVATE)
        if (startupPreference.getBoolean("value" , true)) {
            startupPreference.edit { putBoolean("value" , false) }
            startActivity(Intent(this , StartupActivity::class.java))
        }
    }
}
