package com.d4rk.qrcodescanner.plus.model.schema

import com.d4rk.qrcodescanner.plus.utils.extension.joinToStringNotNullOrBlankWithLineSeparator
import com.d4rk.qrcodescanner.plus.utils.extension.removePrefixIgnoreCase
import com.d4rk.qrcodescanner.plus.utils.extension.startsWithIgnoreCase
import org.apache.commons.codec.binary.Base64
import org.json.JSONObject

class NZCovidTracer(
    val title: String? = null, val addr: String? = null, private val decodedBytes: String? = null
) : Schema {
    companion object {
        private const val PREFIX = "NZCOVIDTRACER:"
        fun parse(text: String): NZCovidTracer? {
            if (text.startsWithIgnoreCase(PREFIX).not()) return null

            return runCatching {
                val decodedBytes = String(Base64().decode(text.removePrefixIgnoreCase(PREFIX)))
                JSONObject(decodedBytes).let { obj ->
                    val title = obj.getString("opn")
                    val address = obj.getString("adr").replace("\\n", "\n")
                    NZCovidTracer(title.trim(), address.trim())
                }
            }.getOrNull()
        }
    }

    override val schema = BarcodeSchema.NZCOVIDTRACER
    override fun toFormattedText(): String =
        listOf(title, addr).joinToStringNotNullOrBlankWithLineSeparator()

    override fun toBarcodeText(): String = "$PREFIX$decodedBytes"
}