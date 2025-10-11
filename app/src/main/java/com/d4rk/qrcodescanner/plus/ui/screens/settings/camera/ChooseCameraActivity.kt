package com.d4rk.qrcodescanner.plus.ui.screens.settings.camera

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.d4rk.qrcodescanner.plus.databinding.ActivityChooseCameraBinding
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class ChooseCameraActivity : BaseActivity() {
    private lateinit var binding: ActivityChooseCameraBinding

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChooseCameraActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseCameraBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }

    override fun onResume() {
        super.onResume()
        showSelectedCamera()
        handleBackCameraButtonChecked()
        handleFrontCameraButtonChecked()
    }

    private fun showSelectedCamera() {
        val isBackCamera = settings.isBackCamera
        binding.buttonBackCamera.isChecked = isBackCamera
        binding.buttonFrontCamera.isChecked = isBackCamera.not()
    }

    private fun handleBackCameraButtonChecked() {
        binding.buttonBackCamera.setCheckedChangedListener { isChecked ->
            if (isChecked) {
                binding.buttonFrontCamera.isChecked = false
            }
            settings.isBackCamera = isChecked
        }
    }

    private fun handleFrontCameraButtonChecked() {
        binding.buttonFrontCamera.setCheckedChangedListener { isChecked ->
            if (isChecked) {
                binding.buttonBackCamera.isChecked = false
            }
            settings.isBackCamera = isChecked.not()
        }
    }
}