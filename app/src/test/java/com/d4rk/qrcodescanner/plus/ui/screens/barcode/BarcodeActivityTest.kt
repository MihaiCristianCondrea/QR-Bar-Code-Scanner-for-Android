package com.d4rk.qrcodescanner.plus.ui.screens.barcode

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeDetailsRepository
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageGenerator
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageSaver
import com.d4rk.qrcodescanner.plus.domain.barcode.WifiConnector
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.SearchEngine
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.utils.extension.toStringId
import com.google.android.gms.ads.AdRequest
import com.google.common.truth.Truth.assertThat
import com.google.zxing.BarcodeFormat
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowPackageManager
import org.robolectric.shadows.ShadowToast

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class BarcodeActivityTest {

    @RelaxedMockK
    private lateinit var barcodeDetailsRepository: BarcodeDetailsRepository

    @RelaxedMockK
    private lateinit var barcodeImageGenerator: BarcodeImageGenerator

    @RelaxedMockK
    private lateinit var barcodeImageSaver: BarcodeImageSaver

    @RelaxedMockK
    private lateinit var settings: Settings

    private val applicationContext: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    private val sampleBarcode = Barcode(
        id = 1L,
        name = "Example",
        text = "https://example.com",
        formattedText = "https://example.com",
        format = BarcodeFormat.QR_CODE,
        schema = BarcodeSchema.URL,
        date = 1_728_000_000_000L,
        isGenerated = false,
        isFavorite = false,
        errorCorrectionLevel = null,
        country = "US"
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        Dispatchers.setMain(UnconfinedTestDispatcher())



        mockkConstructor(AdRequest.Builder::class)
        every { anyConstructed<AdRequest.Builder>().build() } returns mockk(relaxed = true)

        startKoin {
            androidContext(applicationContext)
            modules(
                module {
                    single { barcodeDetailsRepository }
                    single { barcodeImageGenerator }
                    single { barcodeImageSaver }
                    single { settings }
                    single { WifiConnector }
                }
            )
        }

        every { settings.copyToClipboard } returns false
        every { settings.openLinksAutomatically } returns false
        every { settings.barcodeContentColor } returns Color.BLACK
        every { settings.barcodeBackgroundColor } returns Color.WHITE
        every { settings.areBarcodeColorsInversed } returns false
        every { settings.doNotSaveDuplicates } returns false
        every { settings.searchEngine } returns SearchEngine.NONE
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
        clearMocks(settings)
        unmockkAll()
    }

    @Test
    fun `onCreate with valid barcode from intent`() {
        val activity = buildActivityWithIntent(createIntent(sampleBarcode)).setup().get()

        assertThat(activity.isFinishing).isFalse()
        assertThat(activity.title).isEqualTo(activity.getString(sampleBarcode.format.toStringId()))
        assertThat(activity.findViewById<android.widget.TextView>(R.id.text_view_barcode_text).text.toString())
            .isEqualTo(sampleBarcode.formattedText)
    }

    @Test
    fun `onCreate with null barcode from intent`() {
        val intent = createIntent(barcode = null)
        val controller = buildActivityWithIntent(intent)
        val activity = controller.create().get()

        val latestToast = ShadowToast.getTextOfLatestToast()
        assertThat(latestToast).isEqualTo(activity.getString(R.string.barcode_error_missing_extra))
        assertThat(activity.isFinishing).isTrue()
    }

    @Test
    fun `onCreate with restored barcode from savedInstanceState`() {
        val bundle = Bundle().apply {
            putSerializable(BARCODE_KEY, sampleBarcode)
        }
        val intent = createIntent(barcode = null)
        val activity = buildActivityWithIntent(intent).setup(bundle).get()

        assertThat(activity.isFinishing).isFalse()
        assertThat(activity.findViewById<android.widget.TextView>(R.id.text_view_barcode_text).text.toString())
            .isEqualTo(sampleBarcode.formattedText)
    }

    @Test
    fun `applySettings with copyToClipboard enabled`() {
        every { settings.copyToClipboard } returns true

        val activity = buildActivityWithIntent(createIntent(sampleBarcode)).setup().get()
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

        assertThat(clip?.itemCount).isEqualTo(1)
        assertThat(clip?.getItemAt(0)?.text?.toString()).isEqualTo(sampleBarcode.text)
    }

    @Test
    fun `applySettings with openLinksAutomatically disabled`() {
        every { settings.openLinksAutomatically } returns false

        val activity = buildActivityWithIntent(createIntent(sampleBarcode)).setup().get()
        val nextIntent = Shadows.shadowOf(activity).nextStartedActivity

        assertThat(nextIntent).isNull()
    }

    @Test
    fun `applySettings with openLinksAutomatically enabled for URL`() {
        every { settings.openLinksAutomatically } returns true
        every { settings.copyToClipboard } returns false

        val pmShadow = Shadow.extract<ShadowPackageManager>(applicationContext.packageManager)
        val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sampleBarcode.text))
        pmShadow.addResolveInfoForIntent(urlIntent, createResolveInfo())

        val activity = buildActivityWithIntent(createIntent(sampleBarcode)).setup().get()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        Robolectric.flushForegroundThreadScheduler()
        Robolectric.flushBackgroundThreadScheduler()

        val startedIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertThat(startedIntent).isNotNull()
        assertThat(startedIntent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(startedIntent.dataString).isEqualTo(sampleBarcode.text)
    }

    private fun buildActivityWithIntent(intent: Intent) =
        Robolectric.buildActivity(BarcodeActivity::class.java, intent)

    private fun createIntent(barcode: Barcode?, isCreated: Boolean = false): Intent {
        return Intent(applicationContext, BarcodeActivity::class.java).apply {
            putExtra(IS_CREATED, isCreated)
            barcode?.let { putExtra(BARCODE_KEY, it) }
        }
    }

    private fun createResolveInfo(): android.content.pm.ResolveInfo {
        val resolveInfo = android.content.pm.ResolveInfo()
        resolveInfo.activityInfo = android.content.pm.ActivityInfo().apply {
            applicationInfo = android.content.pm.ApplicationInfo().apply {
                packageName = "com.example"
            }
            name = "ExampleActivity"
            packageName = "com.example"
        }
        return resolveInfo
    }

    private companion object {
        const val BARCODE_KEY = "BARCODE_KEY"
        const val IS_CREATED = "IS_CREATED"
    }
}
