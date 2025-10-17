package com.d4rk.qrcodescanner.plus.ui.components.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.view.isVisible
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ViewUsagePreferenceToggleBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView

class UsagePreferenceToggleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewUsagePreferenceToggleBinding.inflate(
        LayoutInflater.from(context),
        this,
        true,
    )

    val iconView: ShapeableImageView
        get() = binding.preferenceIcon

    val titleView: MaterialTextView
        get() = binding.preferenceTitle

    val summaryView: MaterialTextView
        get() = binding.preferenceSummary

    val toggleSwitch: MaterialSwitch
        get() = binding.preferenceSwitch

    init {
        background = null
        isFocusable = true
        isClickable = false

        context.obtainStyledAttributes(attrs, R.styleable.UsagePreferenceToggleView).use { array ->
            if (array.hasValue(R.styleable.UsagePreferenceToggleView_preferenceIcon)) {
                iconView.setImageDrawable(array.getDrawable(R.styleable.UsagePreferenceToggleView_preferenceIcon))
            }
            if (array.hasValue(R.styleable.UsagePreferenceToggleView_preferenceIconTint)) {
                val color =
                    array.getColorStateList(R.styleable.UsagePreferenceToggleView_preferenceIconTint)
                iconView.imageTintList = color
            }
            if (array.hasValue(R.styleable.UsagePreferenceToggleView_preferenceIconContentDescription)) {
                iconView.contentDescription =
                    array.getString(R.styleable.UsagePreferenceToggleView_preferenceIconContentDescription)
            }
            if (array.hasValue(R.styleable.UsagePreferenceToggleView_preferenceTitle)) {
                titleView.text =
                    array.getText(R.styleable.UsagePreferenceToggleView_preferenceTitle)
            }
            if (array.hasValue(R.styleable.UsagePreferenceToggleView_preferenceSummary)) {
                summaryView.text =
                    array.getText(R.styleable.UsagePreferenceToggleView_preferenceSummary)
                summaryView.isVisible = summaryView.text.isNullOrEmpty().not()
            } else {
                summaryView.isVisible = false
            }
            if (array.hasValue(R.styleable.UsagePreferenceToggleView_preferenceSwitchEnabled)) {
                toggleSwitch.isEnabled = array.getBoolean(
                    R.styleable.UsagePreferenceToggleView_preferenceSwitchEnabled,
                    true,
                )
            }
        }
    }

    fun setPreferenceEnabled(enabled: Boolean) {
        isEnabled = enabled
        toggleSwitch.isEnabled = enabled
        iconView.isEnabled = enabled
        titleView.isEnabled = enabled
        summaryView.isEnabled = enabled
    }

    fun setSummaryText(text: CharSequence?) {
        summaryView.text = text
        summaryView.isVisible = text.isNullOrEmpty().not()
    }

    fun setIconTint(colorStateList: ColorStateList?) {
        iconView.imageTintList = colorStateList
    }
}
