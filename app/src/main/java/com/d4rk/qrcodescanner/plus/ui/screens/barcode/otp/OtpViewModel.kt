package com.d4rk.qrcodescanner.plus.ui.screens.barcode.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.domain.create.OTPGenerator
import com.d4rk.qrcodescanner.plus.model.schema.OtpAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class OtpViewModel(
    initialOtp: OtpAuth,
    private val otpGenerator: OTPGenerator,
    private val timerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val otpState = MutableStateFlow(initialOtp)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val timerStateFlow: Flow<TimerUiState> = otpState.flatMapLatest { otp ->
        timerFlowFor(otp)
    }

    private val initialUiState = buildUiState(initialOtp, timerStateFor(initialOtp))

    val uiState: StateFlow<OtpUiState> = combine(otpState, timerStateFlow) { otp, timerState ->
        buildUiState(otp, timerState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = initialUiState
    )

    fun refreshOtp() {
        val current = otpState.value
        if (current.type != OtpAuth.HOTP_TYPE) {
            return
        }
        otpState.update { otp ->
            val nextCounter = otp.counter?.plus(1) ?: 1L
            otp.copy(counter = nextCounter)
        }
    }

    private fun timerFlowFor(otp: OtpAuth): Flow<TimerUiState> {
        if (otp.type != OtpAuth.TOTP_TYPE) {
            return flowOf(TimerUiState.Hidden)
        }
        val safePeriod = (otp.period ?: DEFAULT_PERIOD).toInt().coerceAtLeast(1)
        return flow<TimerUiState> {
            var secondsLeft = secondsUntilNextPeriod(safePeriod)
            emit(visibleTimer(secondsLeft))
            while (true) {
                delay(TICK_INTERVAL_MS)
                secondsLeft -= 1
                if (secondsLeft < 0) {
                    secondsLeft = secondsUntilNextPeriod(safePeriod)
                }
                emit(visibleTimer(secondsLeft))
            }
        }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                emit(TimerUiState.Hidden)
            }
            .flowOn(timerDispatcher)
    }

    private fun secondsUntilNextPeriod(period: Int): Long {
        val currentTimeInSeconds = System.currentTimeMillis() / ONE_SECOND_IN_MILLIS
        val secondsPassed = (currentTimeInSeconds % period).toInt()
        return (period - secondsPassed).toLong()
    }

    private fun visibleTimer(secondsLeft: Long): TimerUiState.Visible {
        val safeSeconds = secondsLeft.coerceAtLeast(0L)
        val minutes = safeSeconds / 60
        val seconds = safeSeconds % 60
        return TimerUiState.Visible(
            totalSeconds = safeSeconds,
            minutes = minutes,
            seconds = seconds
        )
    }

    private fun buildUiState(otp: OtpAuth, timerState: TimerUiState): OtpUiState {
        val password = runCatching { otpGenerator.generateOTP(otp) }.getOrNull()
        return OtpUiState(
            otp = otp,
            password = password,
            timerState = timerState
        )
    }

    private fun timerStateFor(otp: OtpAuth): TimerUiState {
        return if (otp.type == OtpAuth.TOTP_TYPE) {
            val safePeriod = (otp.period ?: DEFAULT_PERIOD).toInt().coerceAtLeast(1)
            visibleTimer(secondsUntilNextPeriod(safePeriod))
        } else {
            TimerUiState.Hidden
        }
    }

    companion object {
        private const val DEFAULT_PERIOD = 30
        private const val TICK_INTERVAL_MS = 1_000L
        private const val ONE_SECOND_IN_MILLIS = 1_000L
    }
}

data class OtpUiState(
    val otp: OtpAuth,
    val password: String?,
    val timerState: TimerUiState
) {
    val isHotp: Boolean = otp.type == OtpAuth.HOTP_TYPE
    val isTotp: Boolean = otp.type == OtpAuth.TOTP_TYPE
    val counter: Long? = otp.counter
}

sealed interface TimerUiState {
    data object Hidden : TimerUiState
    data class Visible(
        val totalSeconds: Long,
        val minutes: Long,
        val seconds: Long
    ) : TimerUiState
}

class OtpViewModelFactory(
    private val initialOtp: OtpAuth,
    private val otpGenerator: OTPGenerator,
    private val timerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OtpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OtpViewModel(initialOtp, otpGenerator, timerDispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
