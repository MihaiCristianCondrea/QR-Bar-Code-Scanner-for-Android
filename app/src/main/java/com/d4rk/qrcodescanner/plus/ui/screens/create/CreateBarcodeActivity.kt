package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityCreateBarcodeBinding
import com.d4rk.qrcodescanner.plus.di.barcodeDatabase
import com.d4rk.qrcodescanner.plus.di.barcodeParser
import com.d4rk.qrcodescanner.plus.di.contactHelper
import com.d4rk.qrcodescanner.plus.di.permissionsHelper
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.schema.App
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.components.navigation.UpNavigationActivity
import com.d4rk.qrcodescanner.plus.ui.components.navigation.setupToolbarWithUpNavigation
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.BarcodeActivity
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateAztecFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateCodabarFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateCode128Fragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateCode39Fragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateCode93Fragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateDataMatrixFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateEan13Fragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateEan8Fragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateItf14Fragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreatePdf417Fragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateUpcAFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateUpcEFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.AppAdapter
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeAppFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeBookmarkFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeCryptocurrencyFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeEmailFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeEventFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeLocationFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeMeCardFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeMmsFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeOtpFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodePhoneFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeSmsFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeTextFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeUrlFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeVCardFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeWifiFragment
import com.d4rk.qrcodescanner.plus.utils.extension.showError
import com.d4rk.qrcodescanner.plus.utils.extension.toStringId
import com.d4rk.qrcodescanner.plus.utils.extension.unsafeLazy
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class CreateBarcodeActivity : UpNavigationActivity(), AppAdapter.Listener {
    private lateinit var binding: ActivityCreateBarcodeBinding
    private val viewModel by viewModels<CreateBarcodeViewModel> {
        CreateBarcodeViewModelFactory(barcodeDatabase, settings)
    }

    companion object {
        private const val BARCODE_FORMAT_KEY = "BARCODE_FORMAT_KEY"
        private const val BARCODE_SCHEMA_KEY = "BARCODE_SCHEMA_KEY"
        private const val DEFAULT_TEXT_KEY = "DEFAULT_TEXT_KEY"
        private const val CHOOSE_PHONE_REQUEST_CODE = 1
        private const val CHOOSE_CONTACT_REQUEST_CODE = 2
        private const val CONTACTS_PERMISSION_REQUEST_CODE = 101
        private val CONTACTS_PERMISSIONS = arrayOf(Manifest.permission.READ_CONTACTS)
        fun start(
            context: Context,
            barcodeFormat: BarcodeFormat,
            barcodeSchema: BarcodeSchema? = null,
            defaultText: String? = null
        ) {
            val intent = Intent(context, CreateBarcodeActivity::class.java).apply {
                putExtra(BARCODE_FORMAT_KEY, barcodeFormat.ordinal)
                putExtra(BARCODE_SCHEMA_KEY, barcodeSchema?.ordinal ?: -1)
                putExtra(DEFAULT_TEXT_KEY, defaultText)
            }
            context.startActivity(intent)
        }
    }

    private val barcodeFormat by unsafeLazy {
        BarcodeFormat.entries.getOrNull(intent?.getIntExtra(BARCODE_FORMAT_KEY, -1) ?: -1)
            ?: BarcodeFormat.QR_CODE
    }
    private val barcodeSchema by unsafeLazy {
        BarcodeSchema.entries.getOrNull(intent?.getIntExtra(BARCODE_SCHEMA_KEY, -1) ?: -1)
    }
    private val defaultText by unsafeLazy {
        intent?.getStringExtra(DEFAULT_TEXT_KEY).orEmpty()
    }
    private var optionsMenu: Menu? = null
    private var isCreateButtonEnabled = false
    var isCreateBarcodeButtonEnabled: Boolean
        get() = isCreateButtonEnabled
        set(enabled) {
            isCreateButtonEnabled = enabled
            updateCreateMenuState()
        }

    private fun updateCreateMenuState() {
        val menuItem = optionsMenu?.findItem(R.id.item_create_barcode) ?: return
        val iconId = if (isCreateButtonEnabled) {
            R.drawable.ic_confirm_enabled
        } else {
            R.drawable.ic_confirm_disabled
        }
        menuItem.icon = ContextCompat.getDrawable(this, iconId)
        menuItem.isEnabled = isCreateButtonEnabled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (createBarcodeImmediatelyIfNeeded()) {
            return
        }
        binding = ActivityCreateBarcodeBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        setupToolbarWithUpNavigation()
        showToolbarTitle()
        showFragment()
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuId = resolveMenuId() ?: run {
            optionsMenu = null
            return false
        }
        menuInflater.inflate(menuId, menu)
        optionsMenu = menu
        updateCreateMenuState()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        updateCreateMenuState()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_phone -> {
                choosePhone()
                true
            }

            R.id.item_contacts -> {
                requestContactsPermissions()
                true
            }

            R.id.item_create_barcode -> {
                createBarcode()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun resolveMenuId(): Int? {
        return when (barcodeSchema) {
            BarcodeSchema.APP -> null
            BarcodeSchema.PHONE, BarcodeSchema.SMS, BarcodeSchema.MMS -> R.menu.menu_create_qr_code_phone
            BarcodeSchema.VCARD, BarcodeSchema.MECARD -> R.menu.menu_create_qr_code_contacts
            else -> R.menu.menu_create_barcode
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            CHOOSE_PHONE_REQUEST_CODE -> showChosenPhone(data)
            CHOOSE_CONTACT_REQUEST_CODE -> showChosenContact(data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE && permissionsHelper.areAllPermissionsGranted(
                grantResults
            )
        ) {
            chooseContact()
        }
    }

    override fun onAppClicked(packageName: String) {
        createBarcodeFromSchema(App.fromPackage(packageName))
    }

    private fun createBarcodeImmediatelyIfNeeded(): Boolean {
        if (intent?.action != Intent.ACTION_SEND) {
            return false
        }
        return when (intent?.type) {
            "text/plain" -> {
                createBarcodeForPlainText()
                true
            }

            "text/x-vcard" -> {
                createBarcodeForVCard()
                true
            }

            else -> false
        }
    }

    private fun createBarcodeForPlainText() {
        val text = intent?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val schema = barcodeParser.parseSchema(barcodeFormat, text)
        createBarcodeFromSchema(schema, true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createBarcodeForVCard() {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java) ?: return
        } else {
            @Suppress("DEPRECATION") intent?.extras?.get(Intent.EXTRA_STREAM) as? Uri ?: return
        }
        lifecycleScope.launch {
            val creationFlow = viewModel.readVCard(contentResolver, uri)
                .map { text -> barcodeParser.parseSchema(barcodeFormat, text) }
                .flatMapConcat { schema -> createBarcodeFlow(schema, true) }
            collectCreationFlow(creationFlow)
        }
    }

    private fun showToolbarTitle() {
        val titleId = barcodeSchema?.toStringId() ?: barcodeFormat.toStringId()
        setTitle(titleId)
    }

    private fun showFragment() {
        val fragment = when {
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.OTHER -> CreateQrCodeTextFragment.newInstance(
                defaultText
            )

            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.URL -> CreateQrCodeUrlFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.BOOKMARK -> CreateQrCodeBookmarkFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.PHONE -> CreateQrCodePhoneFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.WIFI -> CreateQrCodeWifiFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.EMAIL -> CreateQrCodeEmailFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.SMS -> CreateQrCodeSmsFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.MMS -> CreateQrCodeMmsFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.CRYPTOCURRENCY -> CreateQrCodeCryptocurrencyFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.GEO -> CreateQrCodeLocationFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.APP -> CreateQrCodeAppFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.OTP_AUTH -> CreateQrCodeOtpFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.VEVENT -> CreateQrCodeEventFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.VCARD -> CreateQrCodeVCardFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.MECARD -> CreateQrCodeMeCardFragment()
            barcodeFormat == BarcodeFormat.DATA_MATRIX -> CreateDataMatrixFragment()
            barcodeFormat == BarcodeFormat.AZTEC -> CreateAztecFragment()
            barcodeFormat == BarcodeFormat.PDF_417 -> CreatePdf417Fragment()
            barcodeFormat == BarcodeFormat.CODABAR -> CreateCodabarFragment()
            barcodeFormat == BarcodeFormat.CODE_39 -> CreateCode39Fragment()
            barcodeFormat == BarcodeFormat.CODE_93 -> CreateCode93Fragment()
            barcodeFormat == BarcodeFormat.CODE_128 -> CreateCode128Fragment()
            barcodeFormat == BarcodeFormat.EAN_8 -> CreateEan8Fragment()
            barcodeFormat == BarcodeFormat.EAN_13 -> CreateEan13Fragment()
            barcodeFormat == BarcodeFormat.ITF -> CreateItf14Fragment()
            barcodeFormat == BarcodeFormat.UPC_A -> CreateUpcAFragment()
            barcodeFormat == BarcodeFormat.UPC_E -> CreateUpcEFragment()
            else -> return
        }
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    private fun choosePhone() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        startActivityForResultIfExists(
            intent, CHOOSE_PHONE_REQUEST_CODE
        )
    }

    private fun showChosenPhone(data: Intent?) {
        val phone = contactHelper.getPhone(this, data) ?: return
        getCurrentFragment().showPhone(phone)
    }

    private fun requestContactsPermissions() {
        permissionsHelper.requestPermissions(
            this,
            CONTACTS_PERMISSIONS,
            CONTACTS_PERMISSION_REQUEST_CODE
        )

    }

    private fun chooseContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResultIfExists(
            intent, CHOOSE_CONTACT_REQUEST_CODE
        )
    }

    private fun showChosenContact(data: Intent?) {
        val contact = contactHelper.getContact(this, data) ?: return
        getCurrentFragment().showContact(contact)
    }

    private fun startActivityForResultIfExists(intent: Intent, requestCode: Int) {
        if (intent.resolveActivity(packageManager) != null) {
            @Suppress("DEPRECATION")
            startActivityForResult(intent, requestCode)
        } else {
            Snackbar.make(binding.root, R.string.snack_no_app_found, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun createBarcode() {
        val schema = getCurrentFragment().getBarcodeSchema()
        createBarcodeFromSchema(schema)
    }

    private fun createBarcodeFromSchema(schema: Schema, finish: Boolean = false) {
        lifecycleScope.launch {
            collectCreationFlow(createBarcodeFlow(schema, finish))
        }
    }

    private fun createBarcodeFlow(schema: Schema, finish: Boolean): Flow<CreateBarcodeNavigation> {
        val barcode = buildBarcode(schema)
        return viewModel.saveBarcode(barcode).map { savedBarcode ->
            CreateBarcodeNavigation(savedBarcode, finish)
        }
    }

    private suspend fun collectCreationFlow(flow: Flow<CreateBarcodeNavigation>) {
        flow
            .catch { showError(it) }
            .collect { navigation ->
                navigateToBarcodeScreen(navigation.barcode, navigation.finish)
            }
    }

    private fun buildBarcode(schema: Schema): Barcode {
        return Barcode(
            text = schema.toBarcodeText(),
            formattedText = schema.toFormattedText(),
            format = barcodeFormat,
            schema = schema.schema,
            date = System.currentTimeMillis(),
            isGenerated = true
        )
    }

    private data class CreateBarcodeNavigation(
        val barcode: Barcode,
        val finish: Boolean
    )

    private fun getCurrentFragment(): BaseCreateBarcodeFragment {
        return supportFragmentManager.findFragmentById(R.id.container) as BaseCreateBarcodeFragment
    }

    private fun navigateToBarcodeScreen(barcode: Barcode, finish: Boolean) {
        BarcodeActivity.start(this, barcode, true)
        if (finish) {
            finish()
        }
    }
}