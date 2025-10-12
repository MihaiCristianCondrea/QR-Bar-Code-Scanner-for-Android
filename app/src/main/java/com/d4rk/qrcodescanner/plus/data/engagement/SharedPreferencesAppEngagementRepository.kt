package com.d4rk.qrcodescanner.plus.data.engagement

import android.content.Context
import androidx.core.content.edit
import com.d4rk.qrcodescanner.plus.domain.engagement.AppEngagementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val PREFERENCES_NAME = "app_engagement_preferences"
private const val KEY_SESSION_COUNT = "session_count"
private const val KEY_HAS_PROMPTED_REVIEW = "has_prompted_review"

class SharedPreferencesAppEngagementRepository(
    context: Context,
) : AppEngagementRepository {

    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val lock = Mutex()

    private val sessionCountState = MutableStateFlow(preferences.getInt(KEY_SESSION_COUNT, 0))
    private val hasPromptedReviewState = MutableStateFlow(
        preferences.getBoolean(KEY_HAS_PROMPTED_REVIEW, false)
    )

    override val sessionCount: Flow<Int> = sessionCountState.asStateFlow()

    override val hasPromptedReview: Flow<Boolean> = hasPromptedReviewState.asStateFlow()

    override suspend fun incrementSessionCount() {
        lock.withLock {
            val nextCount = sessionCountState.value + 1
            preferences.edit { putInt(KEY_SESSION_COUNT, nextCount) }
            sessionCountState.value = nextCount
        }
    }

    override suspend fun setHasPromptedReview(hasPrompted: Boolean) {
        lock.withLock {
            if (hasPromptedReviewState.value == hasPrompted) {
                return
            }
            preferences.edit { putBoolean(KEY_HAS_PROMPTED_REVIEW, hasPrompted) }
            hasPromptedReviewState.value = hasPrompted
        }
    }
}
