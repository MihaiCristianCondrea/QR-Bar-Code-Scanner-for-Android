package com.d4rk.qrcodescanner.plus.ui.screens.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.d4rk.qrcodescanner.plus.BuildConfig
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ads.AdUtils
import com.d4rk.qrcodescanner.plus.data.help.HelpRepository
import com.d4rk.qrcodescanner.plus.data.help.ReviewLaunchResult
import com.d4rk.qrcodescanner.plus.data.help.ReviewRequestResult
import com.d4rk.qrcodescanner.plus.databinding.ActivityHelpBinding
import com.d4rk.qrcodescanner.plus.databinding.DialogVersionInfoBinding
import com.d4rk.qrcodescanner.plus.databinding.ItemHelpFaqBinding
import com.d4rk.qrcodescanner.plus.ui.components.navigation.UpNavigationActivity
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.d4rk.qrcodescanner.plus.utils.helpers.OpenSourceLicensesHelper
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class HelpActivity : UpNavigationActivity() {

    private lateinit var binding: ActivityHelpBinding
    private val handler = Handler(Looper.getMainLooper())
    private var reviewRequestJob: Job? = null

    private val helpViewModel: HelpViewModel by viewModels {
        val reviewManager = ReviewManagerFactory.create(this)
        HelpViewModelFactory(HelpRepository(reviewManager))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
        AdUtils.loadBanner(binding.faqNativeAd)
        bindFaqItems()
        setupContactSupportCard()
        setupFeedbackFab()

        handler.postDelayed({ binding.fabContactSupport.shrink() }, FAB_AUTO_SHRINK_DELAY_MS)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_feedback, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.view_in_google_play -> {
                openGooglePlayListing()
                true
            }

            R.id.version_info -> {
                showVersionInfoDialog()
                true
            }

            R.id.beta_program -> {
                openLink("https://play.google.com/apps/testing/$packageName")
                true
            }

            R.id.terms_of_service -> {
                openLink("https://mihaicristiancondrea.github.io/profile/#terms-of-service-end-user-software")
                true
            }

            R.id.privacy_policy -> {
                openLink("https://mihaicristiancondrea.github.io/profile/#privacy-policy-end-user-software")
                true
            }

            R.id.oss -> {
                OpenSourceLicensesHelper.openLicensesScreen(this)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showVersionInfoDialog() {
        val dialogBinding = DialogVersionInfoBinding.inflate(LayoutInflater.from(this))
        dialogBinding.appIcon.setImageResource(R.mipmap.ic_launcher)
        dialogBinding.appName.text = getString(R.string.app_name)
        dialogBinding.appVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        dialogBinding.appCopyright.text = getString(R.string.copyright)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openGooglePlayListing() {
        val appPackageName = packageName
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri()))
        }.onFailure {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
                )
            )
        }
    }

    private fun openLink(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(browserIntent)
    }

    private fun setupContactSupportCard() {
        binding.contactSupportCard.setOnClickListener { openSupportEmail() }
    }

    private fun setupFeedbackFab() {
        binding.fabContactSupport.setOnClickListener { requestReview() }
        binding.fabContactSupport.contentDescription = getString(R.string.send_feedback)
    }

    private fun openSupportEmail() {
        val supportEmail = getString(R.string.contact_support_email)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.fromParts("mailto", supportEmail, null)
            putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
            putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.contact_support_email_subject, getString(R.string.app_name))
            )
            putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_support_email_body))
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.contact_support_title)))
        } else {
            Snackbar.make(binding.root, R.string.support_link_unavailable, Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun requestReview() {
        if (reviewRequestJob?.isActive == true) {
            return
        }
        binding.fabContactSupport.isEnabled = false
        reviewRequestJob = lifecycleScope.launch {
            helpViewModel.requestReviewFlow().collect { result ->
                when (result) {
                    is ReviewRequestResult.Success -> handleInAppReview(result.reviewInfo)
                    is ReviewRequestResult.Error -> {
                        binding.fabContactSupport.isEnabled = true
                        launchGooglePlayReviews()
                    }
                }
            }
        }
        reviewRequestJob?.invokeOnCompletion { reviewRequestJob = null }
    }

    private suspend fun handleInAppReview(reviewInfo: ReviewInfo) {
        helpViewModel.launchReviewFlow(this, reviewInfo).collect { launchResult ->
            binding.fabContactSupport.isEnabled = true
            when (launchResult) {
                ReviewLaunchResult.Success -> {
                    Snackbar.make(binding.root, R.string.snack_feedback, Snackbar.LENGTH_SHORT)
                        .show()
                }

                is ReviewLaunchResult.Error -> launchGooglePlayReviews()
            }
        }
    }

    private fun launchGooglePlayReviews() {
        val uri =
            "https://play.google.com/store/apps/details?id=$packageName&showAllReviews=true".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            startActivity(intent)
        }.onFailure {
            Snackbar.make(
                binding.root,
                R.string.snack_unable_to_open_google_play_store,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun bindFaqItems() {
        val faqList = binding.faqList
        faqList.removeAllViews()
        val inflater = LayoutInflater.from(this)
        FAQ_ITEMS.forEachIndexed { index, item ->
            val itemBinding = ItemHelpFaqBinding.inflate(inflater, faqList, false)
            itemBinding.question.setText(item.questionResId)
            itemBinding.answer.setText(item.answerResId)
            itemBinding.answer.isVisible = false
            itemBinding.toggleIcon.rotation = 0f

            val questionText = itemBinding.question.text
            itemBinding.root.contentDescription = questionText
            itemBinding.questionContainer.contentDescription = questionText

            val collapsedStateDescription = getString(R.string.faq_state_collapsed)
            ViewCompat.setStateDescription(itemBinding.root, collapsedStateDescription)
            ViewCompat.setStateDescription(itemBinding.questionContainer, collapsedStateDescription)

            val toggleListener = View.OnClickListener { toggleFaqItem(itemBinding) }
            itemBinding.root.setOnClickListener(toggleListener)
            itemBinding.questionContainer.setOnClickListener(toggleListener)
            itemBinding.toggleIcon.setOnClickListener(toggleListener)

            itemBinding.divider.isVisible = index != FAQ_ITEMS.lastIndex
            faqList.addView(itemBinding.root)
        }
    }

    private fun toggleFaqItem(itemBinding: ItemHelpFaqBinding) {
        val expand = !itemBinding.answer.isVisible
        itemBinding.answer.isVisible = expand

        val rotation = if (expand) 180f else 0f
        itemBinding.toggleIcon.animate().cancel()
        itemBinding.toggleIcon.animate().rotation(rotation)
            .setDuration(TOGGLE_ANIMATION_DURATION_MS).start()

        val stateRes = if (expand) R.string.faq_state_expanded else R.string.faq_state_collapsed
        val stateDescription = getString(stateRes)
        ViewCompat.setStateDescription(itemBinding.root, stateDescription)
        ViewCompat.setStateDescription(itemBinding.questionContainer, stateDescription)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        reviewRequestJob?.cancel()
        reviewRequestJob = null
        super.onDestroy()
    }

    companion object {
        private const val FAB_AUTO_SHRINK_DELAY_MS = 5000L
        private const val TOGGLE_ANIMATION_DURATION_MS = 200L

        private val FAQ_ITEMS = listOf(
            FaqItem(R.string.question_1, R.string.summary_preference_faq_1),
            FaqItem(R.string.question_2, R.string.summary_preference_faq_2),
            FaqItem(R.string.question_3, R.string.summary_preference_faq_3),
            FaqItem(R.string.question_4, R.string.summary_preference_faq_4),
            FaqItem(R.string.question_5, R.string.summary_preference_faq_5),
            FaqItem(R.string.question_6, R.string.summary_preference_faq_6),
            FaqItem(R.string.question_7, R.string.summary_preference_faq_7),
            FaqItem(R.string.question_8, R.string.summary_preference_faq_8),
            FaqItem(R.string.question_9, R.string.summary_preference_faq_9)
        )
    }

    private data class FaqItem(
        @param:StringRes val questionResId: Int,
        @param:StringRes val answerResId: Int
    )
}
