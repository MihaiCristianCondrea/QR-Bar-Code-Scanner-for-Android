package com.d4rk.qrcodescanner.plus.ui.create
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateBarcodeBinding
import com.d4rk.qrcodescanner.plus.databinding.ItemPreferenceBinding
import com.d4rk.qrcodescanner.plus.databinding.ItemPreferenceCategoryBinding
import com.d4rk.qrcodescanner.plus.extension.clipboardManager
import com.d4rk.qrcodescanner.plus.extension.orZero
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.ui.create.barcode.CreateBarcodeAllActivity
import com.d4rk.qrcodescanner.plus.ui.create.qr.CreateQrCodeAllActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.zxing.BarcodeFormat
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class CreateBarcodeFragment : Fragment() {

    private var _binding: FragmentCreateBarcodeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PreferenceItemsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateBarcodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MobileAds.initialize(requireContext())
        setupList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.createList.adapter = null
        _binding = null
    }

    private fun setupList() {
        adapter = PreferenceItemsAdapter(::handleActionClicked)
        binding.createList.layoutManager = LinearLayoutManager(requireContext())
        binding.createList.adapter = adapter
        FastScrollerBuilder(binding.createList).useMd2Style().build()
        adapter.submitList(buildItems())
    }

    private fun buildItems(): List<PreferenceListItem> {
        return listOf(
            PreferenceListItem.Category(R.string.qr_code),
            PreferenceListItem.Action(
                action = PreferenceAction.Clipboard,
                titleRes = R.string.content_from_clipboard,
                iconRes = R.drawable.ic_copy
            ),
            PreferenceListItem.Action(
                action = PreferenceAction.Text,
                titleRes = R.string.text,
                iconRes = R.drawable.ic_text
            ),
            PreferenceListItem.Action(
                action = PreferenceAction.Url,
                titleRes = R.string.url,
                iconRes = R.drawable.ic_link
            ),
            PreferenceListItem.Action(
                action = PreferenceAction.Wifi,
                titleRes = R.string.wifi,
                iconRes = R.drawable.ic_wifi
            ),
            PreferenceListItem.Action(
                action = PreferenceAction.Location,
                titleRes = R.string.location,
                iconRes = R.drawable.ic_location
            ),
            PreferenceListItem.Action(
                action = PreferenceAction.Contact,
                titleRes = R.string.contact_v_card,
                iconRes = R.drawable.ic_contact_white
            ),
            PreferenceListItem.Action(
                action = PreferenceAction.MoreQrCodes,
                titleRes = R.string.more_qr_codes,
                iconRes = R.drawable.ic_qr_code_white,
                showOpenInNew = true
            ),
            PreferenceListItem.Category(R.string.barcode),
            PreferenceListItem.Action(
                action = PreferenceAction.AllBarcodes,
                titleRes = R.string.all_barcodes_codes,
                iconRes = R.drawable.ic_barcode,
                showOpenInNew = true
            )
        )
    }

    private fun handleActionClicked(action: PreferenceAction) {
        when (action) {
            PreferenceAction.Clipboard -> CreateBarcodeActivity.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.OTHER,
                getClipboardContent()
            )

            PreferenceAction.Text -> CreateBarcodeActivity.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.OTHER
            )

            PreferenceAction.Url -> CreateBarcodeActivity.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.URL
            )

            PreferenceAction.Wifi -> CreateBarcodeActivity.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.WIFI
            )

            PreferenceAction.Location -> CreateBarcodeActivity.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.GEO
            )

            PreferenceAction.Contact -> CreateBarcodeActivity.start(
                requireActivity(),
                BarcodeFormat.QR_CODE,
                BarcodeSchema.VCARD
            )

            PreferenceAction.MoreQrCodes -> CreateQrCodeAllActivity.start(requireActivity())
            PreferenceAction.AllBarcodes -> CreateBarcodeAllActivity.start(requireActivity())
        }
    }

    private fun getClipboardContent(): String {
        val clip = requireActivity().clipboardManager?.primaryClip ?: return ""
        return when (clip.itemCount.orZero()) {
            0 -> ""
            else -> clip.getItemAt(0).text.toString()
        }
    }

    private class PreferenceItemsAdapter(
        private val onActionClicked: (PreferenceAction) -> Unit
    ) : ListAdapter<PreferenceListItem, RecyclerView.ViewHolder>(PreferenceDiffCallback()) {

        override fun getItemViewType(position: Int): Int {
            return when (getItem(position)) {
                is PreferenceListItem.Category -> TYPE_CATEGORY
                is PreferenceListItem.Action -> TYPE_ACTION
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_CATEGORY -> {
                    val binding = ItemPreferenceCategoryBinding.inflate(inflater, parent, false)
                    CategoryViewHolder(binding)
                }

                else -> {
                    val binding = ItemPreferenceBinding.inflate(inflater, parent, false)
                    ActionViewHolder(binding, onActionClicked)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is CategoryViewHolder -> holder.bind(getItem(position) as PreferenceListItem.Category)
                is ActionViewHolder -> holder.bind(
                    getItem(position) as PreferenceListItem.Action,
                    isFirstItemInSection(position),
                    isLastItemInSection(position)
                )
            }
        }

        private fun isFirstItemInSection(position: Int): Boolean {
            if (position == 0) return true
            return getItemViewType(position - 1) == TYPE_CATEGORY
        }

        private fun isLastItemInSection(position: Int): Boolean {
            if (position == itemCount - 1) return true
            return getItemViewType(position + 1) == TYPE_CATEGORY
        }

        private class CategoryViewHolder(
            private val binding: ItemPreferenceCategoryBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(category: PreferenceListItem.Category) {
                binding.title.setText(category.titleRes)
            }
        }

        private class ActionViewHolder(
            private val binding: ItemPreferenceBinding,
            private val onActionClicked: (PreferenceAction) -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: PreferenceListItem.Action, first: Boolean, last: Boolean) {
                binding.lessonCard.setOnClickListener { onActionClicked(item.action) }
                if (item.iconRes != 0) {
                    binding.icon.setImageResource(item.iconRes)
                    binding.icon.isVisible = true
                } else {
                    binding.icon.isVisible = false
                }
                binding.title.setText(item.titleRes)
                if (item.summaryRes != null) {
                    binding.summary.setText(item.summaryRes)
                    binding.summary.isVisible = true
                } else {
                    binding.summary.isVisible = false
                }
                if (item.showOpenInNew) {
                    if (binding.widgetFrame.childCount == 0) {
                        LayoutInflater.from(binding.root.context)
                            .inflate(R.layout.item_preference_widget_open_in_new, binding.widgetFrame, true)
                    }
                    binding.widgetFrame.isVisible = true
                    binding.widgetFrame.findViewById<MaterialButton>(R.id.open_in_new)?.apply {
                        isEnabled = false
                    }
                } else {
                    binding.widgetFrame.isVisible = false
                }
                applySpacing(binding.lessonCard, last)
                applyCorners(binding.lessonCard, first, last)
            }

            private fun applySpacing(card: MaterialCardView, last: Boolean) {
                val params = card.layoutParams as? ViewGroup.MarginLayoutParams ?: return
                val spacing = card.resources.getDimensionPixelSize(R.dimen.preference_item_spacing)
                val bottomMargin = if (last) 0 else spacing
                if (params.bottomMargin != bottomMargin) {
                    params.bottomMargin = bottomMargin
                    card.layoutParams = params
                }
            }

            private fun applyCorners(card: MaterialCardView, first: Boolean, last: Boolean) {
                val context = card.context
                val dp4 = context.resources.displayMetrics.density * 4f
                val dp24 = context.resources.displayMetrics.density * 24f
                val shapeBuilder: ShapeAppearanceModel.Builder = card.shapeAppearanceModel.toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, if (first) dp24 else dp4)
                    .setTopRightCorner(CornerFamily.ROUNDED, if (first) dp24 else dp4)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, if (last) dp24 else dp4)
                    .setBottomRightCorner(CornerFamily.ROUNDED, if (last) dp24 else dp4)
                card.shapeAppearanceModel = shapeBuilder.build()
            }
        }

        private class PreferenceDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<PreferenceListItem>() {
            override fun areItemsTheSame(oldItem: PreferenceListItem, newItem: PreferenceListItem): Boolean {
                if (oldItem::class != newItem::class) return false
                return when (oldItem) {
                    is PreferenceListItem.Category -> oldItem.titleRes == (newItem as PreferenceListItem.Category).titleRes
                    is PreferenceListItem.Action -> oldItem.action == (newItem as PreferenceListItem.Action).action
                }
            }

            override fun areContentsTheSame(oldItem: PreferenceListItem, newItem: PreferenceListItem): Boolean {
                return oldItem == newItem
            }
        }

        companion object {
            private const val TYPE_CATEGORY = 0
            private const val TYPE_ACTION = 1
        }
    }

    private sealed interface PreferenceListItem {
        data class Category(@StringRes val titleRes: Int) : PreferenceListItem
        data class Action(
            val action: PreferenceAction,
            @StringRes val titleRes: Int,
            @StringRes val summaryRes: Int? = null,
            @DrawableRes val iconRes: Int,
            val showOpenInNew: Boolean = false
        ) : PreferenceListItem
    }

    private enum class PreferenceAction {
        Clipboard,
        Text,
        Url,
        Wifi,
        Location,
        Contact,
        MoreQrCodes,
        AllBarcodes
    }
}
