package com.d4rk.qrcodescanner.plus.utils.extension

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.ErrorDialogFragment

val Fragment.packageManager : PackageManager get() = requireContext().packageManager
fun Fragment.showError(error : Throwable?) {
    val errorDialog = ErrorDialogFragment.newInstance(requireContext() , error)
    errorDialog.show(childFragmentManager , "")
}