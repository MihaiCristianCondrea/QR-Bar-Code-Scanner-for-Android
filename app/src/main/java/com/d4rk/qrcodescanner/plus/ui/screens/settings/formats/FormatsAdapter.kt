package com.d4rk.qrcodescanner.plus.ui.screens.settings.formats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.databinding.ItemBarcodeFormatBinding
import com.d4rk.qrcodescanner.plus.utils.extension.toStringId
import com.google.zxing.BarcodeFormat

class FormatsAdapter(private val listener : Listener , private val formats : List<BarcodeFormat> , private val formatSelection : List<Boolean>) : RecyclerView.Adapter<FormatsAdapter.ViewHolder>() {
    interface Listener {
        fun onFormatChecked(format : BarcodeFormat , isChecked : Boolean)
    }

    override fun getItemCount() : Int {
        return formats.size
    }

    private lateinit var binding : ItemBarcodeFormatBinding
    override fun onCreateViewHolder(parent : ViewGroup , viewType : Int) : ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemBarcodeFormatBinding.inflate(inflater , parent , false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder , position : Int) {
        holder.show(position)
    }

    inner class ViewHolder(private val binding : ItemBarcodeFormatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun show(position : Int) {
            val format = formats[position]
            binding.textViewText.text = itemView.context.resources.getString(format.toStringId())
            binding.delimiter.isInvisible = position == formats.lastIndex
            binding.checkBox.isChecked = formatSelection[position]
            binding.checkBox.setOnCheckedChangeListener { _ , isChecked ->
                listener.onFormatChecked(format , isChecked)
            }
            itemView.setOnClickListener {
                binding.checkBox.toggle()
            }
        }
    }
}