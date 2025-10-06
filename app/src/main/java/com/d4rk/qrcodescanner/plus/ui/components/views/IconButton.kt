package com.d4rk.qrcodescanner.plus.ui.components.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.LayoutIconButtonBinding

class IconButton : FrameLayout {
    private var binding : LayoutIconButtonBinding
    var text : String
        get() = binding.textView.text.toString()
        set(value) {
            binding.textView.text = value
        }

    constructor(context : Context) : this(context , null)
    constructor(context : Context , attrs : AttributeSet?) : this(context , attrs , - 1)
    constructor(context : Context , attrs : AttributeSet? , defStyleAttr : Int) : super(context , attrs , defStyleAttr) {
        binding = LayoutIconButtonBinding.inflate(LayoutInflater.from(context) , this , true)
        context.obtainStyledAttributes(attrs , R.styleable.IconButton).apply {
            showIcon(this)
            showText(this)
            recycle()
        }
    }

    private fun showIcon(attributes : TypedArray) {
        val iconResId = attributes.getResourceId(R.styleable.IconButton_icon , - 1)
        val icon = AppCompatResources.getDrawable(context , iconResId)
        binding.imageViewSchema.setImageDrawable(icon)
    }

    private fun showText(attributes : TypedArray) {
        binding.textView.text = attributes.getString(R.styleable.IconButton_text)
    }

    override fun setEnabled(enabled : Boolean) {
        super.setEnabled(enabled)
        binding.imageViewSchema.isEnabled = enabled
        binding.textView.isEnabled = enabled
    }
}