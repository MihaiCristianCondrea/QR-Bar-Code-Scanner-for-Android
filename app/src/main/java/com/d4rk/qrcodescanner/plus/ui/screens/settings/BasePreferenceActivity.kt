package com.d4rk.qrcodescanner.plus.ui.screens.settings

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivitySettingsBinding
import com.d4rk.qrcodescanner.plus.di.mainPreferencesRepository
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BasePreferenceActivity : AppCompatActivity() {
    protected lateinit var binding: ActivitySettingsBinding
    private val preferencesRepository by lazy { mainPreferencesRepository }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(preferencesRepository)
    }

    protected open val toolbarTitleResId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        if (supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings, createPreferenceFragment(), FRAGMENT_TAG)
                .commit()
        }
        toolbarTitleResId?.let(::setTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            settingsViewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { uiState ->
                    SettingsUiApplier.apply(this@BasePreferenceActivity, uiState)
                }
        }
    }

    protected abstract fun createPreferenceFragment(): PreferenceFragmentCompat

    private companion object {
        private const val FRAGMENT_TAG = "settings_fragment"
    }
}
