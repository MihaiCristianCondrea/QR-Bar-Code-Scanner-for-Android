package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.os.Build
import android.provider.ContactsContract
import androidx.appcompat.view.menu.MenuBuilder
import androidx.test.core.app.ApplicationProvider
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.google.common.truth.Truth.assertThat
import com.google.zxing.BarcodeFormat
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(org.robolectric.RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@LooperMode(LooperMode.Mode.PAUSED)
class CreateBarcodeActivityTest {
    private val permissionsHelper = mockk<com.d4rk.qrcodescanner.plus.utils.helpers.PermissionsHelper>()

    @Test
    fun `onCreateOptionsMenu inflates phone specific menu`() {
        val activity = buildActivity(barcodeSchema = BarcodeSchema.PHONE)

        val menu = MenuBuilder(activity)
        val created = activity.onCreateOptionsMenu(menu)

        assertThat(created).isTrue()
        assertThat(menu.findItem(R.id.item_phone)).isNotNull()
        assertThat(menu.findItem(R.id.item_contacts)).isNotNull()
        assertThat(menu.findItem(R.id.item_create_barcode)).isNotNull()
    }

    @Test
    fun `onCreateOptionsMenu returns false for app schema`() {
        val activity = buildActivity(barcodeSchema = BarcodeSchema.APP)

        val menu = MenuBuilder(activity)
        val created = activity.onCreateOptionsMenu(menu)

        assertThat(created).isFalse()
        assertThat(menu.size()).isEqualTo(0)
    }

    @Test
    fun `onOptionsItemSelected contacts requests permissions`() {
        val activity = buildActivity(barcodeSchema = BarcodeSchema.PHONE)
        val menu = MenuBuilder(activity)
        activity.onCreateOptionsMenu(menu)

        val handled = activity.onOptionsItemSelected(menu.findItem(R.id.item_contacts))

        assertThat(handled).isTrue()
        verify {
            permissionsHelper.requestPermissions(
                activity,
                match { it.contains(android.Manifest.permission.READ_CONTACTS) },
                any()
            )
        }
    }

    @Test
    fun `onOptionsItemSelected phone launches picker when available`() {
        val activity = buildActivity(barcodeSchema = BarcodeSchema.PHONE)
        val phoneIntent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.example"
                name = "ExampleActivity"
            }
        }
        shadowOf(activity.packageManager).addResolveInfoForIntent(phoneIntent, resolveInfo)

        val menu = MenuBuilder(activity)
        activity.onCreateOptionsMenu(menu)

        val handled = activity.onOptionsItemSelected(menu.findItem(R.id.item_phone))

        assertThat(handled).isTrue()
        val started = shadowOf(activity).nextStartedActivityForResult
        assertThat(started).isNotNull()
        assertThat(started.intent.action).isEqualTo(Intent.ACTION_PICK)
        assertThat(started.intent.type).isEqualTo(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)
        assertThat(started.requestCode).isEqualTo(1)
    }

    private fun buildActivity(
        barcodeFormat: BarcodeFormat = BarcodeFormat.QR_CODE,
        barcodeSchema: BarcodeSchema? = BarcodeSchema.OTHER,
        defaultText: String? = null,
        intentBuilder: Intent.() -> Unit = {}
    ): CreateBarcodeActivity {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CreateBarcodeActivity::class.java).apply {
            putExtra(BARCODE_FORMAT_KEY, barcodeFormat.ordinal)
            putExtra(BARCODE_SCHEMA_KEY, barcodeSchema?.ordinal ?: -1)
            putExtra(DEFAULT_TEXT_KEY, defaultText)
            intentBuilder()
        }
        return Robolectric.buildActivity(CreateBarcodeActivity::class.java, intent).setup().get()
    }

    private companion object {
        private const val BARCODE_FORMAT_KEY = "BARCODE_FORMAT_KEY"
        private const val BARCODE_SCHEMA_KEY = "BARCODE_SCHEMA_KEY"
        private const val DEFAULT_TEXT_KEY = "DEFAULT_TEXT_KEY"
    }
}
