package com.d4rk.qrcodescanner.plus.ui.screens.startup

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.d4rk.qrcodescanner.plus.data.startup.StartupConsentRepository
import com.d4rk.qrcodescanner.plus.databinding.ActivityStartupBinding
import com.d4rk.qrcodescanner.plus.ui.screens.main.MainActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class StartupActivity : AppCompatActivity() {
    private lateinit var binding : ActivityStartupBinding

    private val viewModel : StartupViewModel by viewModels {
        StartupViewModelFactory(StartupConsentRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
        observeViewModel()
        viewModel.initialize(this)
        binding.buttonBrowsePrivacyPolicyAndTermsOfService.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW , "https://sites.google.com/view/d4rk7355608/more/apps/privacy-policy".toUri()))
        }
        binding.floatingButtonAgree.setOnClickListener {
            viewModel.onAgreeClicked()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS) , 1)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        binding.progressIndicator.isVisible = uiState.isLoading
                        binding.floatingButtonAgree.isEnabled = uiState.isLoading.not()
                        uiState.errorMessage?.let { message ->
                            Toast.makeText(this@StartupActivity , message , Toast.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }
                launch {
                    viewModel.events.collectLatest { event ->
                        when (event) {
                            StartupUiEvent.NavigateToMain -> {
                                startActivity(Intent(this@StartupActivity , MainActivity::class.java))
                                finish()
                            }
                            is StartupUiEvent.ShowConsentForm -> {
                                event.consentForm.show(this@StartupActivity) {
                                    viewModel.onConsentFormDismissed(this@StartupActivity)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}