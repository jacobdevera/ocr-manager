package com.gjdevera.ocrreader.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CaptureDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";
    public CaptureDbHelper(Context context) {
        super(context, CaptureContract.DB_NAME, null, CaptureContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + CaptureContract.CaptureEntry.TABLE + " ( " +
                CaptureContract.CaptureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CaptureContract.CaptureEntry.COL_TEXT + " TEXT NOT NULL, " +
                CaptureContract.CaptureEntry.COL_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                CaptureContract.CaptureEntry.COL_PATH + " TEXT)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CaptureContract.CaptureEntry.TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CaptureContract.CaptureEntry.TABLE);
        onCreate(db);
    }

    public String getTableAsString(SQLiteDatabase db, String tableName) {
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }
}
