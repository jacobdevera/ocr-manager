package com.gjdevera.ocrreader;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.gjdevera.ocrreader.db.CaptureContract;
import com.gjdevera.ocrreader.db.CaptureDbHelper;

public class CaptureActivity extends AppCompatActivity {
    private static final String TAG = "CaptureActivity";
    private CaptureDbHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        mHelper = new CaptureDbHelper(this);
        Intent intent = getIntent();
        String result = intent.getStringExtra("text");
        EditText captureEditText = (EditText) findViewById(R.id.editText);
        captureEditText.setText(result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.capture_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_capture:
                EditText captureEditText = (EditText) findViewById(R.id.editText);
                String text = String.valueOf(captureEditText.getText());
                Log.d(TAG, "Saved OCR capture");
                SQLiteDatabase db = mHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(CaptureContract.CaptureEntry.COL_TEXT, text);
                db.insertWithOnConflict(CaptureContract.CaptureEntry.TABLE,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                db.close();
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
