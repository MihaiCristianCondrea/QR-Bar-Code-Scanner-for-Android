package com.d4rk.qrcodescanner.plus.ui.components.preferences

import android.util.TypedValue
import android.view.ViewGroup
import com.d4rk.qrcodescanner.plus.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily

object PreferenceCardStyler {
    private const val SMALL_CORNER_DP = 4f
    private const val LARGE_CORNER_DP = 24f

    fun applySpacing(
        card: MaterialCardView,
        isLastItem: Boolean,
        spacingPx: Int = card.resources.getDimensionPixelSize(R.dimen.preference_item_spacing),
        resetTopMargin: Boolean = false,
    ) {
        val params = card.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        var updated = false
        if (resetTopMargin && params.topMargin != 0) {
            params.topMargin = 0
            updated = true
        }
        val bottomMargin = if (isLastItem) 0 else spacingPx
        if (params.bottomMargin != bottomMargin) {
            params.bottomMargin = bottomMargin
            updated = true
        }
        if (updated) {
            card.layoutParams = params
        }
    }

    fun applyRoundedCorners(card: MaterialCardView, isFirstItem: Boolean, isLastItem: Boolean) {
        val metrics = card.resources.displayMetrics
        val smallRadius =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SMALL_CORNER_DP, metrics)
        val largeRadius =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LARGE_CORNER_DP, metrics)
        val shape = card.shapeAppearanceModel.toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, if (isFirstItem) largeRadius else smallRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, if (isFirstItem) largeRadius else smallRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, if (isLastItem) largeRadius else smallRadius)
            .setBottomRightCorner(
                CornerFamily.ROUNDED,
                if (isLastItem) largeRadius else smallRadius
            )
            .build()
        card.shapeAppearanceModel = shape
    }
}
