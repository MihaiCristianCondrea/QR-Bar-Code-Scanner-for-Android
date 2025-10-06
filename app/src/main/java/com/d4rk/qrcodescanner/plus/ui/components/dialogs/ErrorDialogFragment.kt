package com.d4rk.qrcodescanner.plus.ui.components.dialogs
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.d4rk.qrcodescanner.plus.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ErrorDialogFragment : DialogFragment() {
    interface Listener {
        fun onErrorDialogPositiveButtonClicked()
    }
    companion object {
        private const val ERROR_MESSAGE_KEY = "ERROR_MESSAGE_KEY"
        fun newInstance(context: Context, error: Throwable?): ErrorDialogFragment {
            return ErrorDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ERROR_MESSAGE_KEY, getErrorMessage(context, error))
                }
                isCancelable = false
            }
        }
        private fun getErrorMessage(context: Context, error: Throwable?): String {
            return error?.message ?: context.getString(R.string.something_went_wrong)
        }
    }
    private var listener: Listener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? Listener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = arguments?.getString(ERROR_MESSAGE_KEY).orEmpty()
        return MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.error)
            .setMessage(message)
            .setIcon(R.drawable.ic_warning)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> listener?.onErrorDialogPositiveButtonClicked() }
            .create()
    }
}