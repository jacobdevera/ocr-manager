package com.gjdevera.ocrreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gjdevera.ocrreader.db.Capture;
import com.gjdevera.ocrreader.db.CaptureContract;
import com.gjdevera.ocrreader.db.CaptureDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private CaptureDbHelper mHelper;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<Capture> captureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(this);
        mHelper = new CaptureDbHelper(this);
        updateCaptures();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCaptures();
    }

    private class CaptureViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private CaptureViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), CaptureActivity.class);
            Capture capture = captureList.get(getAdapterPosition());
            intent.putExtra("text", capture.getText());
            intent.putExtra("id", capture.getId());
            startActivity(intent);
        }
    }

    private void updateCaptures() {
        captureList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(CaptureContract.CaptureEntry.TABLE,
                new String[]{CaptureContract.CaptureEntry._ID,
                        CaptureContract.CaptureEntry.COL_TEXT,
                        CaptureContract.CaptureEntry.COL_CREATED},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int textIdx = cursor.getColumnIndex(CaptureContract.CaptureEntry.COL_TEXT);
            int idIdx =  cursor.getColumnIndex(CaptureContract.CaptureEntry._ID);
            int createdIdx = cursor.getColumnIndex(CaptureContract.CaptureEntry.COL_CREATED);
            Capture capture = new Capture(cursor.getLong(idIdx),
                    cursor.getString(textIdx), cursor.getString(createdIdx));
            captureList.add(capture);
        }

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.Adapter<CaptureViewHolder> mAdapter;
        if (mRecyclerView.getLayoutManager() == null) {
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new RecyclerView.Adapter<CaptureViewHolder>() {

                @Override
                public CaptureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.capture_row,
                            parent,
                            false);
                    return new CaptureViewHolder(v);
                }

                @Override
                public void onBindViewHolder(CaptureViewHolder vh, int position) {
                    TextView tv = (TextView) vh.itemView.findViewById(R.id.text1);
                    Capture capture = captureList.get(position);
                    String s = capture.getText();
                    s = s.replace("\n"," ");
                    s = s.substring(0, Math.min(s.length(), 50));
                    tv.setText(s);
                    tv = (TextView) vh.itemView.findViewById(R.id.text2);
                    tv.setText(getDate(capture.getCreated()));
                }

                @Override
                public int getItemCount() {
                    return captureList.size(); // db size
                }
            };
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                    mLayoutManager.getOrientation());
            mRecyclerView.addItemDecoration(dividerItemDecoration);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter = mRecyclerView.getAdapter();
            mAdapter.notifyDataSetChanged();
        }
        cursor.close();
        db.close();
    }

    // attempt to format UTC date to user's local time
    private String getDate(String date) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = formatter.parse(date);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
            dateFormatter.setTimeZone(TimeZone.getDefault());
            date = dateFormatter.format(value);
        }
        catch (Exception e) {
            date = "00-00-0000 00:00";
        }
        return date;
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent().setClass(getApplicationContext(), OcrCaptureActivity.class));
    }
}
