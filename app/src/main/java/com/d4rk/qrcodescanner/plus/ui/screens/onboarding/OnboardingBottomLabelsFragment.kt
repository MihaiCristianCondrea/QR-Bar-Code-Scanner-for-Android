package com.d4rk.qrcodescanner.plus.ui.screens.onboarding

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.edit
import androidx.core.text.inSpans
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentOnboardingBottomLabelsBinding
import com.d4rk.qrcodescanner.plus.databinding.ItemOnboardingSimpleOptionBinding
import com.google.android.material.card.MaterialCardView

class OnboardingBottomLabelsFragment : Fragment(R.layout.fragment_onboarding_bottom_labels) {

    private var _binding: FragmentOnboardingBottomLabelsBinding? = null
    private val binding get() = _binding!!

    private lateinit var optionHolders: List<OptionHolder>
    private var selectedValue: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingBottomLabelsBinding.bind(view)

        optionHolders = listOf(
            OptionHolder(
                binding.cardLabeled,
                binding.optionLabeled,
                Option(
                    value = VALUE_LABELED,
                    titleRes = R.string.labeled,
                    descriptionRes = R.string.onboarding_labels_all,
                ),
            ),
            OptionHolder(
                binding.cardSelected,
                binding.optionSelected,
                Option(
                    value = VALUE_SELECTED,
                    titleRes = R.string.selected_variant,
                    descriptionRes = R.string.onboarding_labels_selected,
                ),
            ),
            OptionHolder(
                binding.cardUnlabeled,
                binding.optionUnlabeled,
                Option(
                    value = VALUE_UNLABELED,
                    titleRes = R.string.unlabeled,
                    descriptionRes = R.string.onboarding_labels_hidden,
                ),
            ),
        )

        val resources = requireContext().resources
        val preferenceKey = resources.getString(R.string.key_bottom_navigation_bar_labels)
        val defaultValue = resources.getString(R.string.default_value_bottom_navigation_bar_labels)
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        selectedValue = preferences.getString(preferenceKey, defaultValue)

        optionHolders.forEach { holder ->
            holder.card.isCheckable = true
            holder.card.isClickable = true
            holder.card.isFocusable = true
            holder.binding.titleText.text = formatOptionText(holder.option)
            val clickListener =
                View.OnClickListener { onOptionSelected(holder.option.value, preferenceKey) }
            holder.card.setOnClickListener(clickListener)
            holder.binding.radioButton.setOnClickListener(clickListener)
        }

        updateSelectionUI(selectedValue)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun formatOptionText(option: Option): CharSequence {
        val builder = SpannableStringBuilder()
        builder.inSpans(StyleSpan(Typeface.BOLD)) {
            append(getString(option.titleRes))
        }
        builder.append('\n')
        builder.append(getString(option.descriptionRes))
        return builder
    }

    private fun onOptionSelected(value: String, preferenceKey: String) {
        if (selectedValue == value) {
            return
        }
        selectedValue = value
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferences.edit { putString(preferenceKey, value) }
        updateSelectionUI(value)
    }

    private fun updateSelectionUI(value: String?) {
        optionHolders.forEach { holder ->
            val isSelected = holder.option.value == value
            holder.card.isChecked = isSelected
            holder.binding.radioButton.isChecked = isSelected
        }
    }

    private data class Option(
        val value: String,
        val titleRes: Int,
        val descriptionRes: Int,
    )

    private data class OptionHolder(
        val card: MaterialCardView,
        val binding: ItemOnboardingSimpleOptionBinding,
        val option: Option,
    )

    companion object {
        private const val VALUE_LABELED = "labeled"
        private const val VALUE_SELECTED = "selected"
        private const val VALUE_UNLABELED = "unlabeled"
    }
}
