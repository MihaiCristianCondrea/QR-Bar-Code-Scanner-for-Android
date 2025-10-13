package com.d4rk.qrcodescanner.plus.ui.components.preferences

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.core.view.updatePaddingRelative
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ads.AdUtils
import com.d4rk.qrcodescanner.plus.databinding.ItemPreferenceBinding
import com.d4rk.qrcodescanner.plus.databinding.ItemPreferenceNativeAdBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textview.MaterialTextView
import kotlin.random.Random

class PreferenceListAdapter<T : Any>(
    private val onActionClicked: (T) -> Unit
) : ListAdapter<PreferenceListItem<T>, RecyclerView.ViewHolder>(PreferenceDiffCallback()) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is PreferenceListItem.Category -> TYPE_CATEGORY
        is PreferenceListItem.Action<*> -> TYPE_ACTION
        is PreferenceListItem.NativeAd -> TYPE_AD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_CATEGORY -> CategoryViewHolder.create(parent)
            TYPE_AD -> NativeAdViewHolder(
                ItemPreferenceNativeAdBinding.inflate(inflater, parent, false)
            )

            else -> ActionViewHolder(
                ItemPreferenceBinding.inflate(inflater, parent, false),
                onActionClicked
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PreferenceListItem.Category -> {
                val vh = holder as? CategoryViewHolder
                    ?: error("CategoryViewHolder expected but was ${holder::class.java.simpleName}")
                vh.bind(item)
            }

            is PreferenceListItem.Action<*> -> {
                // Use nested, generic VH + a single localized cast
                if (holder is ActionViewHolder<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (holder as ActionViewHolder<T>).bind(
                        item as PreferenceListItem.Action<T>,
                        isFirstItemInSection(position),
                        isLastItemInSection(position)
                    )
                } else {
                    error("ActionViewHolder expected but was ${holder::class.java.simpleName}")
                }
            }

            is PreferenceListItem.NativeAd -> {
                val vh = holder as? NativeAdViewHolder
                    ?: error("NativeAdViewHolder expected but was ${holder::class.java.simpleName}")
                vh.bind(item)
            }
        }
    }

    private fun isFirstItemInSection(position: Int): Boolean {
        if (position == 0) return true
        return getItemViewType(position - 1) == TYPE_CATEGORY
    }

    private fun isLastItemInSection(position: Int): Boolean {
        if (position == itemCount - 1) return true
        return getItemViewType(position + 1) == TYPE_CATEGORY
    }

    private class CategoryViewHolder(
        private val titleView: MaterialTextView
    ) : RecyclerView.ViewHolder(titleView) {

        fun bind(category: PreferenceListItem.Category) {
            val context = titleView.context
            val resolvedText: CharSequence? = when {
                category.titleText != null -> category.titleText
                category.titleRes != 0 -> context.getText(category.titleRes)
                else -> null
            }

            val isBlank = resolvedText.isNullOrBlank()

            val padding = if (isBlank) 0 else titleView.dp(PADDING_DP)
            titleView.updatePaddingRelative(
                start = padding,
                top = padding,
                end = padding,
                bottom = padding
            )

            titleView.minHeight = 0
            titleView.visibility = if (isBlank && category.hideWhenBlank) {
                View.GONE
            } else {
                View.VISIBLE
            }

            titleView.text = if (isBlank) null else resolvedText
        }

        companion object {
            private const val PADDING_DP = 16

            fun create(parent: ViewGroup): CategoryViewHolder {
                val context = parent.context
                val materialTextView = MaterialTextView(context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).also { params ->
                        val margin = dp(PADDING_DP)
                        params.marginStart = margin
                        params.marginEnd = margin
                    }

                    gravity = Gravity.CENTER_VERTICAL

                    TextViewCompat.setTextAppearance(
                        this,
                        com.google.android.material.R.style.TextAppearance_Material3_BodySmall
                    )

                    setTextColor(
                        MaterialColors.getColor(
                            this,
                            com.google.android.material.R.attr.colorOnSurfaceVariant
                        )
                    )

                    val padding = dp(PADDING_DP)
                    updatePaddingRelative(
                        start = padding,
                        top = padding,
                        end = padding,
                        bottom = padding
                    )
                    compoundDrawablePadding = padding
                }

                return CategoryViewHolder(materialTextView)
            }
        }
    }

    private class NativeAdViewHolder(
        private val binding: ItemPreferenceNativeAdBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PreferenceListItem.NativeAd) {
            val adView = binding.nativeAdView
            if (item.adLayoutRes != 0) {
                adView.setNativeAdLayout(item.adLayoutRes)
            }
            item.adUnitIdRes?.let(adView::setNativeAdUnitId)

            if (adView.getTag(R.id.native_ad_loaded_tag) != true) {
                AdUtils.loadBanner(adView)
                adView.setTag(R.id.native_ad_loaded_tag, true)
            }
        }
    }

    // NOTE: nested (not inner) and explicitly generic to avoid inner+T capture quirks
    private class ActionViewHolder<T : Any>(
        private val binding: ItemPreferenceBinding,
        private val onActionClicked: (T) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PreferenceListItem.Action<T>, first: Boolean, last: Boolean) {
            binding.lessonCard.setOnClickListener { onActionClicked(item.action) }

            if (item.iconRes != 0) {
                binding.icon.setImageResource(item.iconRes)
            }

            if (item.titleRes == 0 && item.iconRes == 0) {
                binding.root.setPadding(0, 0, 0, 0)
            }

            if (item.iconRes != 0) {
                binding.icon.isVisible = true
            } else {
                binding.icon.isVisible = false
            }

            binding.title.setText(item.titleRes)

            if (item.summaryRes != null) {
                binding.summary.setText(item.summaryRes)
                binding.summary.isVisible = true
            } else {
                binding.summary.isVisible = false
            }

            val widgetLayout = item.widgetLayoutRes
            if (widgetLayout != null) {
                if (binding.widgetFrame.tag != widgetLayout) {
                    binding.widgetFrame.removeAllViews()
                    LayoutInflater.from(binding.root.context)
                        .inflate(widgetLayout, binding.widgetFrame, true)
                    binding.widgetFrame.tag = widgetLayout
                }
                binding.widgetFrame.isVisible = true
                binding.widgetFrame.findViewById<MaterialButton>(R.id.open_in_new)?.isEnabled =
                    false
                binding.widgetFrame.findViewById<MaterialButton>(R.id.open_notifications)?.isEnabled =
                    false
            } else {
                binding.widgetFrame.isVisible = false
                if (!binding.widgetFrame.isEmpty()) {
                    binding.widgetFrame.removeAllViews()
                    binding.widgetFrame.tag = null
                }
            }

            applySpacing(binding.lessonCard, last)
            applyCorners(binding.lessonCard, first, last)
        }

        private fun applySpacing(card: MaterialCardView, last: Boolean) {
            val params = card.layoutParams as? ViewGroup.MarginLayoutParams ?: return
            val spacing = card.resources.getDimensionPixelSize(R.dimen.preference_item_spacing)
            val bottomMargin = if (last) 0 else spacing
            if (params.bottomMargin != bottomMargin) {
                params.bottomMargin = bottomMargin
                card.layoutParams = params
            }
        }

        private fun applyCorners(card: MaterialCardView, first: Boolean, last: Boolean) {
            val context = card.context
            val dp4 = context.resources.displayMetrics.density * 4f
            val dp24 = context.resources.displayMetrics.density * 24f
            val shapeBuilder: ShapeAppearanceModel.Builder =
                card.shapeAppearanceModel.toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, if (first) dp24 else dp4)
                    .setTopRightCorner(CornerFamily.ROUNDED, if (first) dp24 else dp4)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, if (last) dp24 else dp4)
                    .setBottomRightCorner(CornerFamily.ROUNDED, if (last) dp24 else dp4)
            card.shapeAppearanceModel = shapeBuilder.build()
        }
    }

    private class PreferenceDiffCallback<T : Any> : DiffUtil.ItemCallback<PreferenceListItem<T>>() {
        override fun areItemsTheSame(
            oldItem: PreferenceListItem<T>,
            newItem: PreferenceListItem<T>
        ): Boolean {
            if (oldItem::class != newItem::class) return false
            return when {
                oldItem is PreferenceListItem.Category && newItem is PreferenceListItem.Category ->
                    oldItem.titleRes == newItem.titleRes &&
                        oldItem.titleText == newItem.titleText &&
                        oldItem.hideWhenBlank == newItem.hideWhenBlank

                oldItem is PreferenceListItem.Action<*> && newItem is PreferenceListItem.Action<*> ->
                    oldItem.action == newItem.action

                oldItem is PreferenceListItem.NativeAd && newItem is PreferenceListItem.NativeAd ->
                    oldItem.adLayoutRes == newItem.adLayoutRes &&
                        oldItem.adUnitIdRes == newItem.adUnitIdRes

                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: PreferenceListItem<T>,
            newItem: PreferenceListItem<T>
        ): Boolean = oldItem == newItem
    }

    companion object {
        private const val TYPE_CATEGORY = 0
        private const val TYPE_ACTION = 1
        private const val TYPE_AD = 2
    }
}

private fun View.dp(dp: Int): Int {
    val density = resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
}

sealed interface PreferenceListItem<out T : Any> {
    data class Category(
        @param:StringRes val titleRes: Int = 0,
        val titleText: CharSequence? = null,
        val hideWhenBlank: Boolean = true
    ) : PreferenceListItem<Nothing>
    data class Action<T : Any>(
        val action: T,
        @param:StringRes val titleRes: Int,
        @param:StringRes val summaryRes: Int? = null,
        @param:DrawableRes val iconRes: Int,
        @param:LayoutRes val widgetLayoutRes: Int? = null
    ) : PreferenceListItem<T>
    data class NativeAd(
        @param:LayoutRes val adLayoutRes: Int = R.layout.ad_preference,
        @param:StringRes val adUnitIdRes: Int? = null
    ) : PreferenceListItem<Nothing>
}

fun <T : Any> List<PreferenceListItem<T>>.withMiddleNativeAd(
    ad: PreferenceListItem.NativeAd = PreferenceListItem.NativeAd(),
    random: Random = Random.Default
): List<PreferenceListItem<T>> {
    if (isEmpty()) return this

    val result = mutableListOf<PreferenceListItem<T>>()
    var index = 0

    while (index < size) {
        val item = this[index]
        if (item is PreferenceListItem.Category) {
            result += item
            index++
            val sectionItems = mutableListOf<PreferenceListItem<T>>()
            while (index < size && this[index] !is PreferenceListItem.Category) {
                sectionItems += this[index]
                index++
            }
            result += sectionItems.withSectionAds(ad, random)
        } else {
            val sectionItems = mutableListOf<PreferenceListItem<T>>()
            while (index < size && this[index] !is PreferenceListItem.Category) {
                sectionItems += this[index]
                index++
            }
            result += sectionItems.withSectionAds(ad, random)
        }
    }

    return result
}

private fun <T : Any> List<PreferenceListItem<T>>.withSectionAds(
    adTemplate: PreferenceListItem.NativeAd,
    random: Random
): List<PreferenceListItem<T>> {
    if (size < MIN_ITEMS_FOR_AD) return this

    val candidateIndices = (1 until size - 1).toMutableList()
    if (candidateIndices.isEmpty()) return this

    val adPositions = mutableListOf<Int>()
    val firstAdIndex = candidateIndices.random(random)
    adPositions += firstAdIndex

    if (size > MIN_ITEMS_FOR_SECOND_AD) {
        candidateIndices.removeAll { index ->
            kotlin.math.abs(index - firstAdIndex) < MIN_DISTANCE_BETWEEN_ADS
        }

        if (candidateIndices.isNotEmpty()) {
            adPositions += candidateIndices.random(random)
        }
    }

    if (adPositions.isEmpty()) return this

    val sortedPositions = adPositions.sorted()
    val iterator = sortedPositions.iterator()
    var currentAdIndex = if (iterator.hasNext()) iterator.next() else null

    val result = mutableListOf<PreferenceListItem<T>>()
    for ((index, item) in this.withIndex()) {
        while (currentAdIndex != null && currentAdIndex == index) {
            result += adTemplate.copy()
            currentAdIndex = if (iterator.hasNext()) iterator.next() else null
        }
        result += item
    }

    return result
}

private const val MIN_ITEMS_FOR_AD = 3
private const val MIN_ITEMS_FOR_SECOND_AD = 10
private const val MIN_DISTANCE_BETWEEN_ADS = 4
