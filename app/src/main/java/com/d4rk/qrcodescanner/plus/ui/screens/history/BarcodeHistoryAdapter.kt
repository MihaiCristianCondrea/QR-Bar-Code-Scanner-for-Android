package com.d4rk.qrcodescanner.plus.ui.screens.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementConfig
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementController
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementPlan
import com.d4rk.qrcodescanner.plus.databinding.ItemBarcodeHistoryBinding
import com.d4rk.qrcodescanner.plus.databinding.ItemHistoryNativeAdBinding
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.utils.extension.toImageId
import com.d4rk.qrcodescanner.plus.utils.extension.toStringId
import com.google.android.gms.ads.nativead.NativeAd
import java.text.SimpleDateFormat
import java.util.Locale

class BarcodeHistoryAdapter(private val listener: Listener) :
    PagingDataAdapter<Barcode, RecyclerView.ViewHolder>(DiffUtilCallback) {

    interface Listener {
        fun onBarcodeClicked(barcode: Barcode)
    }

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
    private val adPlacementController = NativeAdPlacementController(
        NativeAdPlacementConfig(maxDensity = 0.25, minSpacing = 5, edgeBuffer = 2)
    )
    private var nativeAds: List<NativeAd> = emptyList()

    /** Current ad plan used to overlay ads into the adapter positions. */
    private var adPlan: NativeAdPlacementPlan = NativeAdPlacementPlan.EMPTY

    init {
        addOnPagesUpdatedListener { recomputeAdPositions() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_AD -> NativeAdViewHolder(
                ItemHistoryNativeAdBinding.inflate(inflater, parent, false)
            )

            else -> BarcodeViewHolder(
                ItemBarcodeHistoryBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NativeAdViewHolder -> {
                val nativeAd = adPlan.adAtAdapterPosition(position)
                if (nativeAd != null) holder.bind(nativeAd)
            }

            is BarcodeViewHolder -> bindBarcode(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAdPosition(position)) TYPE_AD else TYPE_BARCODE
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + adPlan.placements.size
    }

    fun updateNativeAds(ads: List<NativeAd>) {
        nativeAds = ads
        adPlacementController.updateAds(nativeAds)
        recomputeAdPositions(forceAdRecomputation = true)
    }

    private fun bindBarcode(holder: BarcodeViewHolder, adapterPosition: Int) {
        val dataIndex = toDataIndex(adapterPosition)
        if (dataIndex < 0) return
        val barcode = getItem(dataIndex) ?: return
        val isLastItem = dataIndex == (super.getItemCount() - 1).coerceAtLeast(-1)
        holder.show(barcode, isLastItem)
    }

    /** Map adapter position -> data index by skipping ad positions before it. */
    private fun toDataIndex(adapterPosition: Int): Int {
        var offset = 0
        for (adPosition in adPlan.adapterPositions) {
            if (adPosition < adapterPosition) offset++ else break
        }
        return adapterPosition - offset
    }

    private fun isAdPosition(position: Int): Boolean {
        return adPlan.adapterPositions.binarySearch(position) >= 0
    }

    /**
     * Recompute where ads should appear, then dispatch precise insert/remove ops
     * instead of using notifyDataSetChanged().
     */
    private fun recomputeAdPositions(forceAdRecomputation: Boolean = false) {
        val dataCount = snapshot().items.size
        if (!forceAdRecomputation && dataCount == 0 && nativeAds.isEmpty()) return

        val oldAdPlacementPlan = adPlan
        val newAdPlacementPlan =
            if (dataCount <= 0 || nativeAds.isEmpty()) {
                NativeAdPlacementPlan.EMPTY
            } else {
                adPlacementController.beginSession().plan(dataCount)
            }

        if (newAdPlacementPlan == oldAdPlacementPlan) return

        val removedAdPositions = mutableListOf<Int>()
        val insertedAdPositions = mutableListOf<Int>()

        val oldAdAdapterPositions = oldAdPlacementPlan.adapterPositions
        val newAdAdapterPositions = newAdPlacementPlan.adapterPositions
        var oldPositionIndex = 0
        var newPositionIndex = 0
        while (oldPositionIndex < oldAdAdapterPositions.size || newPositionIndex < newAdAdapterPositions.size) {
            val oldPosition =
                if (oldPositionIndex < oldAdAdapterPositions.size) oldAdAdapterPositions[oldPositionIndex] else Int.MAX_VALUE
            val newPosition =
                if (newPositionIndex < newAdAdapterPositions.size) newAdAdapterPositions[newPositionIndex] else Int.MAX_VALUE
            when {
                oldPosition == newPosition -> {
                    oldPositionIndex++; newPositionIndex++
                }

                oldPosition < newPosition -> {
                    removedAdPositions += oldPosition; oldPositionIndex++
                }

                else -> {
                    insertedAdPositions += newPosition; newPositionIndex++
                }
            }
        }

        adPlan = newAdPlacementPlan
        for (pos in removedAdPositions.asReversed()) notifyItemRemoved(pos)
        for (pos in insertedAdPositions) notifyItemInserted(pos)
    }

    inner class BarcodeViewHolder(private val binding: ItemBarcodeHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun show(barcode: Barcode, isLastItem: Boolean) {
            showDate(barcode)
            showFormat(barcode)
            showText(barcode)
            showImage(barcode)
            showIsFavorite(barcode)
            showOrHideDelimiter(isLastItem)
            setClickListener(barcode)
        }

        private fun showDate(barcode: Barcode) {
            binding.textViewDate.text = dateFormatter.format(barcode.date)
        }

        private fun showFormat(barcode: Barcode) {
            binding.textViewFormat.setText(barcode.format.toStringId())
        }

        private fun showText(barcode: Barcode) {
            binding.textViewText.text = barcode.name ?: barcode.formattedText
        }

        private fun showImage(barcode: Barcode) {
            val imageId = barcode.schema.toImageId() ?: barcode.format.toImageId()
            val image = AppCompatResources.getDrawable(itemView.context, imageId)
            binding.imageViewSchema.setImageDrawable(image)
        }

        private fun showIsFavorite(barcode: Barcode) {
            binding.imageViewFavorite.isVisible = barcode.isFavorite
        }

        private fun showOrHideDelimiter(isLastItem: Boolean) {
            binding.delimiter.isInvisible = isLastItem
        }

        private fun setClickListener(barcode: Barcode) {
            itemView.setOnClickListener { listener.onBarcodeClicked(barcode) }
        }
    }

    private class NativeAdViewHolder(
        private val binding: ItemHistoryNativeAdBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(nativeAd: NativeAd) {
            binding.nativeAdView.renderNativeAd(nativeAd)
        }
    }

    private object DiffUtilCallback : DiffUtil.ItemCallback<Barcode>() {
        override fun areItemsTheSame(oldItem: Barcode, newItem: Barcode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Barcode, newItem: Barcode): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val TYPE_BARCODE = 0
        private const val TYPE_AD = 1
    }
}