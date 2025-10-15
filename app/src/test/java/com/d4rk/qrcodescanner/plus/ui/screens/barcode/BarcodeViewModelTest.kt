package com.d4rk.qrcodescanner.plus.ui.screens.barcode

import app.cash.turbine.test
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeDetailsRepository
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.model.schema.Url
import com.google.zxing.BarcodeFormat
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BarcodeViewModelTest {

    private val repository: BarcodeDetailsRepository = mockk(relaxUnitFun = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        clearMocks(repository)
        Dispatchers.resetMain()
    }

    @Test
    fun `getUiState returns initial state correctly`() = runTest {
        val initialBarcode = createBarcode(id = 42L, name = "My barcode")

        val viewModel = createViewModel(initialBarcode)

        val state = viewModel.uiState.value

        assertEquals(initialBarcode, state.barcode)
        assertEquals(initialBarcode.name, state.parsedBarcode.name)
        assertTrue(state.isInDatabase)
        assertFalse(state.isProcessing)
        assertFalse(state.isDeleting)
    }

    @Test
    fun `getEvents returns the shared flow`() = runTest {
        val viewModel = createViewModel()

        val events = viewModel.events

        assertNotNull(events)
        assertIs<SharedFlow<BarcodeEvent>>(events)
    }

    @Test
    fun `deleteBarcode successful deletion`() = runTest {
        val barcode = createBarcode(id = 5L)
        val viewModel = createViewModel(barcode)
        every { repository.deleteBarcode(barcode.id) } returns flow { emit(Unit) }

        val states = mutableListOf<Boolean>()
        val job = backgroundScope.launch {
            viewModel.uiState.map { it.isDeleting }.take(3).toList(states)
        }

        viewModel.events.test {
            viewModel.deleteBarcode()

            assertEquals(BarcodeEvent.BarcodeDeleted, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        job.join()
        assertEquals(listOf(false, true, false), states)

        verify(exactly = 1) { repository.deleteBarcode(barcode.id) }
    }

    @Test
    fun `deleteBarcode on barcode not in database`() = runTest {
        val barcode = createBarcode(id = 0L)
        val viewModel = createViewModel(barcode)

        viewModel.events.test {
            viewModel.deleteBarcode()
            expectNoEvents()
        }

        verify(exactly = 0) { repository.deleteBarcode(any()) }
    }

    @Test
    fun `deleteBarcode repository throws error`() = runTest {
        val barcode = createBarcode(id = 9L)
        val viewModel = createViewModel(barcode)
        val error = IllegalStateException("boom")
        every { repository.deleteBarcode(barcode.id) } returns flow { throw error }

        viewModel.events.test {
            viewModel.deleteBarcode()
            val errorEvent = assertIs<BarcodeEvent.Error>(awaitItem())
            assertEquals(error, errorEvent.throwable)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(viewModel.uiState.value.isDeleting)
        verify(exactly = 1) { repository.deleteBarcode(barcode.id) }
    }

    @Test
    fun `updateName successful update`() = runTest {
        val barcode = createBarcode(id = 11L, name = "Initial")
        val trimmedName = "Updated"
        val viewModel = createViewModel(barcode)
        every { repository.saveBarcode(any(), any()) } returns flow { emit(barcode.id) }

        viewModel.events.test {
            viewModel.updateName("  $trimmedName  ")

            val event = awaitItem()
            assertEquals(BarcodeEvent.NameUpdated(trimmedName), event)
            cancelAndIgnoreRemainingEvents()
        }

        val updated = viewModel.uiState.value
        assertEquals(trimmedName, updated.barcode.name)
        assertEquals(trimmedName, updated.parsedBarcode.name)
        assertFalse(updated.isProcessing)
        verify(exactly = 1) {
            repository.saveBarcode(match { it.name == trimmedName && it.id == barcode.id }, false)
        }
    }

    @Test
    fun `updateName with empty or blank name`() = runTest {
        val barcode = createBarcode(id = 10L, name = "Original")
        val viewModel = createViewModel(barcode)

        viewModel.events.test {
            viewModel.updateName("   ")
            expectNoEvents()
        }

        assertEquals(barcode, viewModel.uiState.value.barcode)
        verify(exactly = 0) { repository.saveBarcode(any(), any()) }
    }

    @Test
    fun `updateName on barcode not in database`() = runTest {
        val barcode = createBarcode(id = 0L)
        val viewModel = createViewModel(barcode)

        viewModel.events.test {
            viewModel.updateName("Name")
            expectNoEvents()
        }

        verify(exactly = 0) { repository.saveBarcode(any(), any()) }
    }

    @Test
    fun `updateName repository throws error`() = runTest {
        val barcode = createBarcode(id = 7L, name = "Old")
        val viewModel = createViewModel(barcode)
        val error = RuntimeException("failure")
        every { repository.saveBarcode(any(), any()) } returns flow { throw error }

        viewModel.events.test {
            viewModel.updateName("New")
            val errorEvent = assertIs<BarcodeEvent.Error>(awaitItem())
            assertEquals(error, errorEvent.throwable)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(viewModel.uiState.value.isProcessing)
        verify(exactly = 1) { repository.saveBarcode(any(), false) }
    }

    @Test
    fun `saveBarcode successful save with avoidDuplicates true`() = runTest {
        val barcode = createBarcode(id = 0L)
        val viewModel = createViewModel(barcode)
        val savedId = 33L
        every { repository.saveBarcode(any(), true) } returns flow { emit(savedId) }

        viewModel.events.test {
            viewModel.saveBarcode(avoidDuplicates = true)

            val savedEvent = assertIs<BarcodeEvent.BarcodeSaved>(awaitItem())
            assertEquals(savedId, savedEvent.barcode.id)
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value
        assertEquals(savedId, state.barcode.id)
        assertTrue(state.isInDatabase)
        assertFalse(state.isProcessing)
        verify(exactly = 1) { repository.saveBarcode(barcode, true) }
    }

    @Test
    fun `saveBarcode successful save with avoidDuplicates false`() = runTest {
        val barcode = createBarcode(id = 0L)
        val viewModel = createViewModel(barcode)
        val savedId = 17L
        every { repository.saveBarcode(any(), false) } returns flow { emit(savedId) }

        viewModel.events.test {
            viewModel.saveBarcode(avoidDuplicates = false)

            val savedEvent = assertIs<BarcodeEvent.BarcodeSaved>(awaitItem())
            assertEquals(savedId, savedEvent.barcode.id)
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { repository.saveBarcode(barcode, false) }
    }

    @Test
    fun `saveBarcode on barcode already in database`() = runTest {
        val barcode = createBarcode(id = 4L)
        val viewModel = createViewModel(barcode)

        viewModel.events.test {
            viewModel.saveBarcode(avoidDuplicates = true)
            expectNoEvents()
        }

        verify(exactly = 0) { repository.saveBarcode(any(), any()) }
    }

    @Test
    fun `saveBarcode repository throws error`() = runTest {
        val barcode = createBarcode(id = 0L)
        val viewModel = createViewModel(barcode)
        val error = IllegalArgumentException("save failed")
        every { repository.saveBarcode(any(), any()) } returns flow { throw error }

        viewModel.events.test {
            viewModel.saveBarcode(avoidDuplicates = false)
            val errorEvent = assertIs<BarcodeEvent.Error>(awaitItem())
            assertEquals(error, errorEvent.throwable)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(viewModel.uiState.value.isProcessing)
        verify(exactly = 1) { repository.saveBarcode(barcode, false) }
    }

    @Test
    fun `toggleFavorite from false to true`() = runTest {
        val barcode = createBarcode(id = 2L, isFavorite = false)
        val viewModel = createViewModel(barcode)
        every { repository.saveBarcode(any(), false) } returns flow { emit(barcode.id) }

        viewModel.events.test {
            viewModel.toggleFavorite()
            val event = awaitItem()
            assertEquals(BarcodeEvent.FavoriteToggled(true), event)
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value
        assertTrue(state.barcode.isFavorite)
        verify(exactly = 1) {
            repository.saveBarcode(match { it.isFavorite && it.id == barcode.id }, false)
        }
    }

    @Test
    fun `toggleFavorite from true to false`() = runTest {
        val barcode = createBarcode(id = 3L, isFavorite = true)
        val viewModel = createViewModel(barcode)
        every { repository.saveBarcode(any(), false) } returns flow { emit(barcode.id) }

        viewModel.events.test {
            viewModel.toggleFavorite()
            val event = awaitItem()
            assertEquals(BarcodeEvent.FavoriteToggled(false), event)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(viewModel.uiState.value.barcode.isFavorite)
        verify(exactly = 1) {
            repository.saveBarcode(match { !it.isFavorite && it.id == barcode.id }, false)
        }
    }

    @Test
    fun `toggleFavorite on barcode not in database`() = runTest {
        val barcode = createBarcode(id = 0L)
        val viewModel = createViewModel(barcode)

        viewModel.events.test {
            viewModel.toggleFavorite()
            expectNoEvents()
        }

        verify(exactly = 0) { repository.saveBarcode(any(), any()) }
    }

    @Test
    fun `toggleFavorite repository throws error`() = runTest {
        val barcode = createBarcode(id = 12L, isFavorite = false)
        val viewModel = createViewModel(barcode)
        val error = IllegalStateException("favorite failure")
        every { repository.saveBarcode(any(), any()) } returns flow { throw error }

        viewModel.events.test {
            viewModel.toggleFavorite()
            val errorEvent = assertIs<BarcodeEvent.Error>(awaitItem())
            assertEquals(error, errorEvent.throwable)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(viewModel.uiState.value.isProcessing)
        verify(exactly = 1) { repository.saveBarcode(any(), false) }
    }

    private fun createViewModel(
        initialBarcode: Barcode = createBarcode(),
        repository: BarcodeDetailsRepository = this.repository
    ): BarcodeViewModel {
        return BarcodeViewModel(initialBarcode, repository)
    }

    private fun createBarcode(
        id: Long = 0L,
        name: String? = null,
        isFavorite: Boolean = false
    ): Barcode {
        val schema = Url("https://example.com")
        return Barcode(
            id = id,
            name = name,
            text = schema.toBarcodeText(),
            formattedText = schema.toFormattedText(),
            format = BarcodeFormat.QR_CODE,
            schema = BarcodeSchema.URL,
            date = 123456789L,
            isGenerated = false,
            isFavorite = isFavorite
        )
    }
}
