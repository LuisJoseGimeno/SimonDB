package com.example.simon

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns

object PlayerContract {
    object PlayerEntry : BaseColumns {
        const val TABLE_NAME = "players"
        const val COLUMN_NAME = "name"
        const val COLUMN_SCORE = "score"
    }
}

class PlayerDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "PlayerDatabase.db"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${PlayerContract.PlayerEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${PlayerContract.PlayerEntry.COLUMN_NAME} TEXT," +
                    "${PlayerContract.PlayerEntry.COLUMN_SCORE} INTEGER)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${PlayerContract.PlayerEntry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
}
