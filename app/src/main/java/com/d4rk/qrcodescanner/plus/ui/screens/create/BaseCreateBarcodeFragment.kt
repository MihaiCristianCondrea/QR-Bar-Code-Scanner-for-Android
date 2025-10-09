package com.d4rk.qrcodescanner.plus.ui.screens.create

import androidx.fragment.app.Fragment
import com.d4rk.qrcodescanner.plus.model.Contact
import com.d4rk.qrcodescanner.plus.model.schema.Other
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.utils.extension.unsafeLazy

abstract class BaseCreateBarcodeFragment : Fragment() {
    protected val parentActivity by unsafeLazy { requireActivity() as CreateBarcodeActivity }
    open val latitude : Double? = null
    open val longitude : Double? = null
    open fun getBarcodeSchema() : Schema = Other("")
    open fun showPhone(phone : String) {}
    open fun showContact(contact : Contact) {}
    open fun showLocation(latitude : Double? , longitude : Double?) {}
}