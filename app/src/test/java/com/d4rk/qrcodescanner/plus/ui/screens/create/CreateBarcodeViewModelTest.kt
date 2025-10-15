package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.content.ContentResolver
import android.net.Uri
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.google.zxing.BarcodeFormat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class CreateBarcodeViewModelTest {

    private val barcodeDatabase = mockk<BarcodeDatabase>()
    private val settings = mockk<Settings>()

    @Before
    fun setUp() {
        mockkStatic("com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabaseKt")
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `saveBarcode thread context verification`() = runTest {
        val dispatcher = TrackingDispatcher()
        val viewModel = CreateBarcodeViewModel(barcodeDatabase, settings, dispatcher)
        val barcode = createBarcode()

        every { settings.saveCreatedBarcodesToHistory } returns false

        viewModel.saveBarcode(barcode)

        assertTrue("Expected the IO dispatcher to be used", dispatcher.dispatched)
    }

    @Test
    fun `readVCard with a valid VCF URI`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = CreateBarcodeViewModel(barcodeDatabase, settings, dispatcher)
        val contentResolver = mockk<ContentResolver>()
        val uri = mockk<Uri>()
        val vcard = "BEGIN:VCARD\nEND:VCARD"

        every { contentResolver.openInputStream(uri) } returns vcard.byteInputStream()

        val result = viewModel.readVCard(contentResolver, uri)

        assertEquals(vcard, result)
    }

    @Test
    fun `readVCard with an invalid or non existent URI`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = CreateBarcodeViewModel(barcodeDatabase, settings, dispatcher)
        val contentResolver = mockk<ContentResolver>()
        val uri = mockk<Uri>()

        every { contentResolver.openInputStream(uri) } returns null

        val result = viewModel.readVCard(contentResolver, uri)

        assertEquals("", result)
    }

    @Test
    fun `readVCard with URI pointing to an empty file`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = CreateBarcodeViewModel(barcodeDatabase, settings, dispatcher)
        val contentResolver = mockk<ContentResolver>()
        val uri = mockk<Uri>()

        every { contentResolver.openInputStream(uri) } returns ByteArray(0).inputStream()

        val result = viewModel.readVCard(contentResolver, uri)

        assertEquals("", result)
    }

    @Test
    fun `readVCard with insufficient read permissions`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = CreateBarcodeViewModel(barcodeDatabase, settings, dispatcher)
        val contentResolver = mockk<ContentResolver>()
        val uri = mockk<Uri>()

        every { contentResolver.openInputStream(uri) } throws SecurityException("no permission")

        assertFailsWith<SecurityException> {
            viewModel.readVCard(contentResolver, uri)
        }
    }

    @Test
    fun `readVCard with non text file content`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = CreateBarcodeViewModel(barcodeDatabase, settings, dispatcher)
        val contentResolver = mockk<ContentResolver>()
        val uri = mockk<Uri>()
        val binaryContent = byteArrayOf(0, 1, 2, 3)

        every { contentResolver.openInputStream(uri) } returns binaryContent.inputStream()

        val result = viewModel.readVCard(contentResolver, uri)

        assertEquals(4, result.length)
    }

    private fun createBarcode(): Barcode {
        return Barcode(
            id = 0,
            name = "Example",
            text = "https://example.com",
            formattedText = "https://example.com",
            format = BarcodeFormat.QR_CODE,
            schema = BarcodeSchema.URL,
            date = 1234L,
            isGenerated = true
        )
    }

    private class TrackingDispatcher : CoroutineDispatcher() {
        @Volatile
        var dispatched: Boolean = false

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            dispatched = true
            block.run()
        }
    }
}
