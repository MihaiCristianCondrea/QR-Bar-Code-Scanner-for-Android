package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.d4rk.qrcodescanner.plus.model.Contact
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseCreateBarcodeFragmentTest {

    @Test
    fun `latitude property default value check`() {
        val fragment = object : BaseCreateBarcodeFragment() {}

        assertThat(fragment.latitude).isNull()
    }

    @Test
    fun `longitude property default value check`() {
        val fragment = object : BaseCreateBarcodeFragment() {}

        assertThat(fragment.longitude).isNull()
    }

    @Test
    fun `getBarcodeSchema default implementation`() {
        val fragment = object : BaseCreateBarcodeFragment() {}

        val schema = fragment.getBarcodeSchema()

        assertThat(schema).isInstanceOf(com.d4rk.qrcodescanner.plus.model.schema.Other::class.java)
        assertThat(schema.schema).isEqualTo(BarcodeSchema.OTHER)
        assertThat(schema.toBarcodeText()).isEmpty()
    }

    @Test
    fun `getBarcodeSchema overridden implementation`() {
        val customSchema = object : Schema {
            override val schema: BarcodeSchema = BarcodeSchema.URL
            override fun toFormattedText(): String = "formatted"
            override fun toBarcodeText(): String = "barcode"
        }
        val fragment = object : BaseCreateBarcodeFragment() {
            override fun getBarcodeSchema(): Schema = customSchema
        }

        val schema = fragment.getBarcodeSchema()

        assertThat(schema).isSameInstanceAs(customSchema)
        assertThat(schema.schema).isEqualTo(BarcodeSchema.URL)
        assertThat(schema.toBarcodeText()).isEqualTo("barcode")
    }

    @Test
    fun `showPhone default implementation call`() {
        val fragment = object : BaseCreateBarcodeFragment() {}

        val result = runCatching { fragment.showPhone("1234567890") }

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `showContact default implementation call`() {
        val fragment = object : BaseCreateBarcodeFragment() {}
        val contact = Contact().apply { phone = "123" }

        val result = runCatching { fragment.showContact(contact) }

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `showLocation default implementation call`() {
        val fragment = object : BaseCreateBarcodeFragment() {}

        val result = runCatching { fragment.showLocation(51.0, -0.1) }

        assertThat(result.isSuccess).isTrue()
    }

    private class LifecycleTrackingFragment : BaseCreateBarcodeFragment() {
        var attachedActivity: CreateBarcodeActivity? = null
            private set
        var onCreateCalled: Boolean = false
            private set
        var onCreateViewCalled: Boolean = false
            private set
        var onDetachCalled: Boolean = false
            private set

        override fun onAttach(context: Context) {
            super.onAttach(context)
            attachedActivity = parentActivity
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            onCreateCalled = true
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            onCreateViewCalled = true
            return View(inflater.context)
        }

        override fun onDetach() {
            super.onDetach()
            onDetachCalled = true
        }
    }
}
