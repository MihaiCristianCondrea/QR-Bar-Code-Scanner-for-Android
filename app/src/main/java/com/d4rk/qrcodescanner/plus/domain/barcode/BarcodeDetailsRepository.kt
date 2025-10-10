package com.d4rk.qrcodescanner.plus.domain.barcode

import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.history.save
import com.d4rk.qrcodescanner.plus.model.Barcode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class BarcodeDetailsRepository(
    private val database: BarcodeDatabase,
    private val ioDispatcher: CoroutineDispatcher
) {
    fun saveBarcode(barcode: Barcode, avoidDuplicates: Boolean): Flow<Long> {
        return flow {
            val id = database.save(barcode, avoidDuplicates)
            emit(id)
        }.flowOn(ioDispatcher)
    }

    fun deleteBarcode(id: Long): Flow<Unit> {
        return flow {
            database.delete(id)
            emit(Unit)
        }.flowOn(ioDispatcher)
    }
}
