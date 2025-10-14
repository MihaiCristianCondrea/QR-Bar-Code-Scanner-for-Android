package com.d4rk.qrcodescanner.plus.ads.placement

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.android.gms.ads.nativead.NativeAd
import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random

/**
 * Centralized helper that calculates native ad placements for any paged or static list.
 *
 * The controller keeps a pool of preloaded [NativeAd] objects provided by the host screen and
 * exposes a session based API to decorate lists or generate adapter positions without duplicating
 * placement logic.
 *
 * Placement rules enforced by default:
 * - Ads are never inserted at the first or last adapter position.
 * - Ads respect a minimum spacing defined in [NativeAdPlacementConfig.minSpacing].
 * - For small lists (two or four content items) we still place at most one ad following the
 *   breakpoint rule specified in the requirements.
 * - For long lists we cap the amount of ads using [NativeAdPlacementConfig.maxDensity].
 * - An optional random seed allows deterministic placements in tests.
 */
class NativeAdPlacementController(
    private val config: NativeAdPlacementConfig,
    private val logger: NativeAdPlacementLogger = NativeAdPlacementLogger()
) {

    private var ads: List<NativeAd> = emptyList()

    /**
     * Updates the pool of preloaded ads available for placement.
     */
    fun updateAds(preloadedAds: List<NativeAd>) {
        ads = preloadedAds
    }

    /**
     * Estimates how many ads would be displayed for [contentCount] items using the current config.
     * This is useful before requesting ads from the network.
     */
    fun expectedAdCount(contentCount: Int): Int {
        val calculator = NativeAdPlacementCalculator(config, Random.Default)
        return calculator.determineDesiredAdCount(contentCount, Int.MAX_VALUE)
    }

    /**
     * Creates a new placement session that consumes ads sequentially from the current pool.
     */
    fun beginSession(randomSeedOverride: Long? = null): PlacementSession {
        val seed = randomSeedOverride ?: config.randomSeed
        val random = seed?.let(::Random) ?: Random.Default
        return PlacementSession(ads, config, logger, random)
    }

    class PlacementSession internal constructor(
        private val availableAds: List<NativeAd>,
        private val config: NativeAdPlacementConfig,
        private val logger: NativeAdPlacementLogger,
        private val random: Random
    ) {

        private var nextAdIndex = 0

        /**
         * Generates placements for a paged adapter based on [contentCount].
         */
        fun plan(contentCount: Int): NativeAdPlacementPlan {
            val remainingAds = availableAds.size - nextAdIndex
            if (remainingAds <= 0) {
                return NativeAdPlacementPlan.EMPTY
            }

            val calculator = NativeAdPlacementCalculator(config, random)
            val positions = calculator.calculatePositions(contentCount, remainingAds)
            if (positions.isEmpty()) {
                logger.log(contentCount, 0, emptyList())
                return NativeAdPlacementPlan.EMPTY
            }

            val placements = positions.mapIndexed { index, sourceIndex ->
                val ad = availableAds[nextAdIndex + index]
                NativeAdPlacement(sourceIndex, sourceIndex + index, ad)
            }
            nextAdIndex += placements.size
            logger.log(contentCount, placements.size, positions)
            return NativeAdPlacementPlan(placements)
        }

        /**
         * Decorates the provided [items] list by injecting ads produced through [adFactory].
         */
        fun <T> decorate(items: List<T>, adFactory: (NativeAd) -> T): List<T> {
            if (items.isEmpty()) return items
            val plan = plan(items.size)
            if (plan.placements.isEmpty()) return items

            val iterator = plan.placements.iterator()
            var nextPlacement = if (iterator.hasNext()) iterator.next() else null

            val result = ArrayList<T>(items.size + plan.placements.size)
            items.forEachIndexed { index, item ->
                while (nextPlacement?.sourceIndex == index) {
                    result += adFactory(nextPlacement.nativeAd)
                    nextPlacement = if (iterator.hasNext()) iterator.next() else null
                }
                result += item
            }
            return result
        }
    }
}

/**
 * Immutable view of planned native ad placements.
 */
@ConsistentCopyVisibility
data class NativeAdPlacementPlan internal constructor(
    val placements: List<NativeAdPlacement>
) {
    val adapterPositions: List<Int> = placements.map { it.adapterIndex }

    fun adAtAdapterPosition(position: Int): NativeAd? {
        return placements.firstOrNull { it.adapterIndex == position }?.nativeAd
    }

    companion object {
        val EMPTY = NativeAdPlacementPlan(emptyList())
    }
}

data class NativeAdPlacement(
    val sourceIndex: Int,
    val adapterIndex: Int,
    val nativeAd: NativeAd
)

/**
 * Configuration shared across the app for native ad placements.
 */
data class NativeAdPlacementConfig(
    val maxDensity: Double = 0.25,
    val minSpacing: Int = 4,
    val edgeBuffer: Int = 1,
    val randomSeed: Long? = null
)

class NativeAdPlacementLogger {
    fun log(contentCount: Int, adCount: Int, positions: List<Int>) {
        val density = if (contentCount == 0) 0.0 else adCount.toDouble() / contentCount
        Log.d(
            TAG,
            "native-ad-placement: items=$contentCount ads=$adCount density=${"%.2f".format(density)} positions=$positions"
        )
    }

    companion object {
        private const val TAG = "NativeAdPlacement"
    }
}

internal class NativeAdPlacementCalculator(
    private val config: NativeAdPlacementConfig,
    private val random: Random
) {

    fun calculatePositions(contentCount: Int, availableAds: Int): List<Int> {
        val desiredAds = determineDesiredAdCount(contentCount, availableAds)
        if (desiredAds <= 0) return emptyList()

        val candidateIndices = buildCandidateIndices(contentCount).toMutableList()
        if (candidateIndices.isEmpty()) return emptyList()

        val positions = mutableListOf<Int>()
        while (positions.size < desiredAds && candidateIndices.isNotEmpty()) {
            val index = candidateIndices.random(random)
            positions += index
            candidateIndices.removeAll { candidate ->
                abs(candidate - index) < config.minSpacing
            }
        }

        return positions.sorted()
    }

    fun determineDesiredAdCount(contentCount: Int, availableAds: Int): Int {
        if (contentCount < 2 || availableAds <= 0) return 0
        if (contentCount == 2 || contentCount == 4) {
            return minOf(1, availableAds)
        }

        val densityCap = floor(contentCount * config.maxDensity).toInt()
        val bySpacing = computeMaxPlacementsForSpacing(contentCount)
        val desired = maxOf(1, minOf(densityCap, bySpacing))
        return minOf(desired, availableAds)
    }

    private fun computeMaxPlacementsForSpacing(contentCount: Int): Int {
        if (config.minSpacing <= 1) return contentCount - 1
        var placements = 0
        var nextAllowedIndex = config.edgeBuffer
        while (nextAllowedIndex < contentCount - config.edgeBuffer) {
            placements++
            nextAllowedIndex += config.minSpacing
        }
        return placements
    }

    @VisibleForTesting
    internal fun buildCandidateIndices(contentCount: Int): List<Int> {
        if (contentCount <= 1) return emptyList()
        val start = config.edgeBuffer.coerceAtLeast(1)
        val endExclusive = (contentCount - config.edgeBuffer).coerceAtLeast(start)
        val indices = (start until endExclusive).toList()
        if (indices.isNotEmpty()) return indices
        return (1 until contentCount).toList()
    }
}
