package com.d4rk.qrcodescanner.plus.model.schema

import com.d4rk.qrcodescanner.plus.utils.extension.joinToStringNotNullOrBlankWithLineSeparator
import com.d4rk.qrcodescanner.plus.utils.extension.startsWithIgnoreCase
import com.d4rk.qrcodescanner.plus.utils.extension.unsafeLazy
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BoardingPass(
    val name: String? = null,
    private val pnr: String? = null,
    val from: String? = null,
    private val to: String? = null,
    private val carrier: String? = null,
    private val flight: String? = null,
    val date: String? = null,
    @Suppress("unused") val dateJ: Int = 0,
    private val cabin: String? = null,
    private val seat: String? = null,
    private val seq: String? = null,
    private val ticket: String? = null,
    private val selectee: String? = null,
    private val ffAirline: String? = null,
    private val ffNo: String? = null,
    private val fasttrack: String? = null,
    private val blob: String? = null,
) : Schema {
    companion object {
        private val DATE_FORMATTER by unsafeLazy { SimpleDateFormat("d MMMM", Locale.ENGLISH) }
        fun parse(text: String): BoardingPass? {
            return runCatching {
                text.let {
                    if (it.length < 60) return@let null
                    if (it.startsWithIgnoreCase("M1").not()) return@let null
                    if (it[22] != 'E') return@let null

                    val fieldSize: Int = it.slice(58..59).toInt(16)
                    if (fieldSize != 0 && it[60] != '>') return@let null
                    if (it.length > 60 + fieldSize && it[60 + fieldSize] != '^') return@let null

                    val name = it.slice(2..21).trim()
                    val pnr = it.slice(23..29).trim()
                    val from = it.slice(30..32)
                    val to = it.slice(33..35)
                    val carrier = it.slice(36..38).trim()
                    val flight = it.slice(39..43).trim()
                    val dateJ = it.slice(44..46).toInt()
                    val cabin = it.slice(47..47)
                    val seat = it.slice(48..51).trim()
                    val seq = it.slice(52..56)
                    val today = Calendar.getInstance()
                    today.set(Calendar.DAY_OF_YEAR, dateJ)
                    val date: String = DATE_FORMATTER.format(today.time)

                    var selectee: String? = null
                    var ticket: String? = null
                    var ffAirline: String? = null
                    var ffNo: String? = null
                    var fasttrack: String? = null

                    if (fieldSize != 0) {
                        val size: Int = it.slice(62..63).toInt(16)
                        if (size != 0 && size < 11) return@let null

                        val size1: Int = it.slice(64 + size..65 + size).toInt(16)
                        if (size1 != 0 && (size1 < 37 || size1 > 42)) return@let null

                        ticket = it.slice(66 + size..78 + size).trim()
                        selectee = it.slice(79 + size..79 + size)
                        ffAirline = it.slice(84 + size..86 + size).trim()
                        ffNo = it.slice(87 + size..102 + size).trim()
                        if (size1 == 42) {
                            fasttrack = it.slice(107 + size..107 + size)
                        }
                    }
                    BoardingPass(
                        name,
                        pnr,
                        from,
                        to,
                        carrier,
                        flight,
                        date,
                        dateJ,
                        cabin,
                        seat,
                        seq,
                        ticket,
                        selectee,
                        ffAirline,
                        ffNo,
                        fasttrack,
                        it
                    )
                }
            }.getOrNull()
        }
    }

    override val schema = BarcodeSchema.BOARDINGPASS
    override fun toFormattedText(): String = listOf(
        name,
        pnr,
        "$from->$to",
        "$carrier$flight",
        date,
        cabin,
        seat,
        seq,
        ticket,
        selectee,
        "$ffAirline$ffNo",
        fasttrack
    ).joinToStringNotNullOrBlankWithLineSeparator()

    override fun toBarcodeText(): String {
        return blob ?: ""
    }
}