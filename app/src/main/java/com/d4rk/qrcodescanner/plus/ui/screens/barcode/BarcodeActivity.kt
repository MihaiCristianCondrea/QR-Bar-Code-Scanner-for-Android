package com.d4rk.qrcodescanner.plus.ui.screens.barcode
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.print.PrintHelper
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityBarcodeBinding
import com.d4rk.qrcodescanner.plus.di.barcodeDatabase
import com.d4rk.qrcodescanner.plus.di.barcodeImageGenerator
import com.d4rk.qrcodescanner.plus.di.barcodeImageSaver
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.di.wifiConnector
import com.d4rk.qrcodescanner.plus.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.extension.currentLocale
import com.d4rk.qrcodescanner.plus.extension.orFalse
import com.d4rk.qrcodescanner.plus.extension.showError
import com.d4rk.qrcodescanner.plus.extension.toCountryEmoji
import com.d4rk.qrcodescanner.plus.extension.toEmailType
import com.d4rk.qrcodescanner.plus.extension.toPhoneType
import com.d4rk.qrcodescanner.plus.extension.toStringId
import com.d4rk.qrcodescanner.plus.extension.unsafeLazy
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.otp.OtpActivity
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.save.SaveBarcodeAsImageActivity
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.save.SaveBarcodeAsTextActivity
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.ParsedBarcode
import com.d4rk.qrcodescanner.plus.model.SearchEngine
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.model.schema.OtpAuth
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.ChooseSearchEngineDialogFragment
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.DeleteConfirmationDialogFragment
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.EditBarcodeNameDialogFragment
import com.d4rk.qrcodescanner.plus.domain.history.save
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.text.SimpleDateFormat
import java.util.Locale

class BarcodeActivity : BaseActivity(), DeleteConfirmationDialogFragment.Listener, ChooseSearchEngineDialogFragment.Listener, EditBarcodeNameDialogFragment.Listener {
    companion object {
        private const val BARCODE_KEY = "BARCODE_KEY"
        private const val IS_CREATED = "IS_CREATED"
        fun start(context: Context, barcode: Barcode, isCreated: Boolean = false) {
            val intent = Intent(context, BarcodeActivity::class.java).apply {
                putExtra(BARCODE_KEY, barcode)
                putExtra(IS_CREATED, isCreated)
            }
            context.startActivity(intent)
        }
    }
    private lateinit var binding: ActivityBarcodeBinding
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
    private val originalBarcode by unsafeLazy {
        @Suppress("DEPRECATION")
        intent?.getSerializableExtra(BARCODE_KEY) as? Barcode ?: throw IllegalArgumentException("No barcode passed")
    }
    private val isCreated by unsafeLazy {
        intent?.getBooleanExtra(IS_CREATED, false).orFalse()
    }
    private val barcode by unsafeLazy {
        ParsedBarcode(originalBarcode)
    }
    private val clipboardManager by unsafeLazy {
        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }
    private var originalBrightness: Float = 0.5f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        saveOriginalBrightness()
        applySettings()
        handleToolbarBackPressed()
        handleToolbarMenuClicked()
        handleButtonsClicked()
        showBarcode()
        showOrHideButtons()
        showButtonText()
        MobileAds.initialize(this)
        binding.adView.loadAd(AdRequest.Builder().build())
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }
    override fun onDeleteConfirmed() {
        deleteBarcode()
    }
    override fun onNameConfirmed(name: String) {
        updateBarcodeName(name)
    }
    override fun onSearchEngineSelected(searchEngine: SearchEngine) {
        performWebSearchUsingSearchEngine(searchEngine)
    }
    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }
    private fun saveOriginalBrightness() {
        originalBrightness = window.attributes.screenBrightness
    }
    private fun applySettings() {
        if (settings.copyToClipboard) {
            copyToClipboard(barcode.text)
        }
        if (settings.openLinksAutomatically.not() || isCreated) {
            return
        }
        when (barcode.schema) {
            BarcodeSchema.APP -> openInAppMarket()
            BarcodeSchema.BOOKMARK -> saveBookmark()
            BarcodeSchema.CRYPTOCURRENCY -> openBitcoinUrl()
            BarcodeSchema.EMAIL -> sendEmail(barcode.email)
            BarcodeSchema.GEO -> showLocation()
            BarcodeSchema.GOOGLE_MAPS -> showLocation()
            BarcodeSchema.MMS -> sendSmsOrMms(barcode.phone)
            BarcodeSchema.MECARD -> addToContacts()
            BarcodeSchema.OTP_AUTH -> openOtpInOtherApp()
            BarcodeSchema.PHONE -> callPhone(barcode.phone)
            BarcodeSchema.SMS -> sendSmsOrMms(barcode.phone)
            BarcodeSchema.URL -> openLink()
            BarcodeSchema.VEVENT -> addToCalendar()
            BarcodeSchema.VCARD -> addToContacts()
            BarcodeSchema.WIFI -> connectToWifi()
            BarcodeSchema.YOUTUBE -> openInYoutube()
            BarcodeSchema.NZCOVIDTRACER -> openLink()
            BarcodeSchema.BOARDINGPASS -> return
            else -> return
        }
    }
    private fun handleToolbarBackPressed() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    private fun handleToolbarMenuClicked() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_increase_brightness -> {
                    increaseBrightnessToMax()
                    binding.toolbar.menu.findItem(R.id.item_increase_brightness).isVisible = false
                    binding.toolbar.menu.findItem(R.id.item_decrease_brightness).isVisible = true
                }
                R.id.item_decrease_brightness -> {
                    restoreOriginalBrightness()
                    binding.toolbar.menu.findItem(R.id.item_increase_brightness).isVisible = true
                    binding.toolbar.menu.findItem(R.id.item_decrease_brightness).isVisible = false
                }
                R.id.item_add_to_favorites -> toggleIsFavorite()
                R.id.item_show_barcode_image -> navigateToBarcodeImageActivity()
                R.id.item_save -> saveBarcode()
                R.id.item_delete -> showDeleteBarcodeConfirmationDialog()
            }
            return@setOnMenuItemClickListener true
        }
    }
    private fun handleButtonsClicked() {
        binding.buttonEditName.setOnClickListener {
            showEditBarcodeNameDialog()
        }
        binding.buttonSearchOnWeb.setOnClickListener {
            searchBarcodeTextOnInternet()
        }
        binding.buttonAddToCalendar.setOnClickListener {
            addToCalendar()
        }
        binding.buttonAddToContacts.setOnClickListener {
            addToContacts()
        }
        binding.buttonShowLocation.setOnClickListener {
            showLocation()
        }
        binding.buttonConnectToWifi.setOnClickListener {
            connectToWifi()
        }
        binding.buttonOpenWifiSettings.setOnClickListener {
            openWifiSettings()
        }
        binding.buttonCopyNetworkName.setOnClickListener {
            copyNetworkNameToClipboard()
        }
        binding.buttonCopyNetworkPassword.setOnClickListener {
            copyNetworkPasswordToClipboard()
        }
        binding.buttonOpenApp.setOnClickListener {
            openApp()
        }
        binding.buttonOpenInAppMarket.setOnClickListener {
            openInAppMarket()
        }
        binding.buttonOpenInYoutube.setOnClickListener {
            openInYoutube()
        }
        binding.buttonShowOtp.setOnClickListener {
            showOtp()
        }
        binding.buttonOpenOtp.setOnClickListener {
            openOtpInOtherApp()
        }
        binding.buttonOpenBitcoinUri.setOnClickListener {
            openBitcoinUrl()
        }
        binding.buttonOpenLink.setOnClickListener {
            openLink()
        }
        binding.buttonSaveBookmark.setOnClickListener {
            saveBookmark()
        }
        binding.buttonCallPhone1.setOnClickListener {
            callPhone(barcode.phone)
        }
        binding.buttonCallPhone2.setOnClickListener {
            callPhone(barcode.secondaryPhone)
        }
        binding.buttonCallPhone3.setOnClickListener {
            callPhone(barcode.tertiaryPhone)
        }
        binding.buttonSendSmsOrMms1.setOnClickListener {
            sendSmsOrMms(barcode.phone)
        }
        binding.buttonSendSmsOrMms2.setOnClickListener {
            sendSmsOrMms(barcode.secondaryPhone)
        }
        binding.buttonSendSmsOrMms3.setOnClickListener {
            sendSmsOrMms(barcode.tertiaryPhone)
        }
        binding.buttonSendEmail1.setOnClickListener {
            sendEmail(barcode.email)
        }
        binding.buttonSendEmail2.setOnClickListener {
            sendEmail(barcode.secondaryEmail)
        }
        binding.buttonSendEmail3.setOnClickListener {
            sendEmail(barcode.tertiaryEmail)
        }
        binding.buttonShareAsText.setOnClickListener {
            shareBarcodeAsText()
        }
        binding.buttonCopy.setOnClickListener {
            copyBarcodeTextToClipboard()
        }
        binding.buttonSearch.setOnClickListener {
            searchBarcodeTextOnInternet()
        }
        binding.buttonSaveAsText.setOnClickListener {
            navigateToSaveBarcodeAsTextActivity()
        }
        binding.buttonShareAsImage.setOnClickListener {
            shareBarcodeAsImage()
        }
        binding.buttonSaveAsImage.setOnClickListener {
            navigateToSaveBarcodeAsImageActivity()
        }
        binding.buttonPrint.setOnClickListener {
            printBarcode()
        }
    }
    private fun toggleIsFavorite() {
        val newBarcode = originalBarcode.copy(isFavorite = barcode.isFavorite.not())
        // Launch a coroutine in the lifecycleScope
        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    barcodeDatabase.save(newBarcode)
                }
            }.onSuccess {
                barcode.isFavorite = newBarcode.isFavorite
                showBarcodeIsFavorite(newBarcode.isFavorite)
            }.onFailure { error ->
                showError(error)
            }
        }
    }

    private fun updateBarcodeName(name: String) {
        if (name.isBlank()) {
            return
        }
        val newBarcode = originalBarcode.copy(
            id = barcode.id,
            name = name
        )

        // Launch a coroutine in the lifecycleScope
        lifecycleScope.launch {
            try {
                // Perform the database operation on a background thread
                withContext(context = Dispatchers.IO) {
                    barcodeDatabase.save(newBarcode) // Assuming barcodeDatabase.save() is a suspend function
                    // or you'll need to wrap it appropriately
                    // e.g., using suspendCoroutine or a Flow
                }
                // Update UI on the main thread
                barcode.name = name
                showBarcodeName(name)
            } catch (e: Exception) {
                // Handle errors on the main thread
                showError(e)
            }
        }
    }
    private fun saveBarcode() {
        binding.toolbar.menu?.findItem(R.id.item_save)?.isVisible = false
        // Launch a coroutine in the lifecycleScope
        lifecycleScope.launch {
            try {
                // Perform the database operation on a background thread
                val id = withContext(Dispatchers.IO) {
                    barcodeDatabase.save(originalBarcode, settings.doNotSaveDuplicates) // Assuming barcodeDatabase.save() is a suspend function or returns a Deferred/Flow
                }
                // Update UI on the main thread
                barcode.id = id
                binding.buttonEditName.isVisible = true
                binding.toolbar.menu?.findItem(R.id.item_delete)?.isVisible = true
            } catch (error: Exception) {
                // Handle errors on the main thread
                binding.toolbar.menu?.findItem(R.id.item_save)?.isVisible = true
                showError(error)
            }
        }
    }

    private fun deleteBarcode() {
        showLoading(true)
        // Launch a coroutine in the lifecycleScope
        lifecycleScope.launch {
            try {
                // Perform the database operation on a background thread
                withContext(Dispatchers.IO) {
                    barcodeDatabase.delete(barcode.id) // Assuming barcodeDatabase.delete() is a suspend function
                    // or you'll need to wrap it appropriately
                    // e.g., using suspendCoroutine or a Flow
                }
                // Update UI on the main thread
                finish()
            } catch (error: Exception) {
                // Handle errors on the main thread
                showLoading(false)
                showError(error)
            }
            // No need to call .addTo(disposable) as lifecycleScope handles cancellation.
        }
    }
    private fun addToCalendar() {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, barcode.eventSummary)
            putExtra(CalendarContract.Events.DESCRIPTION, barcode.eventDescription)
            putExtra(CalendarContract.Events.EVENT_LOCATION, barcode.eventLocation)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, barcode.eventStartDate)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, barcode.eventEndDate)
        }
        startActivityIfExists(intent)
    }
    private fun addToContacts() {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
            val fullName = "${barcode.firstName.orEmpty()} ${barcode.lastName.orEmpty()}"
            putExtra(ContactsContract.Intents.Insert.NAME, fullName)
            putExtra(ContactsContract.Intents.Insert.COMPANY, barcode.organization.orEmpty())
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, barcode.jobTitle.orEmpty())
            putExtra(ContactsContract.Intents.Insert.PHONE, barcode.phone.orEmpty())
            putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, barcode.phoneType.orEmpty().toPhoneType())
            putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, barcode.secondaryPhone.orEmpty())
            putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, barcode.secondaryPhoneType.orEmpty().toPhoneType())
            putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, barcode.tertiaryPhone.orEmpty())
            putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, barcode.tertiaryPhoneType.orEmpty().toPhoneType())
            putExtra(ContactsContract.Intents.Insert.EMAIL, barcode.email.orEmpty())
            putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, barcode.emailType.orEmpty().toEmailType())
            putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, barcode.secondaryEmail.orEmpty())
            putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, barcode.secondaryEmailType.orEmpty().toEmailType())
            putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, barcode.tertiaryEmail.orEmpty())
            putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE, barcode.tertiaryEmailType.orEmpty().toEmailType())
            putExtra(ContactsContract.Intents.Insert.NOTES, barcode.note.orEmpty())
        }
        startActivityIfExists(intent)
    }
    private fun callPhone(phone: String?) {
        val phoneUri = "tel:${phone.orEmpty()}"
        startActivityIfExists(Intent.ACTION_DIAL, phoneUri)
    }
    private fun sendSmsOrMms(phone: String?) {
        val uri = "sms:${phone.orEmpty()}".toUri()
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", barcode.smsBody.orEmpty())
        }
        startActivityIfExists(intent)
    }
    private fun sendEmail(email: String?) {
        val uri = "mailto:${email.orEmpty()}".toUri()
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            setDataAndType(uri, "text/plain")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email.orEmpty()))
            putExtra(Intent.EXTRA_SUBJECT, barcode.emailSubject.orEmpty())
            putExtra(Intent.EXTRA_TEXT, barcode.emailBody.orEmpty())
        }
        startActivityIfExists(intent)
    }
    private fun showLocation() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.geoUri.orEmpty())
    }
    private fun connectToWifi() {
        showConnectToWifiButtonEnabled(false)
        // Launch a coroutine in the lifecycleScope
        lifecycleScope.launch {
            try {
                // Assuming wifiConnector.connect(...) can be made a suspend function
                // or adapted to work with coroutines (e.g., using suspendCancellableCoroutine).
                // For this example, let's assume it's adapted to be a suspend function.
                withContext(Dispatchers.IO) { // Perform network operation on IO dispatcher
                    wifiConnector.connect(
                        this@BarcodeActivity, // Use qualified this for activity context
                        barcode.networkAuthType.orEmpty(),
                        barcode.networkName.orEmpty(),
                        barcode.networkPassword.orEmpty(),
                        barcode.isHidden.orFalse(),
                        barcode.anonymousIdentity.orEmpty(),
                        barcode.identity.orEmpty(),
                        barcode.eapMethod.orEmpty(),
                        barcode.phase2Method.orEmpty()
                    ) // This call would be to a suspend function or a Flow collected here
                }
                // Update UI on the main thread after successful connection
                showConnectToWifiButtonEnabled(true)
                snackBar(R.string.connecting)
            } catch (error: Exception) {
                // Handle errors on the main thread
                showConnectToWifiButtonEnabled(true)
                showError(error)
            }
            // No need to call .addTo(disposable) as lifecycleScope handles cancellation.
        }
    }
    private fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivityIfExists(intent)
    }
    private fun copyNetworkNameToClipboard() {
        copyToClipboard(barcode.networkName.orEmpty())
        snackBar(R.string.snack_copied_to_clipboard)
    }
    private fun copyNetworkPasswordToClipboard() {
        copyToClipboard(barcode.networkPassword.orEmpty())
        snackBar(R.string.snack_copied_to_clipboard)
    }
    private fun openApp() {
        val intent = packageManager?.getLaunchIntentForPackage(barcode.appPackage.orEmpty())
        if (intent != null) {
            startActivityIfExists(intent)
        }
    }
    private fun openInAppMarket() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.appMarketUrl.orEmpty())
    }
    private fun openInYoutube() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.youtubeUrl.orEmpty())
    }
    private fun showOtp() {
        val otp = OtpAuth.parse(barcode.otpUrl.orEmpty()) ?: return
        OtpActivity.start(this, otp)
    }
    private fun openOtpInOtherApp() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.otpUrl.orEmpty())
    }
    private fun openBitcoinUrl() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.bitcoinUri.orEmpty())
    }
    private fun openLink() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.url.orEmpty())
    }
    private fun saveBookmark() {
        val intent = Intent(Intent.ACTION_INSERT, "content://browser/bookmarks".toUri()).apply {
            putExtra("title", barcode.bookmarkTitle.orEmpty())
            putExtra("url", barcode.url.orEmpty())
        }
        startActivityIfExists(intent)
    }
    private fun shareBarcodeAsText() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, barcode.text)
        }
        startActivityIfExists(intent)
    }
    private fun copyBarcodeTextToClipboard() {
        copyToClipboard(barcode.text)
        snackBar(R.string.snack_copied_to_clipboard)
    }
    private fun searchBarcodeTextOnInternet() {
        when (val searchEngine = settings.searchEngine) {
           SearchEngine.NONE -> performWebSearch()
           SearchEngine.ASK_EVERY_TIME -> showSearchEnginesDialog()
           else -> performWebSearchUsingSearchEngine(searchEngine)
        }
    }
    private fun performWebSearch() {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, barcode.text)
        }
        startActivityIfExists(intent)
    }
    private fun performWebSearchUsingSearchEngine(searchEngine: SearchEngine) {
        val url = searchEngine.templateUrl + barcode.text
        startActivityIfExists(Intent.ACTION_VIEW, url)
    }
    private fun shareBarcodeAsImage() {
        val imageUri = try {
            val image = barcodeImageGenerator.generateBitmap(originalBarcode, 200, 200, 1)
            barcodeImageSaver.saveImageToCache(this, image, barcode)
        } catch (ex: Exception) {
            showError(ex)
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivityIfExists(intent)
    }
    private fun printBarcode() {
        val barcodeImage = try {
            barcodeImageGenerator.generateBitmap(originalBarcode, 1000, 1000, 3)
        } catch (ex: Exception) {
            showError(ex)
            return
        }
        PrintHelper(this).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
            printBitmap("${barcode.format}_${barcode.schema}_${barcode.date}", barcodeImage)
        }
    }
    private fun navigateToBarcodeImageActivity() {
        BarcodeImageActivity.start(this, originalBarcode)
    }
    private fun navigateToSaveBarcodeAsTextActivity() {
        SaveBarcodeAsTextActivity.start(this, originalBarcode)
    }
    private fun navigateToSaveBarcodeAsImageActivity() {
        SaveBarcodeAsImageActivity.start(this, originalBarcode)
    }
    private fun showBarcode() {
        showBarcodeMenuIfNeeded()
        showBarcodeIsFavorite()
        showBarcodeImageIfNeeded()
        showBarcodeDate()
        showBarcodeFormat()
        showBarcodeName()
        showBarcodeText()
        showBarcodeCountry()
    }
    private fun showBarcodeMenuIfNeeded() {
        binding.toolbar.inflateMenu(R.menu.menu_barcode)
        binding.toolbar.menu.apply {
            findItem(R.id.item_increase_brightness).isVisible = isCreated
            findItem(R.id.item_add_to_favorites)?.isVisible = barcode.isInDb
            findItem(R.id.item_show_barcode_image)?.isVisible = isCreated.not()
            findItem(R.id.item_save)?.isVisible = barcode.isInDb.not()
            findItem(R.id.item_delete)?.isVisible = barcode.isInDb
        }
    }
    private fun showBarcodeIsFavorite() {
        showBarcodeIsFavorite(barcode.isFavorite)
    }
    private fun showBarcodeIsFavorite(isFavorite: Boolean) {
        val iconId = if (isFavorite) {
            R.drawable.ic_favorite_checked
        } else {
            R.drawable.ic_favorite_unchecked
        }
        binding.toolbar.menu?.findItem(R.id.item_add_to_favorites)?.icon = ContextCompat.getDrawable(this, iconId)
    }
    private fun showBarcodeImageIfNeeded() {
        if (isCreated) {
            showBarcodeImage()
        }
    }
    private fun showBarcodeImage() {
        try {
            val bitmap = barcodeImageGenerator.generateBitmap(originalBarcode, 2000, 2000, 0, settings.barcodeContentColor, settings.barcodeBackgroundColor)
            binding.layoutBarcodeImageBackground.isVisible = true
            binding.imageViewBarcode.isVisible = true
            binding.imageViewBarcode.setImageBitmap(bitmap)
            binding.imageViewBarcode.setBackgroundColor(settings.barcodeBackgroundColor)
            binding.layoutBarcodeImageBackground.setBackgroundColor(settings.barcodeBackgroundColor)
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO || settings.areBarcodeColorsInversed) {
                binding.layoutBarcodeImageBackground.setPadding(0, 0, 0, 0)
            }
        } catch (_: Exception) {
            binding.imageViewBarcode.isVisible = false
        }
    }
    private fun showBarcodeDate() {
        binding.textViewDate.text = dateFormatter.format(barcode.date)
    }
    private fun showBarcodeFormat() {
        val format = barcode.format.toStringId()
        binding.toolbar.setTitle(format)
    }
    private fun showBarcodeName() {
        showBarcodeName(barcode.name)
    }
    private fun showBarcodeName(name: String?) {
        binding.textViewBarcodeName.isVisible = name.isNullOrBlank().not()
        binding.textViewBarcodeName.text = name.orEmpty()
    }
    private fun showBarcodeText() {
        binding.textViewBarcodeText.text = if (isCreated) {
            barcode.text
        } else {
            barcode.formattedText
        }
    }
    private fun showBarcodeCountry() {
        val country = barcode.country ?: return
        when (country.contains('/')) {
            false -> showOneBarcodeCountry(country)
            true -> showTwoBarcodeCountries(country.split('/'))
        }
    }
    private fun showOneBarcodeCountry(country: String) {
        val fullCountryName = buildFullCountryName(country)
        showFullCountryName(fullCountryName)
    }
    private fun showTwoBarcodeCountries(countries: List<String>) {
        val firstFullCountryName = buildFullCountryName(countries[0])
        val secondFullCountryName = buildFullCountryName(countries[1])
        val fullCountryName = "$firstFullCountryName / $secondFullCountryName"
        showFullCountryName(fullCountryName)
    }
    private fun buildFullCountryName(country: String): String {
        val currentLocale = currentLocale ?: return ""
        val countryName = Locale.Builder().setRegion(country).build().getDisplayName(currentLocale)
        val countryEmoji = country.toCountryEmoji()
        return "$countryEmoji $countryName"
    }
    private fun showFullCountryName(fullCountryName: String) {
        binding.textViewCountry.apply {
            text = fullCountryName
            isVisible = fullCountryName.isBlank().not()
        }
    }
    private fun showOrHideButtons() {
        binding.buttonSearch.isVisible = isCreated.not()
        binding.buttonEditName.isVisible = barcode.isInDb
        if (isCreated) {
            return
        }
        binding.buttonSearchOnWeb.isVisible = barcode.isProductBarcode
        binding.buttonSearch.isVisible = barcode.isProductBarcode.not()
        binding.buttonAddToCalendar.isVisible = barcode.schema == BarcodeSchema.VEVENT
        binding.buttonAddToContacts.isVisible = barcode.schema == BarcodeSchema.VCARD || barcode.schema == BarcodeSchema.MECARD
        binding.buttonCallPhone1.isVisible = barcode.phone.isNullOrEmpty().not()
        binding.buttonCallPhone2.isVisible = barcode.secondaryPhone.isNullOrEmpty().not()
        binding.buttonCallPhone3.isVisible = barcode.tertiaryPhone.isNullOrEmpty().not()
        binding.buttonSendSmsOrMms1.isVisible = barcode.phone.isNullOrEmpty().not() || barcode.smsBody.isNullOrEmpty().not()
        binding.buttonSendSmsOrMms2.isVisible = barcode.secondaryPhone.isNullOrEmpty().not()
        binding.buttonSendSmsOrMms3.isVisible = barcode.tertiaryPhone.isNullOrEmpty().not()
        binding.buttonSendEmail1.isVisible = barcode.email.isNullOrEmpty().not() || barcode.emailSubject.isNullOrEmpty().not() || barcode.emailBody.isNullOrEmpty().not()
        binding.buttonSendEmail2.isVisible = barcode.secondaryEmail.isNullOrEmpty().not()
        binding.buttonSendEmail3.isVisible = barcode.tertiaryEmail.isNullOrEmpty().not()
        binding.buttonShowLocation.isVisible = barcode.geoUri.isNullOrEmpty().not()
        binding.buttonConnectToWifi.isVisible = barcode.schema == BarcodeSchema.WIFI
        binding.buttonOpenWifiSettings.isVisible = barcode.schema == BarcodeSchema.WIFI
        binding.buttonCopyNetworkName.isVisible = barcode.networkName.isNullOrEmpty().not()
        binding.buttonCopyNetworkPassword.isVisible = barcode.networkPassword.isNullOrEmpty().not()
        binding.buttonOpenApp.isVisible = barcode.appPackage.isNullOrEmpty().not() && isAppInstalled(barcode.appPackage)
        binding.buttonOpenInAppMarket.isVisible = barcode.appMarketUrl.isNullOrEmpty().not()
        binding.buttonOpenInYoutube.isVisible = barcode.youtubeUrl.isNullOrEmpty().not()
        binding.buttonShowOtp.isVisible = barcode.otpUrl.isNullOrEmpty().not()
        binding.buttonOpenOtp.isVisible = barcode.otpUrl.isNullOrEmpty().not()
        binding.buttonOpenBitcoinUri.isVisible = barcode.bitcoinUri.isNullOrEmpty().not()
        binding.buttonOpenLink.isVisible = barcode.url.isNullOrEmpty().not()
        binding.buttonSaveBookmark.isVisible = barcode.schema == BarcodeSchema.BOOKMARK
    }
    private fun showButtonText() {
        binding.buttonCallPhone1.text = getString(R.string.call, barcode.phone)
        binding.buttonCallPhone2.text = getString(R.string.call, barcode.secondaryPhone)
        binding.buttonCallPhone3.text = getString(R.string.call, barcode.tertiaryPhone)
        binding.buttonSendSmsOrMms1.text = getString(R.string.send_sms_mms_to, barcode.phone)
        binding.buttonSendSmsOrMms2.text = getString(R.string.send_sms_mms_to, barcode.secondaryPhone)
        binding.buttonSendSmsOrMms3.text = getString(R.string.send_sms_mms_to, barcode.tertiaryPhone)
        binding.buttonSendEmail1.text = getString(R.string.email_to, barcode.email)
        binding.buttonSendEmail2.text = getString(R.string.email_to, barcode.secondaryEmail)
        binding.buttonSendEmail3.text = getString(R.string.email_to, barcode.tertiaryEmail)
    }
    private fun showConnectToWifiButtonEnabled(isEnabled: Boolean) {
        binding.buttonConnectToWifi.isEnabled = isEnabled
    }
    private fun showDeleteBarcodeConfirmationDialog() {
        val dialog = DeleteConfirmationDialogFragment.newInstance(R.string.dialog_delete)
        dialog.show(supportFragmentManager, "")
    }
    private fun showEditBarcodeNameDialog() {
        val dialog = EditBarcodeNameDialogFragment.newInstance(barcode.name)
        dialog.show(supportFragmentManager, "")
    }
    private fun showSearchEnginesDialog() {
        val dialog = ChooseSearchEngineDialogFragment()
        dialog.show(supportFragmentManager, "")
    }
    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLoading.isVisible = isLoading
        binding.scrollView.isVisible = isLoading.not()
    }
    private fun startActivityIfExists(action: String, uri: String) {
        val intent = Intent(action, uri.toUri())
        startActivityIfExists(intent)
    }
    private fun startActivityIfExists(intent: Intent) {
        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK
        lifecycleScope.launch {
            val exists = withContext(Dispatchers.Default) {
                intent.resolveActivity(packageManager) != null
            }
            if (exists) {
                startActivity(intent)
            } else {
                snackBar(R.string.snack_no_app_found)
            }
        }
    }
    private fun isAppInstalled(appPackage: String?): Boolean {
        return packageManager?.getLaunchIntentForPackage(appPackage.orEmpty()) != null
    }
    private fun copyToClipboard(text: String) {
        val clipData = ClipData.newPlainText("", text)
        clipboardManager.setPrimaryClip(clipData)
    }
    private fun snackBar(stringId: Int) {
        Snackbar.make(binding.root, stringId, Snackbar.LENGTH_LONG).show()
    }
    private fun increaseBrightnessToMax() {
        setBrightness(1.0f)
    }
    private fun restoreOriginalBrightness() {
        setBrightness(originalBrightness)
    }
    private fun setBrightness(brightness: Float) {
        window.attributes = window.attributes.apply {
            screenBrightness = brightness
        }
    }
}