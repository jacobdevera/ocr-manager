package com.gjdevera.ocrreader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CaptureDbHelper extends SQLiteOpenHelper {
    public CaptureDbHelper(Context context) {
        super(context, CaptureContract.DB_NAME, null, CaptureContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + CaptureContract.CaptureEntry.TABLE + " ( " +
                CaptureContract.CaptureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CaptureContract.CaptureEntry.COL_TEXT + " TEXT NOT NULL, " +
                CaptureContract.CaptureEntry.COL_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CaptureContract.CaptureEntry.TABLE);
        onCreate(db);
    }
}
