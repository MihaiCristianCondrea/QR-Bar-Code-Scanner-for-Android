package com.d4rk.qrcodescanner.plus.model.schema

import com.d4rk.qrcodescanner.plus.utils.extension.joinToStringNotNullOrBlankWithLineSeparator
import com.d4rk.qrcodescanner.plus.utils.extension.startsWithIgnoreCase
import com.d4rk.qrcodescanner.plus.utils.extension.unsafeLazy
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BoardingPass(
    val name : String? = null ,
    private val pnr : String? = null ,
    val from : String? = null ,
    private val to : String? = null ,
    private val carrier : String? = null ,
    private val flight : String? = null ,
    val date : String? = null ,
    @Suppress("unused") val dateJ : Int = 0 ,
    private val cabin : String? = null ,
    private val seat : String? = null ,
    private val seq : String? = null ,
    private val ticket : String? = null ,
    private val selectee : String? = null ,
    private val ffAirline : String? = null ,
    private val ffNo : String? = null ,
    private val fasttrack : String? = null ,
    private val blob : String? = null ,
) : Schema {
    companion object {
        private val DATE_FORMATTER by unsafeLazy { SimpleDateFormat("d MMMM" , Locale.ENGLISH) }
        fun parse(text : String) : BoardingPass? {
            try {
                if (text.length < 60) {
                    return null
                }
                if (text.startsWithIgnoreCase("M1").not()) {
                    return null
                }
                if (text[22] != 'E') {
                    return null
                }
                val fieldSize : Int = text.slice(58..59).toInt(16)
                if (fieldSize != 0 && text[60] != '>') {
                    return null
                }
                if (text.length > 60 + fieldSize && text[60 + fieldSize] != '^') {
                    return null
                }
                val name = text.slice(2..21).trim()
                val pnr = text.slice(23..29).trim()
                val from = text.slice(30..32)
                val to = text.slice(33..35)
                val carrier = text.slice(36..38).trim()
                val flight = text.slice(39..43).trim()
                val dateJ = text.slice(44..46).toInt()
                val cabin = text.slice(47..47)
                val seat = text.slice(48..51).trim()
                val seq = text.slice(52..56)
                val today = Calendar.getInstance()
                today.set(Calendar.DAY_OF_YEAR , dateJ)
                val date : String = DATE_FORMATTER.format(today.time)
                var selectee : String? = null
                var ticket : String? = null
                var ffAirline : String? = null
                var ffNo : String? = null
                var fasttrack : String? = null
                if (fieldSize != 0) {
                    @Suppress("UNUSED_VARIABLE") val version : Int = text.slice(61..61).toInt()
                    val size : Int = text.slice(62..63).toInt(16)
                    if (size != 0 && size < 11) {
                        return null
                    }
                    val size1 : Int = text.slice(64 + size..65 + size).toInt(16)
                    if (size1 != 0 && (size1 < 37 || size1 > 42)) {
                        return null
                    }
                    else {
                        ticket = text.slice(66 + size..78 + size).trim()
                        selectee = text.slice(79 + size..79 + size)
                        ffAirline = text.slice(84 + size..86 + size).trim()
                        ffNo = text.slice(87 + size..102 + size).trim()
                        if (size1 == 42) {
                            fasttrack = text.slice(107 + size..107 + size)
                        }
                    }
                }
                return BoardingPass(
                    name , pnr , from , to , carrier , flight , date , dateJ , cabin , seat , seq , ticket , selectee , ffAirline , ffNo , fasttrack , text
                )
            } catch (_ : Exception) {
                return null
            }
        }
    }

    override val schema = BarcodeSchema.BOARDINGPASS
    override fun toFormattedText() : String = listOf(name , pnr , "$from->$to" , "$carrier$flight" , date , cabin , seat , seq , ticket , selectee , "$ffAirline$ffNo" , fasttrack).joinToStringNotNullOrBlankWithLineSeparator()
    override fun toBarcodeText() : String {
        return blob ?: ""
    }
}