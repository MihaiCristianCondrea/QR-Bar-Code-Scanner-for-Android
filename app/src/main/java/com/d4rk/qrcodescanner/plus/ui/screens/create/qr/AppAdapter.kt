package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.databinding.ItemAppBinding

class AppAdapter(private val listener: Listener) :
    ListAdapter<ResolveInfo, AppAdapter.ViewHolder>(DIFF) {

    interface Listener {
        fun onAppClicked(packageName: String)
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        val key = "${item.activityInfo?.packageName}:${item.activityInfo?.name}"
        return key.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isLast = position == itemCount - 1
        holder.bind(getItem(position), isLast)
    }

    class ViewHolder(private val binding: ItemAppBinding, private val listener: Listener) :
        RecyclerView.ViewHolder(binding.root) {

        private val pm: PackageManager
            get() = itemView.context.packageManager

        fun bind(app: ResolveInfo, isLast: Boolean) {
            binding.textView.text = app.loadLabel(pm)
            binding.imageView.setImageDrawable(app.loadIcon(pm))
            binding.delimiter.isInvisible = isLast
            itemView.setOnClickListener {
                listener.onAppClicked(app.activityInfo?.packageName.orEmpty())
            }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<ResolveInfo>() {
            override fun areItemsTheSame(oldItem: ResolveInfo, newItem: ResolveInfo): Boolean {
                val oldPackageName = oldItem.activityInfo?.packageName
                val oldActivityName = oldItem.activityInfo?.name
                val newPackageName = newItem.activityInfo?.packageName
                val newActivityName = newItem.activityInfo?.name
                return oldPackageName == newPackageName && oldActivityName == newActivityName
            }

            override fun areContentsTheSame(oldItem: ResolveInfo, newItem: ResolveInfo): Boolean {
                return areItemsTheSame(oldItem, newItem)
            }
        }
    }
}