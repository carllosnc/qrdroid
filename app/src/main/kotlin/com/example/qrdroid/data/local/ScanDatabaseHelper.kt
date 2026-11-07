package com.example.qrdroid.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.qrdroid.data.model.ScanItem
import com.example.qrdroid.data.model.ScanType

class ScanDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "qrdroid_scans.db"
        const val DATABASE_VERSION = 1

        const val TABLE_SCANS = "scans"
        const val COLUMN_ID = "id"
        const val COLUMN_RAW_VALUE = "raw_value"
        const val COLUMN_DISPLAY_VALUE = "display_value"
        const val COLUMN_FORMAT_NAME = "format_name"
        const val COLUMN_FORMAT_CODE = "format_code"
        const val COLUMN_VALUE_TYPE = "value_type"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_IS_FAVORITE = "is_favorite"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_SCANS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RAW_VALUE TEXT NOT NULL,
                $COLUMN_DISPLAY_VALUE TEXT NOT NULL,
                $COLUMN_FORMAT_NAME TEXT NOT NULL,
                $COLUMN_FORMAT_CODE INTEGER NOT NULL,
                $COLUMN_VALUE_TYPE TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_IS_FAVORITE INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_scans_timestamp ON $TABLE_SCANS ($COLUMN_TIMESTAMP DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migration logic if schema changes in future versions
    }

    fun insertScan(item: ScanItem): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RAW_VALUE, item.rawValue)
            put(COLUMN_DISPLAY_VALUE, item.displayValue)
            put(COLUMN_FORMAT_NAME, item.formatName)
            put(COLUMN_FORMAT_CODE, item.formatCode)
            put(COLUMN_VALUE_TYPE, item.valueType.name)
            put(COLUMN_TIMESTAMP, item.timestamp)
            put(COLUMN_IS_FAVORITE, if (item.isFavorite) 1 else 0)
        }
        return db.insert(TABLE_SCANS, null, values)
    }

    fun getAllScans(): List<ScanItem> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SCANS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )
        return cursor.use { cursorToScanList(it) }
    }

    fun toggleFavorite(id: Long, isFavorite: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_FAVORITE, if (isFavorite) 1 else 0)
        }
        return db.update(TABLE_SCANS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteScan(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_SCANS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun clearAllScans(): Int {
        val db = writableDatabase
        return db.delete(TABLE_SCANS, null, null)
    }

    fun searchScans(query: String): List<ScanItem> {
        val db = readableDatabase
        val pattern = "%$query%"
        val cursor = db.query(
            TABLE_SCANS,
            null,
            "$COLUMN_DISPLAY_VALUE LIKE ? OR $COLUMN_RAW_VALUE LIKE ? OR $COLUMN_FORMAT_NAME LIKE ?",
            arrayOf(pattern, pattern, pattern),
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )
        return cursor.use { cursorToScanList(it) }
    }

    private fun cursorToScanList(cursor: Cursor): List<ScanItem> {
        val list = mutableListOf<ScanItem>()
        val idIdx = cursor.getColumnIndexOrThrow(COLUMN_ID)
        val rawIdx = cursor.getColumnIndexOrThrow(COLUMN_RAW_VALUE)
        val displayIdx = cursor.getColumnIndexOrThrow(COLUMN_DISPLAY_VALUE)
        val formatNameIdx = cursor.getColumnIndexOrThrow(COLUMN_FORMAT_NAME)
        val formatCodeIdx = cursor.getColumnIndexOrThrow(COLUMN_FORMAT_CODE)
        val valueTypeIdx = cursor.getColumnIndexOrThrow(COLUMN_VALUE_TYPE)
        val timestampIdx = cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)
        val isFavoriteIdx = cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)

        while (cursor.moveToNext()) {
            val typeStr = cursor.getString(valueTypeIdx)
            val type = try {
                ScanType.valueOf(typeStr)
            } catch (e: Exception) {
                ScanType.TEXT
            }
            list.add(
                ScanItem(
                    id = cursor.getLong(idIdx),
                    rawValue = cursor.getString(rawIdx),
                    displayValue = cursor.getString(displayIdx),
                    formatName = cursor.getString(formatNameIdx),
                    formatCode = cursor.getInt(formatCodeIdx),
                    valueType = type,
                    timestamp = cursor.getLong(timestampIdx),
                    isFavorite = cursor.getInt(isFavoriteIdx) == 1
                )
            )
        }
        return list
    }
}
