package com.d4rk.qrcodescanner.plus.utils.extension

import java.text.DateFormat
import java.util.Date

fun DateFormat.parseOrNull(date : String?) : Date? {
    return try {
        parse(date.orEmpty())
    } catch (_ : Exception) {
        null
    }
}

fun List<DateFormat>.parseOrNull(date : String?) : Date? {
    forEach { dateParser ->
        val parsedDate = dateParser.parseOrNull(date)
        if (parsedDate != null) {
            return parsedDate
        }
    }
    return null
}

fun DateFormat.formatOrNull(time : Long?) : String? {
    return try {
        format(Date(time !!))
    } catch (_ : Exception) {
        null
    }
}