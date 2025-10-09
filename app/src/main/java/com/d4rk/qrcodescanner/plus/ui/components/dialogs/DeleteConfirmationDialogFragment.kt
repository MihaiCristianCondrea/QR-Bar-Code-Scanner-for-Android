package com.d4rk.qrcodescanner.plus.ui.components.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.utils.extension.orZero
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeleteConfirmationDialogFragment : DialogFragment() {
    companion object {
        private const val MESSAGE_ID_KEY = "MESSAGE_ID_KEY"
        fun newInstance(messageId : Int) : DeleteConfirmationDialogFragment {
            return DeleteConfirmationDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(MESSAGE_ID_KEY , messageId)
                }
                isCancelable = false
            }
        }
    }

    interface Listener {
        fun onDeleteConfirmed()
    }

    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog {
        val listener = requireActivity() as? Listener ?: parentFragment as? Listener
        val messageId = arguments?.getInt(MESSAGE_ID_KEY).orZero()
        return MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.dialog_delete).setMessage(messageId).setIcon(R.drawable.ic_delete).setPositiveButton(R.string.delete) { _ , _ ->
                    listener?.onDeleteConfirmed()
                }.setNegativeButton(android.R.string.cancel , null).create()
    }
}