package com.d4rk.qrcodescanner.plus.domain.history

import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import com.d4rk.qrcodescanner.plus.model.ExportBarcode
import com.google.common.truth.Truth.assertThat
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class BarcodeSaverTest {
    private lateinit var context: Context
    private lateinit var provider: RecordingDownloadsProvider

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        provider = RecordingDownloadsProvider()
        val providerInfo = ProviderInfo().apply { authority = MediaStore.AUTHORITY }
        provider.attachInfo(context, providerInfo)
        ShadowContentResolver.registerProviderInternal(MediaStore.AUTHORITY, provider)
    }

    @After
    fun tearDown() {
        provider.cleanUp()
        ShadowContentResolver.reset()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveBarcodeHistoryAsJson_usesJsonMimeTypeOnApi29() = runTest {
        val barcode = ExportBarcode(
            date = 1_728_000_000_000L,
            format = BarcodeFormat.QR_CODE,
            text = "hello json"
        )

        BarcodeSaver.saveBarcodeHistoryAsJson(context, "history", listOf(barcode))

        val values = provider.lastInsertedValues
        assertThat(values).isNotNull()
        assertThat(values!!.getAsString(MediaStore.Downloads.MIME_TYPE)).isEqualTo("application/json")
        val written = provider.lastWrittenFile?.readText()
        assertThat(written).isNotNull()
        assertThat(written).contains("hello json")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveBarcodeHistoryAsCsv_usesCsvMimeTypeOnApi29() = runTest {
        val barcode = ExportBarcode(
            date = 1_728_000_000_000L,
            format = BarcodeFormat.EAN_13,
            text = "hello csv"
        )

        BarcodeSaver.saveBarcodeHistoryAsCsv(context, "history", listOf(barcode))

        val values = provider.lastInsertedValues
        assertThat(values).isNotNull()
        assertThat(values!!.getAsString(MediaStore.Downloads.MIME_TYPE)).isEqualTo("text/csv")
        val written = provider.lastWrittenFile?.readText()
        assertThat(written).isNotNull()
        assertThat(written).contains("hello csv")
    }

    private class RecordingDownloadsProvider : android.content.ContentProvider() {
        private data class DownloadRecord(
            val values: ContentValues,
            val file: File
        )

        private val records = mutableMapOf<Uri, DownloadRecord>()
        var lastInsertedValues: ContentValues? = null
            private set
        var lastWrittenFile: File? = null
            private set

        override fun onCreate(): Boolean = true

        override fun insert(uri: Uri, values: ContentValues?): Uri? {
            val context = context ?: return null
            val storedValues = if (values != null) ContentValues(values) else ContentValues()
            val displayName = storedValues.getAsString(MediaStore.Downloads.DISPLAY_NAME)
                ?: "export-${UUID.randomUUID()}"
            val file = File(context.cacheDir, displayName).apply {
                parentFile?.mkdirs()
                if (exists()) {
                    delete()
                }
                createNewFile()
            }
            val resultUri = Uri.parse("content://${MediaStore.AUTHORITY}/downloads/${UUID.randomUUID()}")
            records[resultUri] = DownloadRecord(storedValues, file)
            lastInsertedValues = storedValues
            lastWrittenFile = file
            return resultUri
        }

        @Throws(FileNotFoundException::class)
        override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
            val record = records[uri] ?: throw FileNotFoundException("Unknown URI: $uri")
            val accessMode = ParcelFileDescriptor.MODE_CREATE or
                ParcelFileDescriptor.MODE_WRITE_ONLY or
                ParcelFileDescriptor.MODE_TRUNCATE
            return ParcelFileDescriptor.open(record.file, accessMode)
        }

        override fun getType(uri: Uri): String? {
            return records[uri]?.values?.getAsString(MediaStore.Downloads.MIME_TYPE)
        }

        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?
        ) = throw UnsupportedOperationException("Not implemented")

        override fun delete(
            uri: Uri,
            selection: String?,
            selectionArgs: Array<out String>?
        ): Int {
            records.remove(uri)?.file?.delete()
            return 0
        }

        override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?
        ): Int = 0

        fun cleanUp() {
            records.values.forEach { it.file.delete() }
            records.clear()
            lastInsertedValues = null
            lastWrittenFile = null
        }
    }
}
