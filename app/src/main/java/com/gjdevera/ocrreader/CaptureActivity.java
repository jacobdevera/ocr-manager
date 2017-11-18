package com.gjdevera.ocrreader;

import android.app.Activity;
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
    private boolean newCapture;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        mHelper = new CaptureDbHelper(this);
        Intent intent = getIntent();
        String result = intent.getStringExtra("text");
        newCapture = intent.getBooleanExtra("newCapture", false);
        if (!newCapture) {
            id = intent.getLongExtra("id", -1);
        }
        EditText captureEditText = (EditText) findViewById(R.id.editText);
        captureEditText.setText(result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_capture, menu);
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
                if (newCapture) { // new capture
                    db.insertWithOnConflict(CaptureContract.CaptureEntry.TABLE,
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_REPLACE);
                } else { // update existing capture
                    String selection = CaptureContract.CaptureEntry._ID + " = ?";
                    String[] selectionArgs = { Long.toString(id) };
                    db.update(CaptureContract.CaptureEntry.TABLE, values, selection, selectionArgs);
                }
                db.close();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
