package com.d4rk.qrcodescanner.plus.ui.components.views
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.LayoutSettingsRadioButtonBinding
class SettingsRadioButton : FrameLayout {
    private var binding: LayoutSettingsRadioButtonBinding
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        binding = LayoutSettingsRadioButtonBinding.inflate(LayoutInflater.from(context), this, true)
        context.obtainStyledAttributes(attrs, R.styleable.SettingsRadioButton).apply {
            showText(this)
            showDelimiter(this)
            recycle()
        }
        binding.root.setOnClickListener {
            binding.radioButton.toggle()
        }
    }
    var isChecked: Boolean
        get() = binding.radioButton.isChecked
        set(value) { binding.radioButton.isChecked = value }
    fun setCheckedChangedListener(listener: ((Boolean) -> Unit)?) {
        binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
            listener?.invoke(isChecked)
        }
    }
    private fun showText(attributes: TypedArray) {
        binding.textViewText.text = attributes.getString(R.styleable.SettingsRadioButton_text).orEmpty()
    }
    private fun showDelimiter(attributes: TypedArray) {
        binding.delimiter.isInvisible = attributes.getBoolean(R.styleable.SettingsRadioButton_isDelimiterVisible, true).not()
    }
}