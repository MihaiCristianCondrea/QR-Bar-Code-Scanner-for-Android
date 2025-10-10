package com.d4rk.qrcodescanner.plus.ui.screens.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.databinding.FragmentBarcodeHistoryListBinding
import com.d4rk.qrcodescanner.plus.di.barcodeHistoryRepository
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeHistoryFilter
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.BarcodeActivity
import com.d4rk.qrcodescanner.plus.utils.extension.orZero
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BarcodeHistoryListFragment : Fragment() , BarcodeHistoryAdapter.Listener {
    private lateinit var _binding : FragmentBarcodeHistoryListBinding
    private val binding get() = _binding
    private var hasLoadedEmptyStateAd = false
    private val historyRepository by lazy { barcodeHistoryRepository }
    private val historyFilter : BarcodeHistoryFilter by lazy {
        when (arguments?.getInt(TYPE_KEY).orZero()) {
            TYPE_ALL -> BarcodeHistoryFilter.ALL
            TYPE_FAVORITES -> BarcodeHistoryFilter.FAVORITES
            else -> throw IllegalStateException("Unknown history filter")
        }
    }
    private val viewModel : BarcodeHistoryListViewModel by viewModels {
        BarcodeHistoryListViewModelFactory(historyRepository , historyFilter)
    }

    companion object {
        private const val TYPE_ALL = 0
        private const val TYPE_FAVORITES = 1
        private const val TYPE_KEY = "TYPE_KEY"
        fun newInstanceAll() : BarcodeHistoryListFragment {
            return BarcodeHistoryListFragment().apply {
                arguments = Bundle().apply {
                    putInt(TYPE_KEY , TYPE_ALL)
                }
            }
        }

        fun newInstanceFavorites() : BarcodeHistoryListFragment {
            return BarcodeHistoryListFragment().apply {
                arguments = Bundle().apply {
                    putInt(TYPE_KEY , TYPE_FAVORITES)
                }
            }
        }
    }

    private val scanHistoryAdapter = BarcodeHistoryAdapter(this)
    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View {
        _binding = FragmentBarcodeHistoryListBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        initRecyclerView()
        observeHistory()
        observeHistoryLoadState()
    }

    override fun onBarcodeClicked(barcode : Barcode) {
        BarcodeActivity.start(requireActivity() , barcode)
    }

    private fun initRecyclerView() {
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scanHistoryAdapter
        }
    }

    private fun observeHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.history.collectLatest { pagingData ->
                    scanHistoryAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun observeHistoryLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scanHistoryAdapter.loadStateFlow.collectLatest { loadStates ->
                    val isEmpty = loadStates.refresh is LoadState.NotLoading && scanHistoryAdapter.itemCount == 0
                    binding.recyclerViewHistory.isVisible = ! isEmpty
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
}
