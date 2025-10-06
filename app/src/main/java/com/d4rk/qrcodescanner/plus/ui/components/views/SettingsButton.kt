package com.d4rk.qrcodescanner.plus.ui.components.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.LayoutSettingsButtonBinding

class SettingsButton : FrameLayout {
    private var binding : LayoutSettingsButtonBinding

    constructor(context : Context) : this(context , null)
    constructor(context : Context , attrs : AttributeSet?) : this(context , attrs , - 1)
    constructor(context : Context , attrs : AttributeSet? , defStyleAttr : Int) : super(context , attrs , defStyleAttr) {
        binding = LayoutSettingsButtonBinding.inflate(LayoutInflater.from(context) , this , true)
        context.obtainStyledAttributes(attrs , R.styleable.SettingsButton).apply {
            showText(this)
            showHint(this)
            showSwitch(this)
            recycle()
        }
    }

    var hint : String
        get() = binding.textViewHint.text.toString()
        set(value) {
            binding.textViewHint.apply {
                text = value
                isVisible = text.isNullOrEmpty().not()
            }
        }
    var isChecked : Boolean
        get() = binding.switchButton.isChecked
        set(value) {
            binding.switchButton.isChecked = value
        }

    override fun setEnabled(enabled : Boolean) {
        super.setEnabled(enabled)
        binding.textViewText.isEnabled = enabled
    }

    fun setCheckedChangedListener(listener : ((Boolean) -> Unit)?) {
        binding.switchButton.setOnCheckedChangeListener { _ , isChecked ->
            listener?.invoke(isChecked)
        }
    }

    private fun showText(attributes : TypedArray) {
        binding.textViewText.text = attributes.getString(R.styleable.SettingsButton_text).orEmpty()
    }

    private fun showHint(attributes : TypedArray) {
        hint = attributes.getString(R.styleable.SettingsButton_hint).orEmpty()
    }

    private fun showSwitch(attributes : TypedArray) {
        binding.switchButton.isVisible = attributes.getBoolean(R.styleable.SettingsButton_isSwitchVisible , true)
        if (binding.switchButton.isVisible) {
            binding.switchButton.setOnClickListener {
                binding.switchButton.toggle()
            }
        }
    }
}