package com.d4rk.qrcodescanner.plus.ui.screens.onboarding

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentOnboardingSelectionBinding
import com.d4rk.qrcodescanner.plus.databinding.ItemOnboardingOptionBinding
import com.google.android.material.card.MaterialCardView

class OnboardingSelectionFragment : Fragment(R.layout.fragment_onboarding_selection) {

    enum class SelectionType { THEME, START_DESTINATION }

    private var _binding: FragmentOnboardingSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectionType: SelectionType
    private lateinit var optionHolders: List<OptionHolder>
    private var selectedValue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val typeName = requireArguments().getString(ARG_SELECTION_TYPE)
            ?: SelectionType.THEME.name
        selectionType = SelectionType.valueOf(typeName)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingSelectionBinding.bind(view)

        val config = buildOptions(requireContext(), selectionType)
        optionHolders = listOf(
            OptionHolder(binding.cardFirst, binding.optionFirst, config.options[0]),
            OptionHolder(binding.cardSecond, binding.optionSecond, config.options[1]),
            OptionHolder(binding.cardThird, binding.optionThird, config.options[2]),
        )

        binding.titleText.setText(config.titleRes)
        binding.descriptionText.setText(config.descriptionRes)

        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        selectedValue = preferences.getString(config.preferenceKey, config.defaultValue)
        optionHolders.forEach { holder ->
            bindOption(holder)
        }
        updateSelectionUI(selectedValue)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindOption(holder: OptionHolder) {
        holder.card.isCheckable = true
        holder.card.isClickable = true
        holder.card.isFocusable = true
        holder.binding.iconView.setImageResource(holder.option.iconRes)
        holder.binding.titleText.setText(holder.option.titleRes)
        holder.binding.descriptionText.setText(holder.option.descriptionRes)

        val clickListener = View.OnClickListener { onOptionSelected(holder.option.value) }
        holder.card.setOnClickListener(clickListener)
        holder.binding.radioButton.setOnClickListener(clickListener)
    }

    private fun onOptionSelected(value: String) {
        if (selectedValue == value) {
            return
        }
        selectedValue = value
        storeSelection(value)
        updateSelectionUI(value)
        if (selectionType == SelectionType.THEME) {
            applyTheme(value)
        }
    }

    private fun storeSelection(value: String) {
        val context = requireContext()
        val options = buildOptions(context, selectionType)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit { putString(options.preferenceKey, value) }
    }

    private fun updateSelectionUI(value: String?) {
        optionHolders.forEach { holder ->
            val isSelected = holder.option.value == value
            holder.card.isChecked = isSelected
            holder.binding.radioButton.isChecked = isSelected
        }
    }

    private fun applyTheme(value: String) {
        when (value) {
            MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MODE_NIGHT_AUTO_BATTERY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private data class SelectionOption(
        val value: String,
        @param:DrawableRes val iconRes: Int,
        @param:StringRes val titleRes: Int,
        @param:StringRes val descriptionRes: Int,
    )

    private data class OptionsConfig(
        val preferenceKey: String,
        val defaultValue: String,
        @param:StringRes val titleRes: Int,
        @param:StringRes val descriptionRes: Int,
        val options: List<SelectionOption>,
    )

    private data class OptionHolder(
        val card: MaterialCardView,
        val binding: ItemOnboardingOptionBinding,
        val option: SelectionOption,
    )

    private fun buildOptions(context: Context, type: SelectionType): OptionsConfig {
        val resources = context.resources
        return when (type) {
            SelectionType.THEME -> OptionsConfig(
                preferenceKey = resources.getString(R.string.key_theme),
                defaultValue = resources.getString(R.string.default_value_theme),
                titleRes = R.string.onboarding_theme_title,
                descriptionRes = R.string.onboarding_theme_description,
                options = listOf(
                    SelectionOption(
                        value = MODE_NIGHT_FOLLOW_SYSTEM,
                        iconRes = R.drawable.ic_settings,
                        titleRes = R.string.onboarding_theme_follow_system_title,
                        descriptionRes = R.string.onboarding_theme_follow_system_description,
                    ),
                    SelectionOption(
                        value = MODE_NIGHT_NO,
                        iconRes = R.drawable.ic_brightness_high,
                        titleRes = R.string.onboarding_theme_light_title,
                        descriptionRes = R.string.onboarding_theme_light_description,
                    ),
                    SelectionOption(
                        value = MODE_NIGHT_YES,
                        iconRes = R.drawable.ic_brightness_low,
                        titleRes = R.string.onboarding_theme_dark_title,
                        descriptionRes = R.string.onboarding_theme_dark_description,
                    ),
                ),
            )

            SelectionType.START_DESTINATION -> OptionsConfig(
                preferenceKey = resources.getString(R.string.key_default_tab),
                defaultValue = resources.getString(R.string.default_value_tab),
                titleRes = R.string.onboarding_start_title,
                descriptionRes = R.string.onboarding_start_description,
                options = listOf(
                    SelectionOption(
                        value = VALUE_START_SCAN,
                        iconRes = R.drawable.ic_scan,
                        titleRes = R.string.scan,
                        descriptionRes = R.string.onboarding_start_scan_description,
                    ),
                    SelectionOption(
                        value = VALUE_START_CREATE,
                        iconRes = R.drawable.ic_create_checked,
                        titleRes = R.string.create,
                        descriptionRes = R.string.onboarding_start_create_description,
                    ),
                    SelectionOption(
                        value = VALUE_START_HISTORY,
                        iconRes = R.drawable.ic_history,
                        titleRes = R.string.history,
                        descriptionRes = R.string.onboarding_start_history_description,
                    ),
                ),
            )
        }
    }

    companion object {
        private const val ARG_SELECTION_TYPE = "selection_type"
        private const val MODE_NIGHT_NO = "MODE_NIGHT_NO"
        private const val MODE_NIGHT_YES = "MODE_NIGHT_YES"
        private const val MODE_NIGHT_FOLLOW_SYSTEM = "MODE_NIGHT_FOLLOW_SYSTEM"
        private const val MODE_NIGHT_AUTO_BATTERY = "MODE_NIGHT_AUTO_BATTERY"
        private const val VALUE_START_SCAN = "scan"
        private const val VALUE_START_CREATE = "create"
        private const val VALUE_START_HISTORY = "history"

        fun newInstance(type: SelectionType): OnboardingSelectionFragment {
            return OnboardingSelectionFragment().apply {
                arguments = Bundle().apply { putString(ARG_SELECTION_TYPE, type.name) }
            }
        }
    }
}
