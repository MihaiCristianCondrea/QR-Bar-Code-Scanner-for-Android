package com.d4rk.qrcodescanner.plus.ui.screens.barcode.otp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityBarcodeOtpBinding
import com.d4rk.qrcodescanner.plus.di.otpGenerator
import com.d4rk.qrcodescanner.plus.utils.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.utils.extension.orZero
import com.d4rk.qrcodescanner.plus.model.schema.OtpAuth
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class OtpActivity : BaseActivity() {
    private lateinit var binding : ActivityBarcodeOtpBinding

    companion object {
        private const val OTP_KEY = "OTP_KEY"
        fun start(context : Context , opt : OtpAuth) {
            val intent = Intent(context , OtpActivity::class.java).apply {
                putExtra(OTP_KEY , opt)
            }
            context.startActivity(intent)
        }
    }

    private var timerJob : Job? = null
    private lateinit var otp : OtpAuth
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableSecurity()
        supportEdgeToEdge()
        parseOtp()
        handleToolbarBackClicked()
        handleRefreshOtpClicked()
        showOtp()
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    private fun enableSecurity() {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE , WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true , applyBottom = true)
    }

    private fun parseOtp() {
        @Suppress("DEPRECATION") otp = intent?.getSerializableExtra(OTP_KEY) as OtpAuth
    }

    private fun handleToolbarBackClicked() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleRefreshOtpClicked() {
        binding.buttonRefresh.setOnClickListener {
            refreshOtp()
        }
    }

    private fun refreshOtp() {
        otp = otp.copy(counter = otp.counter.orZero() + 1L)
        showOtp()
    }

    private fun showOtp() {
        when (otp.type) {
            OtpAuth.HOTP_TYPE -> showHotp()
            OtpAuth.TOTP_TYPE -> showTotp()
        }
        binding.textViewPassword.text = otpGenerator.generateOTP(otp) ?: getString(R.string.unable_to_generate_password)
    }

    private fun showHotp() {
        binding.buttonRefresh.isVisible = true
        binding.textViewCounter.isVisible = true
        binding.textViewCounter.text = getString(R.string.counter , otp.counter.orZero().toString())
    }

    private fun showTotp() {
        binding.textViewTimer.isVisible = true
        startTimer()
    }

    private fun startTimer() {
        val period = otp.period ?: 30
        val currentTimeInSeconds = System.currentTimeMillis() / 1000
        val secondsPassed = currentTimeInSeconds % period
        var secondsLeft = period - secondsPassed
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            while (secondsLeft >= 0) {
                showTime(secondsLeft)
                delay(1000)
                secondsLeft --
            }
            showOtp()
        }
    }

    private fun showTime(secondsLeft : Long) {
        val minutes = secondsLeft / 60
        val seconds = secondsLeft % 60
        binding.textViewTimer.text = getString(R.string.timer_left , minutes.toTime() , seconds.toTime())
    }

    private fun Long.toTime() : String {
        return if (this >= 10) {
            this.toString()
        }
        else {
            "0$this"
        }
    }
}