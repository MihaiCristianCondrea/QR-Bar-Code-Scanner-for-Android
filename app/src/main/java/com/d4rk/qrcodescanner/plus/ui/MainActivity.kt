package com.d4rk.qrcodescanner.plus.ui
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.BuildConfig
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityMainBinding
import com.d4rk.qrcodescanner.plus.notifications.AppUpdateNotificationsManager
import com.d4rk.qrcodescanner.plus.notifications.AppUsageNotificationsManager
import com.d4rk.qrcodescanner.plus.ui.settings.SettingsActivity
import com.d4rk.qrcodescanner.plus.ui.settings.help.HelpActivity
import com.d4rk.qrcodescanner.plus.ui.startup.StartupActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationBarView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appUpdateManager: AppUpdateManager
    private val requestUpdateCode = 1
    private lateinit var appUpdateNotificationsManager: AppUpdateNotificationsManager
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private val changelogUrl =
        "https://raw.githubusercontent.com/D4rK7355608/com.d4rk.qrcodescanner.plus/master/CHANGELOG.md"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        MobileAds.initialize(this)
        binding.adView.loadAd(AdRequest.Builder().build())
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateNotificationsManager = AppUpdateNotificationsManager(this)

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            val handled = when (menuItem.itemId) {
                R.id.drawer_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.drawer_help -> {
                    startActivity(Intent(this, HelpActivity::class.java))
                    true
                }
                R.id.drawer_updates -> {
                    startActivity(Intent(Intent.ACTION_VIEW, changelogUrl.toUri()))
                    true
                }
                R.id.drawer_share -> {
                    val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                        )
                        putExtra(Intent.EXTRA_SUBJECT, R.string.share_subject)
                    }
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
                    true
                }
                else -> false
            }
            if (handled) {
                binding.drawerLayout.closeDrawers()
            }
            handled
        }
        applyAppSettings()
    }

    private fun applyAppSettings() {
        val themeValues = resources.getStringArray(R.array.preference_theme_values)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val defaultTabKey = getString(R.string.key_default_tab)
        val defaultTabValue = getString(R.string.default_value_tab)
        val defaultTabValues = resources.getStringArray(R.array.preference_default_tab_values)
        val bottomNavigationBarLabelsKey = getString(R.string.key_bottom_navigation_bar_labels)
        val bottomNavigationBarLabelsValues = resources.getStringArray(R.array.preference_bottom_navigation_bar_labels_values)
        val labelDefaultValue = getString(R.string.default_value_bottom_navigation_bar_labels)

        when (sharedPreferences.getString(getString(R.string.key_theme), getString(R.string.default_value_theme))) {
            themeValues[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            themeValues[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            themeValues[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            themeValues[3] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
        val languageCode = sharedPreferences.getString(getString(R.string.key_language), getString(R.string.default_value_language))
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        val startFragmentId = when (sharedPreferences.getString(defaultTabKey, defaultTabValue)) {
            defaultTabValues[0] -> R.id.navigation_scan
            defaultTabValues[1] -> R.id.navigation_create
            defaultTabValues[2] -> R.id.navigation_history
            else -> R.id.navigation_scan
        }
        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
        navGraph.setStartDestination(startFragmentId)
        navController.graph = navGraph

        binding.navView.setupWithNavController(navController)
        binding.navView.labelVisibilityMode = when (sharedPreferences.getString(bottomNavigationBarLabelsKey, labelDefaultValue)) {
            bottomNavigationBarLabelsValues[0] -> NavigationBarView.LABEL_VISIBILITY_LABELED
            bottomNavigationBarLabelsValues[1] -> NavigationBarView.LABEL_VISIBILITY_SELECTED
            bottomNavigationBarLabelsValues[2] -> NavigationBarView.LABEL_VISIBILITY_UNLABELED
            else -> NavigationBarView.LABEL_VISIBILITY_AUTO
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_scan, R.id.navigation_create, R.id.navigation_history),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.support -> {
              //  startActivity(Intent(this, SupportActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_firebase), true)) {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
        } else {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        }
        val appUsageNotificationsManager = AppUsageNotificationsManager(this)
        appUsageNotificationsManager.checkAndSendAppUsageNotification()
        appUpdateNotificationsManager.checkAndSendUpdateNotification()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                @Suppress("DEPRECATION")
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, requestUpdateCode)
            }
        }
        startupScreen()
    }

    private fun startupScreen() {
        val startupPreference = getSharedPreferences("startup", MODE_PRIVATE)
        if (startupPreference.getBoolean("value", true)) {
            startupPreference.edit { putBoolean("value", false) }
            startActivity(Intent(this, StartupActivity::class.java))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestUpdateCode) {
            when (resultCode) {
                RESULT_OK -> {}
                RESULT_CANCELED -> {}
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {}
            }
        }
    }
}