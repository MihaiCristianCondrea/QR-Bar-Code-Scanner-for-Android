package com.d4rk.qrcodescanner.plus.ui.components.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.DialogEditBarcodeNameBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditBarcodeNameDialogFragment : DialogFragment() {
    private lateinit var binding: DialogEditBarcodeNameBinding

    interface Listener {
        fun onNameConfirmed(name: String)
    }

    companion object {
        private const val NAME_KEY = "NAME_KEY"
        fun newInstance(name: String?): EditBarcodeNameDialogFragment {
            return EditBarcodeNameDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(NAME_KEY, name)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = requireActivity() as? Listener
        val name = arguments?.getString(NAME_KEY).orEmpty()
        binding = DialogEditBarcodeNameBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.edit_name)
            .setView(binding.root).setPositiveButton(android.R.string.ok) { _, _ ->
            val newName = binding.editTextBarcodeName.text.toString()
            listener?.onNameConfirmed(newName)
        }.setIcon(R.drawable.ic_edit).setNegativeButton(android.R.string.cancel, null).create()
        dialog.setOnShowListener {
            initNameEditText(binding.editTextBarcodeName, name)
        }
        dialog.show()
        return dialog
    }

    private fun initNameEditText(editText: EditText, name: String) {
        editText.apply {
            setText(name)
            setSelection(name.length)
            requestFocus()
        }
        val manager =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        manager?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}