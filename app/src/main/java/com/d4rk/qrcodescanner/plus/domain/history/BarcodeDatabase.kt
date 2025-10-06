package com.d4rk.qrcodescanner.plus.domain.history

import android.content.Context
import androidx.paging.PagingSource // Changed from DataSource.Factory for Paging 3
import androidx.room.TypeConverter
import androidx.room.Room
import androidx.room.Query
import androidx.room.Database
import androidx.room.Dao
import androidx.room.RoomDatabase
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.ExportBarcode
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BarcodeDatabaseTypeConverter {
    @TypeConverter
    fun fromBarcodeFormat(barcodeFormat: BarcodeFormat): String {
        return barcodeFormat.name
    }

    @TypeConverter
    fun toBarcodeFormat(value: String): BarcodeFormat {
        return BarcodeFormat.valueOf(value)
    }

    @TypeConverter
    fun fromBarcodeSchema(barcodeSchema: BarcodeSchema): String {
        return barcodeSchema.name
    }

    @TypeConverter
    fun toBarcodeSchema(value: String): BarcodeSchema {
        return BarcodeSchema.valueOf(value)
    }
}

@Database(entities = [Barcode::class], version = 2)
abstract class BarcodeDatabaseFactory : RoomDatabase() {
    abstract fun getBarcodeDatabase(): BarcodeDatabase

    // It's common to put the companion object for creating the instance in the Database class
    companion object {
        @Volatile
        private var INSTANCE: BarcodeDatabase? = null

        fun getInstance(context: Context): BarcodeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BarcodeDatabaseFactory::class.java, "db"
                )
                    .addMigrations(object : Migration(1, 2) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("ALTER TABLE codes ADD COLUMN name TEXT")
                        }
                    })
                    // It's generally a good practice to allow main thread queries
                    // only for testing or very specific scenarios.
                    // For production, ensure database operations are on background threads.
                    // .allowMainThreadQueries() // Consider if really needed
                    .build()
                    .getBarcodeDatabase()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface BarcodeDatabase {
    // For Paging 3, DAO should return PagingSource
    @Query("SELECT * FROM codes ORDER BY date DESC")
    fun getAll(): PagingSource<Int, Barcode>

    @Query("SELECT * FROM codes WHERE isFavorite = 1 ORDER BY date DESC")
    fun getFavorites(): PagingSource<Int, Barcode> // Or Flow<List<Barcode>> if not using Paging

    // Use Flow for observable queries
    @Query("SELECT date, format, text FROM codes ORDER BY date DESC")
    fun getAllForExport(): Flow<List<ExportBarcode>>

    // Suspend function for one-shot read
    @Query("SELECT * FROM codes WHERE format = :format AND text = :text LIMIT 1")
    suspend fun find(format: String, text: String): List<Barcode> // Returns List or nullable Barcode

    // Suspend function for insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(barcode: Barcode): Long // Returns the new rowId

    // Suspend function for delete
    @Query("DELETE FROM codes WHERE id = :id")
    suspend fun delete(id: Long) // No return value needed for Completable equivalent

    // Suspend function for delete all
    @Query("DELETE FROM codes")
    suspend fun deleteAll()
}

// Extension functions updated to use suspend
suspend fun BarcodeDatabase.save(barcode: Barcode, doNotSaveDuplicates: Boolean): Long {
    return if (doNotSaveDuplicates) {
        saveIfNotPresent(barcode)
    } else {
        // Ensure this call is to the suspend version of save
        save(barcode)
    }
}

suspend fun BarcodeDatabase.saveIfNotPresent(barcode: Barcode): Long {
    // Operations that involve database access should typically run on a background thread.
    // withContext(Dispatchers.IO) is a common way to achieve this.
    return withContext(Dispatchers.IO) {
        val found = find(barcode.format.name, barcode.text)
        if (found.isEmpty()) {
            save(barcode)
        } else {
            found[0].id
        }
    }
}