package com.d4rk.qrcodescanner.plus.ui.components.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.model.SearchEngine
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChooseSearchEngineDialogFragment : DialogFragment() {
    companion object {
        private val ITEMS = arrayOf(
            SearchEngine.BING , SearchEngine.DUCK_DUCK_GO , SearchEngine.GOOGLE , SearchEngine.STARTPAGE , SearchEngine.QWANT , SearchEngine.YAHOO , SearchEngine.YANDEX
        )
    }

    interface Listener {
        fun onSearchEngineSelected(searchEngine : SearchEngine)
    }

    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog {
        val listener = requireActivity() as? Listener
        val items = arrayOf(
            getString(R.string.activity_choose_search_engine_bing) ,
            getString(R.string.activity_choose_search_engine_duck_duck_go) ,
            getString(R.string.activity_choose_search_engine_google) ,
            getString(R.string.activity_choose_search_engine_qwant) ,
            getString(R.string.activity_choose_search_engine_startpage) ,
            getString(R.string.activity_choose_search_engine_yahoo) ,
            getString(R.string.activity_choose_search_engine_yandex)
        )
        val dialog = MaterialAlertDialogBuilder(requireActivity()).setItems(items) { _ , itemClicked ->
                    val searchEngine = ITEMS[itemClicked]
                    listener?.onSearchEngineSelected(searchEngine)
                }.setNegativeButton(android.R.string.ok) { _ , _ -> }.create()
        return dialog
    }
}