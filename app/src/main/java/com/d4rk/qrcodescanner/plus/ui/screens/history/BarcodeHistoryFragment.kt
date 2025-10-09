package com.d4rk.qrcodescanner.plus.ui.screens.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.d4rk.qrcodescanner.plus.databinding.FragmentBarcodeHistoryBinding
import com.d4rk.qrcodescanner.plus.di.barcodeDatabase
import com.d4rk.qrcodescanner.plus.utils.extension.showError
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.DeleteConfirmationDialogFragment
import com.d4rk.qrcodescanner.plus.ui.screens.history.export.ExportHistoryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BarcodeHistoryFragment : Fragment() , DeleteConfirmationDialogFragment.Listener {
    private lateinit var _binding : FragmentBarcodeHistoryBinding
    private val binding get() = _binding
    private var hasLoadedEmptyStateAd = false
    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View {
        _binding = FragmentBarcodeHistoryBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        binding.exportHistoryButton.setOnClickListener {
            navigateToExportHistoryScreen()
        }
        initTabs()
        observeHistoryState()
    }

    override fun onDeleteConfirmed() {
        clearHistory()
    }

    private fun initTabs() {
        binding.viewPager.adapter = BarcodeHistoryViewPagerAdapter(requireContext() , childFragmentManager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun observeHistoryState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                barcodeDatabase.observeCount().collectLatest { count ->
                    val isEmpty = count == 0
                    binding.tabLayout.isVisible = ! isEmpty
                    binding.viewPager.isVisible = ! isEmpty
                    binding.exportHistoryButton.isVisible = ! isEmpty
                    binding.emptyHistoryContainer.isVisible = isEmpty
                    binding.emptyHistoryMessage.isVisible = isEmpty
                    binding.emptyHistoryAdView.isVisible = isEmpty
                    if (isEmpty && ! hasLoadedEmptyStateAd) {
                        binding.emptyHistoryAdView.loadAd()
                        hasLoadedEmptyStateAd = true
                    }
                    if (! isEmpty) {
                        hasLoadedEmptyStateAd = false
                    }
                }
            }
        }
    }

    private fun navigateToExportHistoryScreen() {
        ExportHistoryActivity.start(requireActivity())
    }

    private fun clearHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { barcodeDatabase.deleteAll() }
            } catch (e : Exception) {
                showError(e)
            }
        }
    }
}
