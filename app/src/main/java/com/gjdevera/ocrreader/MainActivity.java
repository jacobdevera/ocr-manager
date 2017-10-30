package com.gjdevera.ocrreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gjdevera.ocrreader.db.CaptureContract;
import com.gjdevera.ocrreader.db.CaptureDbHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private CaptureDbHelper mHelper;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<String> captureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(this);
        mHelper = new CaptureDbHelper(this);
        getCaptures();
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
            intent.putExtra("text", captureList.get(getAdapterPosition()));
            startActivity(intent);
            getCaptures();
        }
    }

    private void getCaptures () {
        captureList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(CaptureContract.CaptureEntry.TABLE,
                new String[]{CaptureContract.CaptureEntry._ID, CaptureContract.CaptureEntry.COL_TEXT},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(CaptureContract.CaptureEntry.COL_TEXT);
            captureList.add(cursor.getString(idx));
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
                    tv.setText(captureList.get(position));
                    tv = (TextView) vh.itemView.findViewById(R.id.text2);
                    tv.setText("" + position);
                }

                @Override
                public int getItemCount() {
                    return captureList.size(); // db size
                }
            };
            /*DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                    mLayoutManager.getOrientation());*
            mRecyclerView.addItemDecoration(dividerItemDecoration);*/
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter = mRecyclerView.getAdapter();
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent().setClass(getApplicationContext(), OcrCaptureActivity.class));
    }
}
