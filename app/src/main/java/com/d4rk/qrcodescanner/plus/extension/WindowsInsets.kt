package com.d4rk.qrcodescanner.plus.extension

import android.view.View
import android.view.WindowInsets

@Suppress("DEPRECATION")
fun View.applySystemWindowInsets(applyLeft : Boolean = false , applyTop : Boolean = false , applyRight : Boolean = false , applyBottom : Boolean = false) {
    doOnApplyWindowInsets { view , insets , padding ->
        val left = if (applyLeft) insets.systemWindowInsetLeft else 0
        val top = if (applyTop) insets.systemWindowInsetTop else 0
        val right = if (applyRight) insets.systemWindowInsetRight else 0
        val bottom = if (applyBottom) insets.systemWindowInsetBottom else 0
        view.setPadding(
            padding.left + left , padding.top + top , padding.right + right , padding.bottom + bottom
        )
    }
}

fun View.doOnApplyWindowInsets(f : (View , WindowInsets , InitialPadding) -> Unit) {
    val initialPadding = recordInitialPaddingForView(this)
    setOnApplyWindowInsetsListener { v , insets ->
        f(v , insets , initialPadding)
        insets
    }
    requestApplyInsetsWhenAttached()
}

data class InitialPadding(
    val left : Int , val top : Int , val right : Int , val bottom : Int
)

private fun recordInitialPaddingForView(view : View) = InitialPadding(
    view.paddingLeft , view.paddingTop , view.paddingRight , view.paddingBottom
)

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    }
    else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v : View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v : View) = Unit
        })
    }
}