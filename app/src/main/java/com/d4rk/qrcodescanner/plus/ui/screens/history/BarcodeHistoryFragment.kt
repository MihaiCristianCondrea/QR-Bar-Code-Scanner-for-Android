package com.d4rk.qrcodescanner.plus.ui.screens.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.d4rk.qrcodescanner.plus.databinding.FragmentBarcodeHistoryBinding
import com.d4rk.qrcodescanner.plus.di.barcodeDatabase
import com.d4rk.qrcodescanner.plus.extension.showError
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.DeleteConfirmationDialogFragment
import com.d4rk.qrcodescanner.plus.ui.screens.history.export.ExportHistoryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BarcodeHistoryFragment : Fragment() , DeleteConfirmationDialogFragment.Listener {
    private lateinit var _binding : FragmentBarcodeHistoryBinding
    private val binding get() = _binding
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
    }

    override fun onDeleteConfirmed() {
        clearHistory()
    }

    private fun initTabs() {
        binding.viewPager.adapter = BarcodeHistoryViewPagerAdapter(requireContext() , childFragmentManager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
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