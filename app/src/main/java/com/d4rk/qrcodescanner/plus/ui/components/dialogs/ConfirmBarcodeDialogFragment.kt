package com.d4rk.qrcodescanner.plus.ui.components.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.utils.extension.toStringId
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ConfirmBarcodeDialogFragment : DialogFragment() {
    interface Listener {
        fun onBarcodeConfirmed(barcode: Barcode)
        fun onBarcodeDeclined()
    }

    companion object {
        private const val BARCODE_KEY = "BARCODE_FORMAT_MESSAGE_ID_KEY"
        fun newInstance(barcode: Barcode): ConfirmBarcodeDialogFragment {
            return ConfirmBarcodeDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(BARCODE_KEY, barcode)
                }
                isCancelable = false
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = parentFragment as? Listener
        @Suppress("DEPRECATION") val barcode = arguments?.getSerializable(BARCODE_KEY) as? Barcode
            ?: throw IllegalArgumentException("No barcode passed")
        val messageId = barcode.format.toStringId()
        val dialog = MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.confirm)
            .setMessage(messageId).setIcon(R.drawable.ic_check).setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener?.onBarcodeConfirmed(barcode)
            }.setNegativeButton(android.R.string.cancel) { _, _ ->
                listener?.onBarcodeDeclined()
            }.create()
        return dialog
    }
}