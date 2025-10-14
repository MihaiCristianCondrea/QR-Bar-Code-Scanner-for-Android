package com.d4rk.qrcodescanner.plus.ui.screens.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.databinding.ItemBarcodeHistoryBinding
import com.d4rk.qrcodescanner.plus.databinding.ItemHistoryNativeAdBinding
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.utils.extension.toImageId
import com.d4rk.qrcodescanner.plus.utils.extension.toStringId
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

class BarcodeHistoryAdapter(private val listener: Listener) :
    PagingDataAdapter<Barcode, RecyclerView.ViewHolder>(DiffUtilCallback) {
    interface Listener {
        fun onBarcodeClicked(barcode: Barcode)
    }

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
    private var adAdapterPositions: List<Int> = emptyList()
    private var lastDataItemCount: Int = -1

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
            is NativeAdViewHolder -> holder.bind()
            is BarcodeViewHolder -> bindBarcode(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAdPosition(position)) TYPE_AD else TYPE_BARCODE
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + adAdapterPositions.size
    }

    private fun bindBarcode(holder: BarcodeViewHolder, adapterPosition: Int) {
        val dataIndex = toDataIndex(adapterPosition)
        if (dataIndex < 0) return
        val barcode = getItem(dataIndex) ?: return
        val isLastItem = dataIndex == super.getItemCount().dec().coerceAtLeast(-1)
        holder.show(barcode, isLastItem)
    }

    private fun toDataIndex(adapterPosition: Int): Int {
        var offset = 0
        for (adPosition in adAdapterPositions) {
            if (adPosition < adapterPosition) {
                offset++
            } else {
                break
            }
        }
        return adapterPosition - offset
    }

    private fun isAdPosition(position: Int): Boolean {
        return adAdapterPositions.binarySearch(position) >= 0
    }

    private fun recomputeAdPositions() {
        val dataCount = snapshot().items.size
        if (dataCount == lastDataItemCount) return
        lastDataItemCount = dataCount

        val sourcePositions = calculateAdPositions(dataCount)
        adAdapterPositions = calculateAdapterPositions(sourcePositions)
        notifyDataSetChanged()
    }

    private fun calculateAdPositions(size: Int, random: Random = Random.Default): List<Int> {
        if (size < MIN_ITEMS_FOR_AD) return emptyList()

        val candidateIndices = (1 until size - 1).toMutableList()
        if (candidateIndices.isEmpty()) return emptyList()

        val positions = mutableListOf<Int>()
        val firstAd = candidateIndices.random(random)
        positions += firstAd

        if (size > MIN_ITEMS_FOR_SECOND_AD) {
            candidateIndices.removeAll { index ->
                abs(index - firstAd) < MIN_DISTANCE_BETWEEN_ADS
            }

            if (candidateIndices.isNotEmpty()) {
                positions += candidateIndices.random(random)
            }
        }

        return positions.sorted()
    }

    private fun calculateAdapterPositions(sourcePositions: List<Int>): List<Int> {
        var offset = 0
        return sourcePositions.map { index ->
            val adapterPosition = index + offset
            offset++
            adapterPosition
        }
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
            itemView.setOnClickListener {
                listener.onBarcodeClicked(barcode)
            }
        }
    }

    private class NativeAdViewHolder(
        private val binding: ItemHistoryNativeAdBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.nativeAdView.loadAd()
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
        private const val MIN_ITEMS_FOR_AD = 3
        private const val MIN_ITEMS_FOR_SECOND_AD = 10
        private const val MIN_DISTANCE_BETWEEN_ADS = 4
    }
}
