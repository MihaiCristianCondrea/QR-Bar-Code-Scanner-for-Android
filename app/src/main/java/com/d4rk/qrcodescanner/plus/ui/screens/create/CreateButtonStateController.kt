package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.text.TextWatcher
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class CreateButtonStateController(
    private val fragment: BaseCreateBarcodeFragment,
    private val predicate: (List<EditText>) -> Boolean
) : DefaultLifecycleObserver {

    private var lifecycleOwner: LifecycleOwner? = null
    private val editTexts = mutableListOf<EditText>()
    private val textWatchers = mutableMapOf<EditText, TextWatcher>()

    fun bind(lifecycleOwner: LifecycleOwner, vararg editTexts: EditText) {
        release()
        this.lifecycleOwner = lifecycleOwner.also {
            it.lifecycle.addObserver(this)
        }
        this.editTexts.addAll(editTexts)
        editTexts.forEach { editText ->
            val watcher = editText.addTextChangedListener { updateState() }
            textWatchers[editText] = watcher
        }
        updateState()
    }

    fun refresh() {
        updateState()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        release()
    }

    private fun updateState() {
        fragment.parentActivity.isCreateBarcodeButtonEnabled = predicate(editTexts)
    }

    private fun release() {
        textWatchers.forEach { (editText, watcher) ->
            editText.removeTextChangedListener(watcher)
        }
        textWatchers.clear()
        editTexts.clear()
        val owner = lifecycleOwner
        lifecycleOwner = null
        owner?.lifecycle?.removeObserver(this)
    }
}
