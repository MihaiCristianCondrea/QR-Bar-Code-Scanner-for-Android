package com.d4rk.qrcodescanner.plus.ui.screens.barcode

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageGenerator
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.utils.extension.toStringId
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.google.common.truth.Truth.assertThat
import com.google.zxing.BarcodeFormat
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import java.io.Serializable
import java.lang.reflect.Modifier
import java.text.SimpleDateFormat
import java.util.Locale
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.Robolectric.buildActivity
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.LooperMode.Mode

@LooperMode(Mode.PAUSED)
class BarcodeImageActivityTest {

    private val application: Application = ApplicationProvider.getApplicationContext()
    private lateinit var settings: Settings
    private val defaultBarcode = Barcode(
        id = 1L,
        text = "Hello World",
        formattedText = "Hello World",
        format = BarcodeFormat.QR_CODE,
        schema = BarcodeSchema.URL,
        date = 1_690_000_000_000L,
        isGenerated = true
    )

    @Before
    fun setUp() {
        mockkObject(BarcodeImageGenerator)
        settings = mockk(relaxed = true)
        every { settings.barcodeBackgroundColor } returns Color.WHITE
        every { settings.barcodeContentColor } returns Color.BLACK
        every { settings.areBarcodeColorsInversed } returns false
        startKoin {
            androidContext(application)
            modules(
                module {
                    single { settings }
                    single { BarcodeImageGenerator }
                }
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        unmockkAll()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    @Test
    fun `onCreate  Activity starts with a valid barcode`() {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns bitmap

        val controller = launchController()
        val activity = controller.setup().get()
        idleCoroutines()

        val imageView = activity.findViewById<ImageView>(R.id.image_view_barcode)
        val dateView = activity.findViewById<TextView>(R.id.text_view_date)
        val textView = activity.findViewById<TextView>(R.id.text_view_barcode_text)

        assertThat(imageView.drawable).isNotNull()
        assertThat(imageView.isVisible).isTrue()

        val expectedDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH).format(defaultBarcode.date)
        assertThat(dateView.text.toString()).isEqualTo(expectedDate)
        assertThat(textView.text.toString()).isEqualTo(defaultBarcode.text)
        assertThat(activity.title).isEqualTo(activity.getString(defaultBarcode.format.toStringId()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `onCreate  Activity starts without a barcode`() {
        val controller = launchController(includeExtra = false)
        controller.setup()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `onCreate  Activity starts with a null barcode`() {
        val controller = launchController(barcodeExtra = null)
        controller.setup()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `onCreate  Activity starts with an invalid barcode type`() {
        val controller = launchController(barcodeExtra = "invalid type")
        controller.setup()
    }

    @Test
    fun `onCreate  Activity recreation with saved instance state`() {
        val bitmap = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888)
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns bitmap

        val controller = launchController()
        controller.setup()
        idleCoroutines()

        controller.recreate()
        val recreated = controller.get()
        idleCoroutines()

        val imageView = recreated.findViewById<ImageView>(R.id.image_view_barcode)
        assertThat(imageView.drawable).isNotNull()
        assertThat(recreated.title).isEqualTo(recreated.getString(R.string.qr_code))
    }

    @Test
    fun `onCreate  EdgeToEdge is applied correctly`() {
        mockkObject(EdgeToEdgeHelper)
        justRun { EdgeToEdgeHelper.applyEdgeToEdge(any(), any()) }
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns null

        val controller = launchController()
        controller.setup()

        verify { EdgeToEdgeHelper.applyEdgeToEdge(any(), any()) }
    }

    @Test
    fun `optionsMenu  Menu creation and initial state`() {
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns null

        val activity = launchController().setup().get()
        val shadowActivity = Shadows.shadowOf(activity)
        val menu = shadowActivity.optionsMenu

        assertThat(menu).isNotNull()
        val increase = menu!!.findItem(R.id.item_increase_brightness)
        val decrease = menu.findItem(R.id.item_decrease_brightness)
        assertThat(increase.isVisible).isTrue()
        assertThat(decrease.isVisible).isFalse()
    }

    @Test
    fun `optionsMenu  Increase brightness functionality`() {
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns null

        val initialBrightness = 0.2f
        val controller = launchController(initialBrightness = initialBrightness)
        val activity = controller.setup().get()
        val menu = Shadows.shadowOf(activity).optionsMenu!!

        activity.onOptionsItemSelected(menu.findItem(R.id.item_increase_brightness))

        assertThat(activity.window.attributes.screenBrightness).isEqualTo(1.0f)
        assertThat(menu.findItem(R.id.item_increase_brightness).isVisible).isFalse()
        assertThat(menu.findItem(R.id.item_decrease_brightness).isVisible).isTrue()
    }

    @Test
    fun `optionsMenu  Decrease brightness functionality`() {
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns null

        val initialBrightness = 0.45f
        val controller = launchController(initialBrightness = initialBrightness)
        val activity = controller.setup().get()
        val menu = Shadows.shadowOf(activity).optionsMenu!!

        activity.onOptionsItemSelected(menu.findItem(R.id.item_increase_brightness))
        activity.onOptionsItemSelected(menu.findItem(R.id.item_decrease_brightness))

        assertThat(activity.window.attributes.screenBrightness).isEqualTo(initialBrightness)
        assertThat(menu.findItem(R.id.item_increase_brightness).isVisible).isTrue()
        assertThat(menu.findItem(R.id.item_decrease_brightness).isVisible).isFalse()
    }

    @Test
    fun `optionsMenu  Toggling brightness multiple times`() {
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns null

        val initialBrightness = 0.33f
        val controller = launchController(initialBrightness = initialBrightness)
        val activity = controller.setup().get()
        val menu = Shadows.shadowOf(activity).optionsMenu!!
        val increase = menu.findItem(R.id.item_increase_brightness)
        val decrease = menu.findItem(R.id.item_decrease_brightness)

        repeat(3) {
            activity.onOptionsItemSelected(increase)
            assertThat(activity.window.attributes.screenBrightness).isEqualTo(1.0f)
            assertThat(increase.isVisible).isFalse()
            assertThat(decrease.isVisible).isTrue()

            activity.onOptionsItemSelected(decrease)
            assertThat(activity.window.attributes.screenBrightness).isEqualTo(initialBrightness)
            assertThat(increase.isVisible).isTrue()
            assertThat(decrease.isVisible).isFalse()
        }
    }

    @Test
    fun `optionsMenu  State persistence on recreation`() {
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns null

        val controller = launchController(initialBrightness = 0.25f)
        var activity = controller.setup().get()
        var menu = Shadows.shadowOf(activity).optionsMenu!!

        activity.onOptionsItemSelected(menu.findItem(R.id.item_increase_brightness))
        controller.recreate()

        activity = controller.get()
        idleCoroutines()
        menu = Shadows.shadowOf(activity).optionsMenu!!

        assertThat(activity.window.attributes.screenBrightness).isEqualTo(1.0f)
        assertThat(menu.findItem(R.id.item_increase_brightness).isVisible).isTrue()
        assertThat(menu.findItem(R.id.item_decrease_brightness).isVisible).isFalse()
    }

    @Test
    fun `showBarcodeImage  Successful image generation`() {
        val bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns bitmap

        val activity = launchController().setup().get()
        idleCoroutines()

        verify {
            BarcodeImageGenerator.generateBitmap(
                defaultBarcode,
                2000,
                2000,
                0,
                settings.barcodeContentColor,
                settings.barcodeBackgroundColor
            )
        }

        val imageView = activity.findViewById<ImageView>(R.id.image_view_barcode)
        assertThat(imageView.isVisible).isTrue()
        assertThat(imageView.drawable).isNotNull()
    }

    @Test
    fun `showBarcodeImage  Null bitmap from generator`() {
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns null

        val activity = launchController().setup().get()
        idleCoroutines()

        val imageView = activity.findViewById<ImageView>(R.id.image_view_barcode)
        assertThat(imageView.isVisible).isFalse()
    }

    @Test
    fun `showBarcodeImage  Exception during image generation`() {
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } throws IllegalStateException()

        val activity = launchController().setup().get()
        idleCoroutines()

        val imageView = activity.findViewById<ImageView>(R.id.image_view_barcode)
        assertThat(imageView.isVisible).isFalse()
    }

    @Test
    fun `showBarcodeImage  Correct background and padding settings  Light Mode `() {
        val bitmap = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888)
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns bitmap
        every { settings.barcodeBackgroundColor } returns Color.GREEN
        every { settings.areBarcodeColorsInversed } returns false
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val activity = launchController().setup().get()
        idleCoroutines()

        val imageView = activity.findViewById<ImageView>(R.id.image_view_barcode)
        val backgroundLayout = activity.findViewById<FrameLayout>(R.id.layout_barcode_image_background)

        val imageBackgroundColor = (imageView.background as ColorDrawable).color
        val layoutBackgroundColor = (backgroundLayout.background as ColorDrawable).color
        assertThat(imageBackgroundColor).isEqualTo(Color.GREEN)
        assertThat(layoutBackgroundColor).isEqualTo(Color.GREEN)
        assertThat(backgroundLayout.paddingLeft).isEqualTo(0)
        assertThat(backgroundLayout.paddingTop).isEqualTo(0)
        assertThat(backgroundLayout.paddingRight).isEqualTo(0)
        assertThat(backgroundLayout.paddingBottom).isEqualTo(0)
    }

    @Test
    fun `showBarcodeImage  Correct background and padding settings  Dark Mode `() {
        val bitmap = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888)
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns bitmap
        every { settings.barcodeBackgroundColor } returns Color.MAGENTA
        every { settings.areBarcodeColorsInversed } returns false
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val activity = launchController().setup().get()
        idleCoroutines()

        val backgroundLayout = activity.findViewById<FrameLayout>(R.id.layout_barcode_image_background)

        val layoutBackgroundColor = (backgroundLayout.background as ColorDrawable).color
        assertThat(layoutBackgroundColor).isEqualTo(Color.MAGENTA)
        assertThat(backgroundLayout.paddingLeft).isGreaterThan(0)
    }

    @Test
    fun `showBarcodeImage  Correct background and padding settings  Colors Inversed `() {
        val bitmap = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888)
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns bitmap
        every { settings.barcodeBackgroundColor } returns Color.YELLOW
        every { settings.areBarcodeColorsInversed } returns true
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val activity = launchController().setup().get()
        idleCoroutines()

        val backgroundLayout = activity.findViewById<FrameLayout>(R.id.layout_barcode_image_background)

        val layoutBackgroundColor = (backgroundLayout.background as ColorDrawable).color
        assertThat(layoutBackgroundColor).isEqualTo(Color.YELLOW)
        assertThat(backgroundLayout.paddingLeft).isEqualTo(0)
        assertThat(backgroundLayout.paddingTop).isEqualTo(0)
        assertThat(backgroundLayout.paddingRight).isEqualTo(0)
        assertThat(backgroundLayout.paddingBottom).isEqualTo(0)
    }

    @Test
    fun `showBarcodeDate  Date formatting correctness`() {
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns null

        val activity = launchController().setup().get()
        idleCoroutines()
        val dateView = activity.findViewById<TextView>(R.id.text_view_date)
        val expected = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH).format(defaultBarcode.date)

        assertThat(dateView.text.toString()).isEqualTo(expected)
    }

    @Test
    fun `showBarcodeDate  Date formatting thread safety`() {
        val field = BarcodeImageActivity::class.java.getDeclaredField("dateFormatter")
        assertThat(Modifier.isStatic(field.modifiers)).isFalse()
        assertThat(field.type).isEqualTo(SimpleDateFormat::class.java)
    }

    @Test
    fun `showBarcodeFormat  Title is set correctly`() {
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns null

        val activity = launchController().setup().get()
        idleCoroutines()

        assertThat(activity.title).isEqualTo(activity.getString(R.string.qr_code))
    }

    @Test
    fun `showBarcodeText  Text is displayed correctly`() {
        every { BarcodeImageGenerator.generateBitmap(any(), any(), any(), any(), any(), any()) } returns null

        val activity = launchController().setup().get()
        idleCoroutines()

        val textView = activity.findViewById<TextView>(R.id.text_view_barcode_text)
        assertThat(textView.text.toString()).isEqualTo(defaultBarcode.text)
    }

    private fun launchController(
        barcodeExtra: Any? = defaultBarcode,
        includeExtra: Boolean = true,
        initialBrightness: Float = 0.5f
    ): ActivityController<BarcodeImageActivity> {
        val intent = Intent(application, BarcodeImageActivity::class.java)
        if (includeExtra) {
            when (barcodeExtra) {
                null -> intent.putExtra(BARCODE_KEY, null as Serializable?)
                is Serializable -> intent.putExtra(BARCODE_KEY, barcodeExtra)
                else -> throw IllegalArgumentException("Barcode extra must be Serializable")
            }
        }
        val controller = buildActivity(BarcodeImageActivity::class.java, intent)
        controller.get().window.attributes = controller.get().window.attributes.apply {
            screenBrightness = initialBrightness
        }
        return controller
    }

    private fun idleCoroutines() {
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Robolectric.flushBackgroundThreadScheduler() // FIXME: 'static fun flushBackgroundThreadScheduler(): Unit' is deprecated. Deprecated in Java.
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    companion object {
        private const val BARCODE_KEY = "BARCODE_KEY"
    }
}
