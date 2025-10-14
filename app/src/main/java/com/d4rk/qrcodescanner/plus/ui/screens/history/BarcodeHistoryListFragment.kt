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
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ads.loader.NativeAdPreloader
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementConfig
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementController
import com.d4rk.qrcodescanner.plus.databinding.FragmentBarcodeHistoryListBinding
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeHistoryFilter
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeHistoryRepository
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.BarcodeActivity
import com.d4rk.qrcodescanner.plus.utils.extension.orZero
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class BarcodeHistoryListFragment : Fragment(), BarcodeHistoryAdapter.Listener {
    private lateinit var _binding: FragmentBarcodeHistoryListBinding
    private val binding get() = _binding
    private var hasLoadedEmptyStateAd = false
    private val barcodeHistoryRepository: BarcodeHistoryRepository by inject()
    private val historyFilter: BarcodeHistoryFilter by lazy {
        when (arguments?.getInt(TYPE_KEY).orZero()) {
            TYPE_ALL -> BarcodeHistoryFilter.ALL
            TYPE_FAVORITES -> BarcodeHistoryFilter.FAVORITES
            else -> throw IllegalStateException("Unknown history filter")
        }
    }
    private val viewModel: BarcodeHistoryListViewModel by viewModels {
        BarcodeHistoryListViewModelFactory(barcodeHistoryRepository, historyFilter)
    }

    companion object {
        private const val TYPE_ALL = 0
        private const val TYPE_FAVORITES = 1
        private const val TYPE_KEY = "TYPE_KEY"
        fun newInstanceAll(): BarcodeHistoryListFragment {
            return BarcodeHistoryListFragment().apply {
                arguments = Bundle().apply {
                    putInt(TYPE_KEY, TYPE_ALL)
                }
            }
        }

        fun newInstanceFavorites(): BarcodeHistoryListFragment {
            return BarcodeHistoryListFragment().apply {
                arguments = Bundle().apply {
                    putInt(TYPE_KEY, TYPE_FAVORITES)
                }
            }
        }
    }

    private val scanHistoryAdapter = BarcodeHistoryAdapter(this)
    private val nativeAds = mutableListOf<NativeAd>()
    private val placementEstimator = NativeAdPlacementController(
        NativeAdPlacementConfig(maxDensity = 0.25, minSpacing = 5, edgeBuffer = 2)
    )
    private var isLoadingAds: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarcodeHistoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        observeHistory()
        observeHistoryLoadState()
    }

    override fun onBarcodeClicked(barcode: Barcode) {
        BarcodeActivity.start(requireActivity(), barcode)
    }

    private fun initRecyclerView() {
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scanHistoryAdapter
        }
        scanHistoryAdapter.addOnPagesUpdatedListener { maybePreloadAds() }
    }

    private fun observeHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.history.collectLatest { pagingData ->
                    scanHistoryAdapter.submitData(pagingData)
                    maybePreloadAds()
                }
            }
        }
    }

    private fun observeHistoryLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scanHistoryAdapter.loadStateFlow.collectLatest { loadStates ->
                    val isEmpty =
                        loadStates.refresh is LoadState.NotLoading && scanHistoryAdapter.itemCount == 0
                    binding.recyclerViewHistory.isVisible = !isEmpty
                    binding.emptyHistoryAdView.isVisible = isEmpty
                    if (isEmpty && !hasLoadedEmptyStateAd) {
                        binding.emptyHistoryAdView.loadAd()
                        hasLoadedEmptyStateAd = true
                    }
                    if (!isEmpty) {
                        hasLoadedEmptyStateAd = false
                    }
                }
            }
        }
    }

    private fun maybePreloadAds() {
        val snapshotSize = scanHistoryAdapter.snapshot().items.size
        val expectedAds = placementEstimator.expectedAdCount(snapshotSize)
        if (expectedAds == 0) {
            if (nativeAds.isNotEmpty()) {
                nativeAds.forEach(NativeAd::destroy)
                nativeAds.clear()
                scanHistoryAdapter.updateNativeAds(nativeAds)
            }
            return
        }
        if (expectedAds <= nativeAds.size || isLoadingAds) {
            if (nativeAds.isNotEmpty()) {
                if (expectedAds < nativeAds.size) {
                    val iterator = nativeAds.listIterator(expectedAds)
                    val toRemove = mutableListOf<NativeAd>()
                    while (iterator.hasNext()) {
                        toRemove += iterator.next()
                        iterator.remove()
                    }
                    toRemove.forEach(NativeAd::destroy)
                }
                scanHistoryAdapter.updateNativeAds(nativeAds)
            }
            return
        }

        val missing = expectedAds - nativeAds.size

        isLoadingAds = true
        NativeAdPreloader.preload(
            context = requireContext(),
            adUnitId = getString(R.string.native_ad_support_unit_id),
            adRequest = AdRequest.Builder().build(),
            count = missing,
            onFinished = { ads ->
                if (!isAdded) {
                    ads.forEach(NativeAd::destroy)
                    isLoadingAds = false
                    return@preload
                }
                nativeAds.addAll(ads)
                scanHistoryAdapter.updateNativeAds(nativeAds)
                isLoadingAds = false
            },
            onFailed = { error: LoadAdError ->
                isLoadingAds = false
            }
        )
    }

    override fun onDestroyView() {
        binding.recyclerViewHistory.adapter = null
        nativeAds.forEach(NativeAd::destroy)
        nativeAds.clear()
        super.onDestroyView()
    }
}
