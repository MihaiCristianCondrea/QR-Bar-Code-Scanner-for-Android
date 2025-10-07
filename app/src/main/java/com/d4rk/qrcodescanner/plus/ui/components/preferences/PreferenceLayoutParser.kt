package com.d4rk.qrcodescanner.plus.ui.components.preferences

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import android.util.Xml
import androidx.core.content.res.use
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

sealed interface PreferenceLayoutEntry {
    data class Category(@StringRes val titleRes: Int) : PreferenceLayoutEntry

    data class Action(
        val key: String,
        @StringRes val titleRes: Int,
        @StringRes val summaryRes: Int?,
        @DrawableRes val iconRes: Int,
        @LayoutRes val widgetLayoutRes: Int?
    ) : PreferenceLayoutEntry
}

object PreferenceLayoutParser {

    @Throws(XmlPullParserException::class)
    fun parse(context: Context, @XmlRes xmlResId: Int): List<PreferenceLayoutEntry> {
        val entries = mutableListOf<PreferenceLayoutEntry>()
        val parser = context.resources.getXml(xmlResId)

        parser.use {
            var eventType = it.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when {
                        it.name.endsWith("PreferenceCategory") -> {
                            parseCategory(context, it)?.let(entries::add)
                        }

                        it.name.endsWith("Preference") -> {
                            parseAction(context, it)?.let(entries::add)
                        }
                    }
                }
                eventType = it.next()
            }
        }

        return entries
    }

    private fun parseCategory(context: Context, parser: XmlPullParser): PreferenceLayoutEntry.Category? {
        val attributeSet = Xml.asAttributeSet(parser)
        return context.obtainStyledAttributes(attributeSet, androidx.preference.R.styleable.Preference).use { typedArray ->
            val titleRes = typedArray.getResourceId(androidx.preference.R.styleable.Preference_title, 0)
            if (titleRes == 0) {
                null
            } else {
                PreferenceLayoutEntry.Category(titleRes)
            }
        }
    }

    private fun parseAction(context: Context, parser: XmlPullParser): PreferenceLayoutEntry.Action? {
        val attributeSet = Xml.asAttributeSet(parser)
        return context.obtainStyledAttributes(attributeSet, androidx.preference.R.styleable.Preference).use { typedArray ->
            val key = typedArray.getString(androidx.preference.R.styleable.Preference_key)
            val titleRes = typedArray.getResourceId(androidx.preference.R.styleable.Preference_title, 0)
            val summaryRes = typedArray.getResourceId(androidx.preference.R.styleable.Preference_summary, 0).takeIf { it != 0 }
            val iconRes = typedArray.getResourceId(androidx.preference.R.styleable.Preference_icon, 0)
            val widgetLayoutRes = typedArray.getResourceId(androidx.preference.R.styleable.Preference_widgetLayout, 0).takeIf { it != 0 }

            if (key.isNullOrEmpty() || titleRes == 0 || iconRes == 0) {
                null
            } else {
                PreferenceLayoutEntry.Action(
                    key = key,
                    titleRes = titleRes,
                    summaryRes = summaryRes,
                    iconRes = iconRes,
                    widgetLayoutRes = widgetLayoutRes
                )
            }
        }
    }

    private inline fun <T> android.content.res.XmlResourceParser.use(block: (android.content.res.XmlResourceParser) -> T): T {
        try {
            return block(this)
        } finally {
            close()
        }
    }
}
