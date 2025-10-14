package com.d4rk.qrcodescanner.plus.ui.screens.barcode.otp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityBarcodeOtpBinding
import com.d4rk.qrcodescanner.plus.domain.create.OTPGenerator
import com.d4rk.qrcodescanner.plus.model.schema.OtpAuth
import com.d4rk.qrcodescanner.plus.ui.components.navigation.UpNavigationActivity
import com.d4rk.qrcodescanner.plus.ui.components.navigation.setupToolbarWithUpNavigation
import com.d4rk.qrcodescanner.plus.utils.extension.orZero
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.inject

class OtpActivity : UpNavigationActivity() {
    private lateinit var binding: ActivityBarcodeOtpBinding
    private val otpGenerator: OTPGenerator by inject()

    companion object {
        private const val OTP_KEY = "OTP_KEY"
        fun start(context: Context, opt: OtpAuth) {
            val intent = Intent(context, OtpActivity::class.java).apply {
                putExtra(OTP_KEY, opt)
            }
            context.startActivity(intent)
        }
    }

    @Suppress("DEPRECATION")
    private val otpArgs: OtpAuth by lazy(LazyThreadSafetyMode.NONE) {
        val otp = intent?.getSerializableExtra(OTP_KEY) as? OtpAuth
        requireNotNull(otp) { "OtpActivity requires an OtpAuth argument" }
    }

    private val viewModel: OtpViewModel by viewModels {
        OtpViewModelFactory(otpArgs, otpGenerator)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeOtpBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        setupToolbarWithUpNavigation()
        enableSecurity()
        handleRefreshOtpClicked()
        collectUiState()
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }

    private fun enableSecurity() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    private fun handleRefreshOtpClicked() {
        binding.buttonRefresh.setOnClickListener {
            viewModel.refreshOtp()
        }
    }

    private fun collectUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    renderUiState(uiState)
                }
            }
        }
    }

    private fun renderUiState(uiState: OtpUiState) {
        binding.buttonRefresh.isVisible = uiState.isHotp
        binding.textViewCounter.isVisible = uiState.isHotp
        if (uiState.isHotp) {
            binding.textViewCounter.text =
                getString(R.string.counter, uiState.counter.orZero().toString())
        }

        val timerVisible = uiState.isTotp && uiState.timerState is TimerUiState.Visible
        binding.textViewTimer.isVisible = timerVisible
        if (timerVisible) {
            val timerState = uiState.timerState
            binding.textViewTimer.text = getString(
                R.string.timer_left,
                timerState.minutes.toTimeComponent(),
                timerState.seconds.toTimeComponent()
            )
        }

        binding.textViewPassword.text =
            uiState.password ?: getString(R.string.unable_to_generate_password)
    }

    private fun Long.toTimeComponent(): String {
        return if (this >= 10) {
            toString()
        } else {
            "0$this"
        }
    }
}
