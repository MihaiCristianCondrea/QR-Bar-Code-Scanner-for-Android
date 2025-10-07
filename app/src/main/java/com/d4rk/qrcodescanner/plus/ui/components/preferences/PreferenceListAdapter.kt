package com.d4rk.qrcodescanner.plus.ui.components.preferences

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ItemPreferenceBinding
import com.d4rk.qrcodescanner.plus.databinding.ItemPreferenceCategoryBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

class PreferenceListAdapter<T : Any>(
    private val onActionClicked: (T) -> Unit
) : ListAdapter<PreferenceListItem<T>, RecyclerView.ViewHolder>(PreferenceDiffCallback()) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is PreferenceListItem.Category -> TYPE_CATEGORY
        is PreferenceListItem.Action<*> -> TYPE_ACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_CATEGORY -> CategoryViewHolder(
                ItemPreferenceCategoryBinding.inflate(inflater, parent, false)
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
        private val binding: ItemPreferenceCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: PreferenceListItem.Category) {
            binding.title.setText(category.titleRes)
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
                binding.widgetFrame.findViewById<MaterialButton>(R.id.open_in_new)?.isEnabled = false
                binding.widgetFrame.findViewById<MaterialButton>(R.id.open_notifications)?.isEnabled = false
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
                    oldItem.titleRes == newItem.titleRes

                oldItem is PreferenceListItem.Action<*> && newItem is PreferenceListItem.Action<*> ->
                    oldItem.action == newItem.action

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
    }
}

sealed interface PreferenceListItem<out T : Any> {
    data class Category(@param:StringRes val titleRes: Int) : PreferenceListItem<Nothing>
    data class Action<T : Any>(
        val action: T,
        @param:StringRes val titleRes: Int,
        @param:StringRes val summaryRes: Int? = null,
        @param:DrawableRes val iconRes: Int,
        @param:LayoutRes val widgetLayoutRes: Int? = null
    ) : PreferenceListItem<T>
}
