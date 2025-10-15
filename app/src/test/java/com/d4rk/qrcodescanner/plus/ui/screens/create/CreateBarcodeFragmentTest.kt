package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementController
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateBarcodeBinding
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListItem
import com.google.common.truth.Truth.assertThat
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CreateBarcodeFragmentTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `onCreateView binding inflation`() {
        val fragment = CreateBarcodeFragment()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inflater = LayoutInflater.from(context)
        val container = FrameLayout(context)

        fragment.onCreateView(inflater, container, null)

        val bindingField = CreateBarcodeFragment::class.java.getDeclaredField("_binding")
        bindingField.isAccessible = true

        val binding = bindingField.get(fragment)

        assertThat(binding).isInstanceOf(FragmentCreateBarcodeBinding::class.java)
    }

    @Test
    fun `onCreateView returns root view`() {
        val fragment = CreateBarcodeFragment()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inflater = LayoutInflater.from(context)
        val container = FrameLayout(context)

        val view = fragment.onCreateView(inflater, container, null)

        val bindingField = CreateBarcodeFragment::class.java.getDeclaredField("_binding")
        bindingField.isAccessible = true
        val binding = bindingField.get(fragment) as FragmentCreateBarcodeBinding

        assertThat(view).isSameInstanceAs(binding.root)
        assertThat(view.findViewById<RecyclerView>(R.id.create_list)).isNotNull()
    }

    @Test
    fun `estimateAdCount with empty list`() {
        val fragment = CreateBarcodeFragment()
        fragment.setBaseItems(emptyList())

        val estimate = fragment.invokeEstimateAdCount()

        assertThat(estimate).isEqualTo(0)
    }

    @Test
    fun `estimateAdCount with items but no categories`() {
        val fragment = CreateBarcodeFragment()
        val actionItems = List(4) { index ->
            PreferenceListItem.Action(
                action = index,
                titleRes = android.R.string.ok,
                iconRes = android.R.drawable.btn_default
            )
        }

        fragment.setBaseItems(actionItems)

        val estimate = fragment.invokeEstimateAdCount()

        assertThat(estimate).isEqualTo(1)
    }

    @Test
    fun `estimateAdCount with categories and items`() {
        val fragment = CreateBarcodeFragment()
        val items: List<PreferenceListItem<*>> = listOf(
            PreferenceListItem.Category(titleRes = android.R.string.ok),
            PreferenceListItem.Action(action = 1, titleRes = android.R.string.ok, iconRes = android.R.drawable.btn_default),
            PreferenceListItem.Action(action = 2, titleRes = android.R.string.ok, iconRes = android.R.drawable.btn_default),
            PreferenceListItem.Action(action = 3, titleRes = android.R.string.ok, iconRes = android.R.drawable.btn_default),
            PreferenceListItem.Category(titleRes = android.R.string.cancel),
            PreferenceListItem.Action(action = 4, titleRes = android.R.string.ok, iconRes = android.R.drawable.btn_default),
            PreferenceListItem.Action(action = 5, titleRes = android.R.string.ok, iconRes = android.R.drawable.btn_default)
        )

        fragment.setBaseItems(items)

        val estimate = fragment.invokeEstimateAdCount()

        assertThat(estimate).isEqualTo(2)
    }

    @Test
    fun `applyNativeAds with no ads available`() {
        val fragment = CreateBarcodeFragment()
        val items: List<PreferenceListItem<*>> = listOf(
            PreferenceListItem.Category(titleRes = android.R.string.ok),
            PreferenceListItem.Action(action = 1, titleRes = android.R.string.ok, iconRes = android.R.drawable.btn_default)
        )
        fragment.setBaseItems(items)

        val controllerField = CreateBarcodeFragment::class.java.getDeclaredField("adPlacementController")
        controllerField.isAccessible = true
        val controller = controllerField.get(fragment) as NativeAdPlacementController
        val session = controller.beginSession()

        val method = CreateBarcodeFragment::class.java.getDeclaredMethod(
            "applyNativeAds",
            NativeAdPlacementController.PlacementSession::class.java
        )
        method.isAccessible = true
        val result = method.invoke(fragment, session) as List<*>

        assertThat(result).containsExactlyElementsIn(items).inOrder()
    }

    private fun CreateBarcodeFragment.setBaseItems(items: List<PreferenceListItem<*>>) {
        val field = CreateBarcodeFragment::class.java.getDeclaredField("baseItems")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        field.set(this, items)
    }

    private fun CreateBarcodeFragment.invokeEstimateAdCount(): Int {
        val method = CreateBarcodeFragment::class.java.getDeclaredMethod("estimateAdCount")
        method.isAccessible = true
        return method.invoke(this) as Int
    }
}
