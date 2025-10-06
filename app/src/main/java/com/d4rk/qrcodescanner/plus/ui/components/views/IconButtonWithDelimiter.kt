package com.d4rk.qrcodescanner.plus.ui.components.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.LayoutIconButtonWithDelimiterBinding

class IconButtonWithDelimiter : FrameLayout {
    private var binding : LayoutIconButtonWithDelimiterBinding

    constructor(context : Context) : this(context , null)
    constructor(context : Context , attrs : AttributeSet?) : this(context , attrs , - 1)
    constructor(context : Context , attrs : AttributeSet? , defStyleAttr : Int) : super(context , attrs , defStyleAttr) {
        binding = LayoutIconButtonWithDelimiterBinding.inflate(LayoutInflater.from(context) , this , true)
        context.obtainStyledAttributes(attrs , R.styleable.IconButtonWithDelimiter).apply {
            showIcon(this)
            showText(this)
            showArrow(this)
            showDelimiter(this)
            recycle()
        }
    }

    private fun showIcon(attributes : TypedArray) {
        val iconResId = attributes.getResourceId(R.styleable.IconButtonWithDelimiter_icon , - 1)
        val icon = AppCompatResources.getDrawable(context , iconResId)
        binding.imageViewSchema.setImageDrawable(icon)
    }

    private fun showText(attributes : TypedArray) {
        binding.textView.text = attributes.getString(R.styleable.IconButtonWithDelimiter_text).orEmpty()
    }

    private fun showArrow(attributes : TypedArray) {
        binding.imageViewArrow.isVisible = attributes.getBoolean(R.styleable.IconButtonWithDelimiter_isArrowVisible , false)
    }

    private fun showDelimiter(attributes : TypedArray) {
        binding.delimiter.isInvisible = attributes.getBoolean(R.styleable.IconButtonWithDelimiter_isDelimiterVisible , true).not()
    }

    override fun setEnabled(enabled : Boolean) {
        super.setEnabled(enabled)
        binding.imageViewSchema.isEnabled = enabled
        binding.textView.isEnabled = enabled
    }
}