package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodeAppBinding
import com.d4rk.qrcodescanner.plus.extension.showError
import com.d4rk.qrcodescanner.plus.extension.unsafeLazy
import com.d4rk.qrcodescanner.plus.model.schema.App
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateQrCodeAppFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding : FragmentCreateQrCodeAppBinding
    private val appAdapter by unsafeLazy { AppAdapter(parentActivity) }
    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View {
        binding = FragmentCreateQrCodeAppBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        initRecyclerView()
        loadApps()
    }

    override fun getBarcodeSchema() : Schema {
        return App.fromPackage("")
    }

    private fun initRecyclerView() {
        binding.recyclerViewApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appAdapter
        }
    }

    private fun loadApps() {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apps = withContext(Dispatchers.IO) { getApps() }
                showLoading(false)
                showApps(apps)
            } catch (error : Exception) {
                showLoading(false)
                showError(error)
            }
        }
    }

    private fun getApps() : List<ResolveInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return requireContext().packageManager.queryIntentActivities(mainIntent , 0).filter { it.activityInfo?.packageName != null }
    }

    private fun showLoading(isLoading : Boolean) {
        binding.progressBarLoading.isVisible = isLoading
        binding.recyclerViewApps.isVisible = isLoading.not()
    }

    private fun showApps(apps : List<ResolveInfo>) {
        appAdapter.apps = apps
    }
}