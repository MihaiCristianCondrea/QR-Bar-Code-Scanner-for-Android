package com.d4rk.qrcodescanner.plus.domain.engagement

import kotlinx.coroutines.flow.Flow

/**
 * Repository that tracks lightweight engagement state used for Play Store integrations.
 */
interface AppEngagementRepository {
    /**
     * Emits the number of app sessions recorded so far.
     */
    val sessionCount: Flow<Int>

    /**
     * Emits whether the in-app review dialog has already been prompted.
     */
    val hasPromptedReview: Flow<Boolean>

    /**
     * Increments the persisted session counter by one.
     */
    suspend fun incrementSessionCount()

    /**
     * Persists whether the in-app review flow has been shown to the user.
     */
    suspend fun setHasPromptedReview(hasPrompted: Boolean)
}
