package com.d4rk.qrcodescanner.plus.ui.screens.settings

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.textview.MaterialTextView
import androidx.core.view.isNotEmpty
import androidx.core.view.updatePaddingRelative

abstract class BasePreferenceFragment(@param:XmlRes private val preferenceResId: Int) : PreferenceFragmentCompat() {
    private var settingsList: RecyclerView? = null
    private var preferenceAdapterObserver: RecyclerView.AdapterDataObserver? = null
    private var preferenceChildAttachListener: RecyclerView.OnChildAttachStateChangeListener? = null
    private var preferenceLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    final override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(preferenceResId, rootKey)
        preferenceScreen?.let(::applyMaterialLayouts)
        onPreferencesCreated()
    }

    protected open fun onPreferencesCreated() = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(null)
        setDividerHeight(0)
        val listView = listView
        settingsList = listView
        val verticalPadding = resources.getDimensionPixelSize(R.dimen.preference_list_vertical_padding)
        listView.setPadding(listView.paddingLeft, verticalPadding, listView.paddingRight, verticalPadding)
        listView.clipToPadding = false
        setupPreferenceCardStyling(listView)
    }

    override fun onDestroyView() {
        settingsList?.let { recyclerView ->
            recyclerView.adapter?.let { adapter ->
                preferenceAdapterObserver?.let(adapter::unregisterAdapterDataObserver)
            }
            preferenceChildAttachListener?.let(recyclerView::removeOnChildAttachStateChangeListener)
            preferenceLayoutListener?.let { listener ->
                val observer = recyclerView.viewTreeObserver
                if (observer.isAlive) {
                    observer.removeOnGlobalLayoutListener(listener)
                }
            }
        }
        preferenceAdapterObserver = null
        preferenceChildAttachListener = null
        preferenceLayoutListener = null
        settingsList = null
        super.onDestroyView()
    }

    private fun applyMaterialLayouts(group: PreferenceGroup) {
        for (index in 0 until group.preferenceCount) {
            val preference = group.getPreference(index)
            when (preference) {
                is PreferenceCategory -> preference.layoutResource = R.layout.item_preference_category
                is SwitchPreferenceCompat -> {
                    preference.layoutResource = R.layout.item_preference
                    preference.widgetLayoutResource = R.layout.widget_preference_switch
                }
                else -> preference.layoutResource = R.layout.item_preference
            }
            preference.isIconSpaceReserved = false
            if (preference is PreferenceGroup) {
                applyMaterialLayouts(preference)
            }
        }
    }

    private fun setupPreferenceCardStyling(listView: RecyclerView) {
        val updateRunnable = Runnable { updatePreferenceCardShapes(listView) }
        listView.adapter?.let { adapter ->
            val observer = object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() { updateRunnable.run() }
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) { updateRunnable.run() }
                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) { updateRunnable.run() }
                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) { updateRunnable.run() }
                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) { updateRunnable.run() }
            }
            adapter.registerAdapterDataObserver(observer)
            preferenceAdapterObserver = observer
        }
        val attachListener = object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) { updateRunnable.run() }
            override fun onChildViewDetachedFromWindow(view: View) { updateRunnable.run() }
        }
        listView.addOnChildAttachStateChangeListener(attachListener)
        preferenceChildAttachListener = attachListener
        val layoutListener = ViewTreeObserver.OnGlobalLayoutListener { updateRunnable.run() }
        listView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        preferenceLayoutListener = layoutListener
        listView.post(updateRunnable)
    }

    private fun updatePreferenceCardShapes(listView: RecyclerView) {
        val adapter = listView.adapter ?: return
        val screen = preferenceScreen ?: return
        val preferences = getVisiblePreferences(screen)
        val itemCount = minOf(adapter.itemCount, preferences.size)
        val spacing = resources.getDimensionPixelSize(R.dimen.preference_item_spacing)
        for (position in 0 until itemCount) {
            val preference = preferences[position]
            val holder = listView.findViewHolderForAdapterPosition(position) ?: continue
            val itemView = holder.itemView
            if (preference is PreferenceCategory) {
                val titleView = itemView.findViewById<MaterialTextView>(android.R.id.title)
                titleView?.let { textView ->
                    val titleText = preference.title
                    val isBlank = titleText?.toString().isNullOrBlank()
                    val padding = if (isBlank) 0 else textView.dp(CATEGORY_PADDING_DP)
                    textView.updatePaddingRelative(
                        start = padding,
                        top = padding,
                        end = padding,
                        bottom = padding
                    )
                    textView.minHeight = 0
                    textView.visibility = if (isBlank) View.GONE else View.VISIBLE
                    textView.text = if (isBlank) null else titleText
                    textView.compoundDrawablePadding = textView.dp(CATEGORY_DRAWABLE_PADDING_DP)
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        preference.icon,
                        null,
                        null,
                        null
                    )
                }
                (itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                    val topMargin = if (position == 0) 0 else spacing
                    val bottomMargin = spacing
                    val horizontalMargin = itemView.dp(CATEGORY_HORIZONTAL_MARGIN_DP)
                    var updated = false
                    if (params.marginStart != horizontalMargin) {
                        params.marginStart = horizontalMargin
                        updated = true
                    }
                    if (params.marginEnd != horizontalMargin) {
                        params.marginEnd = horizontalMargin
                        updated = true
                    }
                    if (params.topMargin != topMargin) {
                        params.topMargin = topMargin
                        updated = true
                    }
                    if (params.bottomMargin != bottomMargin) {
                        params.bottomMargin = bottomMargin
                        updated = true
                    }
                    if (updated) {
                        itemView.layoutParams = params
                    }
                }
                continue
            }
            val card = when (itemView) {
                is MaterialCardView -> itemView
                else -> itemView.findViewById(R.id.lesson_card)
            }
            val first = isFirstPreferenceInSection(preferences, position)
            val last = isLastPreferenceInSection(preferences, position)
            applyRoundedCorners(card, first, last)
            (card.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                var updated = false
                if (params.topMargin != 0) {
                    params.topMargin = 0
                    updated = true
                }
                val bottomMargin = if (last) 0 else spacing
                if (params.bottomMargin != bottomMargin) {
                    params.bottomMargin = bottomMargin
                    updated = true
                }
                if (updated) {
                    card.layoutParams = params
                }
            }
            syncAccessoryVisibility(card)
        }
    }

    private fun isFirstPreferenceInSection(preferences: List<Preference>, position: Int): Boolean {
        for (index in position - 1 downTo 0) {
            val previous = preferences[index]
            if (!previous.isVisible) continue
            return previous is PreferenceCategory
        }
        return true
    }

    private fun isLastPreferenceInSection(preferences: List<Preference>, position: Int): Boolean {
        for (index in position + 1 until preferences.size) {
            val next = preferences[index]
            if (!next.isVisible) continue
            return next is PreferenceCategory
        }
        return true
    }

    private fun applyRoundedCorners(card: MaterialCardView, first: Boolean, last: Boolean) {
        val metrics = card.resources.displayMetrics
        val smallRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, metrics)
        val largeRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, metrics)
        val shape = card.shapeAppearanceModel.toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, if (first) largeRadius else smallRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, if (first) largeRadius else smallRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, if (last) largeRadius else smallRadius)
            .setBottomRightCorner(CornerFamily.ROUNDED, if (last) largeRadius else smallRadius)
            .build()
        card.shapeAppearanceModel = shape
    }

    private fun syncAccessoryVisibility(itemView: View) {
        val iconView = itemView.findViewById<ImageView>(android.R.id.icon)
        iconView?.let { icon ->
            val hasIcon = icon.drawable != null
            icon.visibility = if (hasIcon) View.VISIBLE else View.GONE
        }
        val titleView = itemView.findViewById<MaterialTextView>(android.R.id.title)
        val summaryView = itemView.findViewById<MaterialTextView>(android.R.id.summary)
        val hasTitle = titleView?.text?.isBlank() == false
        val hasSummary = summaryView?.text?.isBlank() == false
        titleView?.visibility = if (hasTitle) View.VISIBLE else View.GONE
        summaryView?.visibility = if (hasSummary) View.VISIBLE else View.GONE
        val textContainer = when {
            titleView?.parent is ViewGroup -> titleView.parent as ViewGroup
            summaryView?.parent is ViewGroup -> summaryView.parent as ViewGroup
            else -> null
        }
        textContainer?.visibility = if (hasTitle || hasSummary) View.VISIBLE else View.GONE
        val widgetFrame = itemView.findViewById<ViewGroup>(android.R.id.widget_frame)
        widgetFrame?.let { frame ->
            val hasChild = frame.isNotEmpty()
            frame.visibility = if (hasChild) View.VISIBLE else View.GONE
            if (hasChild) {
                for (index in 0 until frame.childCount) {
                    frame.getChildAt(index).isDuplicateParentStateEnabled = true
                }
            }
        }
    }

    protected fun bindSwitchPreference(
        keyResId: Int,
        getter: () -> Boolean,
        setter: (Boolean) -> Unit
    ) {
        val preference = findPreference<SwitchPreferenceCompat>(getString(keyResId))
        preference?.apply {
            isChecked = getter()
            setOnPreferenceChangeListener { _, newValue ->
                setter(newValue as Boolean)
                true
            }
        }
    }

    private fun getVisiblePreferences(group: PreferenceGroup): List<Preference> {
        val result = mutableListOf<Preference>()
        collectVisiblePreferences(group, result)
        return result
    }

    private fun collectVisiblePreferences(group: PreferenceGroup, out: MutableList<Preference>) {
        for (index in 0 until group.preferenceCount) {
            val preference = group.getPreference(index)
            if (!preference.isVisible) continue
            out.add(preference)
            if (preference is PreferenceGroup && preference !is PreferenceScreen) {
                collectVisiblePreferences(preference, out)
            }
        }
    }
}

private fun View.dp(dp: Int): Int {
    val density = resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
}

private const val CATEGORY_PADDING_DP = 16
private const val CATEGORY_DRAWABLE_PADDING_DP = 16
private const val CATEGORY_HORIZONTAL_MARGIN_DP = 16
