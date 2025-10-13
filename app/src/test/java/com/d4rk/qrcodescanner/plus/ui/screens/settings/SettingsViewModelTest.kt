package com.d4rk.qrcodescanner.plus.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import app.cash.turbine.test
import com.d4rk.qrcodescanner.plus.domain.main.BottomNavigationLabelsPreference
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferences
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.main.StartDestinationPreference
import com.d4rk.qrcodescanner.plus.domain.main.ThemePreference
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @Test
    fun `ui state reflects preferences emitted by repository`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeMainPreferencesRepository(
            flow {
                emit(
                    MainPreferences(
                        theme = ThemePreference.DARK,
                        languageTag = "es",
                        bottomNavigationLabels = BottomNavigationLabelsPreference.SELECTED,
                        startDestination = StartDestinationPreference.CREATE
                    )
                )
            }
        )

        val viewModel = SettingsViewModel(repository, dispatcher)

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(SettingsUiState())
            advanceUntilIdle()
            assertThat(
                awaitItem()
            ).isEqualTo(
                SettingsUiState(
                    themeMode = AppCompatDelegate.MODE_NIGHT_YES,
                    languageTag = "es"
                )
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ui state falls back to defaults when repository throws`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeMainPreferencesRepository(
            flow {
                emit(
                    MainPreferences(
                        theme = ThemePreference.LIGHT,
                        languageTag = "fr",
                        bottomNavigationLabels = BottomNavigationLabelsPreference.LABELED,
                        startDestination = StartDestinationPreference.SCAN
                    )
                )
                throw IllegalStateException("boom")
            }
        )

        val viewModel = SettingsViewModel(repository, dispatcher)

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(SettingsUiState())
            advanceUntilIdle()
            assertThat(
                awaitItem()
            ).isEqualTo(
                SettingsUiState(
                    themeMode = AppCompatDelegate.MODE_NIGHT_NO,
                    languageTag = "fr"
                )
            )
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(SettingsUiState())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeMainPreferencesRepository(
        override val mainPreferences: Flow<MainPreferences>
    ) : MainPreferencesRepository
}
