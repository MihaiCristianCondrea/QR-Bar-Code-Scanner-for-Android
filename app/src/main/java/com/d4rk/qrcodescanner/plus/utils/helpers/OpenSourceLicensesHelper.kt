package com.d4rk.qrcodescanner.plus.utils.helpers

import android.content.Context
import android.content.Intent
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

object OpenSourceLicensesHelper {

    fun openLicensesScreen(context: Context) {
        val intent = Intent(context, OssLicensesMenuActivity::class.java)
        context.startActivity(intent)
    }
}
