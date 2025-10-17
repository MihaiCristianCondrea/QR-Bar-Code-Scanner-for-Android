package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.content.Context
import androidx.annotation.XmlRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ads.loader.NativeAdPreloader
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementConfig
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementController
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutEntry
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutParser
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListAdapter
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListItem
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class CreateAllPreferenceListController<Action : Enum<Action>>(
    private val context: Context,
    private val recyclerView: RecyclerView,
    @param:XmlRes private val preferencesXmlRes: Int,
    private val mapActionEntry: (PreferenceLayoutEntry.Action) -> PreferenceListItem.Action<Action>?,
    onActionSelected: (Action) -> Unit,
) {
    private val adapter = PreferenceListAdapter(onActionSelected)
    private val nativeAds = mutableListOf<NativeAd>()
    private val adPlacementController = NativeAdPlacementController(
        NativeAdPlacementConfig(maxDensity = 0.3, minSpacing = 4, edgeBuffer = 1)
    )
    private var baseItems: List<PreferenceListItem<Action>> = emptyList()

    fun initialize() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        FastScrollerBuilder(recyclerView).useMd2Style().build()

        baseItems = buildItems()
        adapter.submitList(baseItems)
        preloadAdsIfNeeded()
    }

    fun destroy() {
        nativeAds.forEach(NativeAd::destroy)
        nativeAds.clear()
    }

    private fun buildItems(): List<PreferenceListItem<Action>> {
        val entries = PreferenceLayoutParser.parse(context, preferencesXmlRes)
        return entries.mapNotNull { entry ->
            when (entry) {
                is PreferenceLayoutEntry.Category -> PreferenceListItem.Category(entry.titleRes)
                is PreferenceLayoutEntry.Action -> mapActionEntry(entry)
            }
        }
    }

    private fun preloadAdsIfNeeded() {
        val requestedAds = estimateAdCount()
        if (requestedAds <= 0) {
            return
        }

        val adUnitId = context.getString(R.string.native_ad_support_unit_id)
        NativeAdPreloader.preload(
            context = context,
            adUnitId = adUnitId,
            adRequest = AdRequest.Builder().build(),
            count = requestedAds,
            onFinished = { ads ->
                if (ads.isEmpty()) {
                    return@preload
                }
                nativeAds.forEach(NativeAd::destroy)
                nativeAds.clear()
                nativeAds.addAll(ads)
                adPlacementController.updateAds(nativeAds)
                val session = adPlacementController.beginSession()
                adapter.submitList(applyNativeAds(session))
            },
        )
    }

    private fun estimateAdCount(): Int {
        if (baseItems.isEmpty()) return 0
        var count = 0
        var sectionSize = 0
        baseItems.forEach { item ->
            if (item is PreferenceListItem.Category) {
                if (sectionSize > 0) {
                    count += adPlacementController.expectedAdCount(sectionSize)
                    sectionSize = 0
                }
            } else {
                sectionSize++
            }
        }
        if (sectionSize > 0) {
            count += adPlacementController.expectedAdCount(sectionSize)
        }
        return count
    }

    private fun applyNativeAds(
        session: NativeAdPlacementController.PlacementSession,
    ): List<PreferenceListItem<Action>> {
        if (baseItems.isEmpty()) return baseItems
        val result = mutableListOf<PreferenceListItem<Action>>()
        val sectionItems = mutableListOf<PreferenceListItem<Action>>()

        fun flushSection() {
            if (sectionItems.isEmpty()) return
            val decorated = session.decorate(sectionItems) { ad ->
                PreferenceListItem.NativeAd(nativeAd = ad)
            }
            result += decorated
            sectionItems.clear()
        }

        baseItems.forEach { item ->
            if (item is PreferenceListItem.Category) {
                flushSection()
                result += item
            } else {
                sectionItems += item
            }
        }

        flushSection()
        return result
    }
}
