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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.gjdevera.ocrreader.db.Capture;
import com.gjdevera.ocrreader.db.CaptureContract;
import com.gjdevera.ocrreader.db.CaptureDbHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActionMode.Callback {
    private CaptureDbHelper mHelper;
    private List<Capture> captureList;
    private ActionMode actionMode;
    private CaptureViewAdapter mAdapter;
    private boolean isMultiSelect;
    private List<Long> selectedIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent().setClass(getApplicationContext(), OcrCaptureActivity.class));
            }
        });
        mHelper = new CaptureDbHelper(this);
        isMultiSelect = false;
        selectedIds = new ArrayList<>();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        updateCaptures();

        mAdapter = new CaptureViewAdapter(this, captureList);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(this, mRecyclerView, new RecyclerItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect){
                    //if multiple selection is enabled then select item on single click else perform normal click on item
                    multiSelect(position);
                } else {
                    Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
                    Capture capture = captureList.get(position);
                    intent.putExtra("text", capture.getText());
                    intent.putExtra("id", capture.getId());
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect){
                    selectedIds = new ArrayList<>();
                    isMultiSelect = true;
                    if (actionMode == null){
                        actionMode = startActionMode(MainActivity.this); //show ActionMode.
                    }
                }
                multiSelect(position);
            }
        }));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCaptures();
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
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        cursor.close();
        db.close();
    }

    private void multiSelect(int position) {
        Capture capture = captureList.get(position);
        if (capture != null){
            if (actionMode != null) {
                if (selectedIds.contains(capture.getId()))
                    selectedIds.remove(Long.valueOf(capture.getId()));
                else
                    selectedIds.add(capture.getId());
                if (selectedIds.size() > 0)
                    actionMode.setTitle(String.valueOf(selectedIds.size())); // show selected item count on action mode
                else {
                    actionMode.setTitle(""); // remove item count from action mode
                    actionMode.finish(); // hide action mode
                }
                mAdapter.setSelectedIds(selectedIds);
            }
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_select, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.action_delete:
                StringBuilder stringBuilder = new StringBuilder();
                for (Capture data : captureList) {
                    if (selectedIds.contains(data.getId()))
                        stringBuilder.append("\n").append(data.getText());
                }
                Toast.makeText(this, "Selected items are :" + stringBuilder.toString(), Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        isMultiSelect = false;
        selectedIds = new ArrayList<>();
        mAdapter.setSelectedIds(new ArrayList<Long>());
    }

}
