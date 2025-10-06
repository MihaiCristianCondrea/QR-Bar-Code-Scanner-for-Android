package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.databinding.ItemAppBinding

class AppAdapter(private val listener : Listener) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {
    private lateinit var binding : ItemAppBinding

    interface Listener {
        fun onAppClicked(packageName : String)
    }

    var apps : List<ResolveInfo> = emptyList()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() : Int {
        return apps.size
    }

    override fun onCreateViewHolder(parent : ViewGroup , viewType : Int) : ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemAppBinding.inflate(inflater , parent , false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder , position : Int) {
        val app = apps[position]
        val isLastPosition = position == apps.lastIndex
        holder.show(app , isLastPosition)
    }

    inner class ViewHolder(private val binding : ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        private val packageManager : PackageManager
            get() = itemView.context.applicationContext.packageManager

        fun show(app : ResolveInfo , isLastPosition : Boolean) {
            showName(app)
            showIcon(app)
            showDelimiter(isLastPosition)
            handleItemClicked(app)
        }

        private fun showName(app : ResolveInfo) {
            binding.textView.text = app.loadLabel(packageManager)
        }

        private fun showIcon(app : ResolveInfo) {
            binding.imageView.setImageDrawable(app.loadIcon(packageManager))
        }

        private fun showDelimiter(isLastPosition : Boolean) {
            binding.delimiter.isInvisible = isLastPosition
        }

        private fun handleItemClicked(app : ResolveInfo) {
            itemView.setOnClickListener {
                listener.onAppClicked(app.activityInfo?.packageName.orEmpty())
            }
        }
    }
}