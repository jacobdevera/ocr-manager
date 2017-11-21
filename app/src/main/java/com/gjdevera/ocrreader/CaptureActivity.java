package com.gjdevera.ocrreader;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.gjdevera.ocrreader.db.CaptureContract;
import com.gjdevera.ocrreader.db.CaptureDbHelper;
import com.gjdevera.ocrreader.db.ImgHelper;

public class CaptureActivity extends AppCompatActivity {
    private static final String TAG = "CaptureActivity";
    private CaptureDbHelper mHelper;
    private EditText editText;
    private boolean newCapture;
    private String path;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        mHelper = new CaptureDbHelper(this);
        Intent intent = getIntent();
        String result = intent.getStringExtra("text");
        newCapture = intent.getBooleanExtra("newCapture", false);
        path = intent.getStringExtra("path");
        if (!newCapture) {
            id = intent.getLongExtra("id", -1);
        }
        editText = (EditText) findViewById(R.id.editText);
        editText.setText(result);

        ImageView imageView = findViewById(R.id.imageView);
        Bitmap bmp = ImgHelper.getCorrectedImageOrientation(path);
        imageView.setImageBitmap(bmp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_capture, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String text = String.valueOf(editText.getText());
        switch (item.getItemId()) {
            case R.id.action_save_capture:
                Log.d(TAG, "Saved OCR capture");
                SQLiteDatabase db = mHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(CaptureContract.CaptureEntry.COL_TEXT, text);
                values.put(CaptureContract.CaptureEntry.COL_PATH, path);

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

            case R.id.action_share_capture:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");

                // Verify that the intent will resolve to an activity
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
