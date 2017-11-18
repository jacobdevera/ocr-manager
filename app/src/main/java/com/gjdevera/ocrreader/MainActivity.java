package com.gjdevera.ocrreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements ActionMode.Callback {
    private static final String TAG = "MainActivity";
    private Snackbar sb;
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
        captureList = new ArrayList<>();
        mAdapter = new CaptureViewAdapter(this, captureList);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(mAdapter);
        updateCaptures();
        mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(this, mRecyclerView, new RecyclerItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect){ // if selecting multiple items
                    multiSelect(position);
                } else { // open capture normally
                    Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
                    Capture capture = captureList.get(position);
                    intent.putExtra("text", capture.getText());
                    intent.putExtra("id", capture.getId());
                    startActivityForResult(intent, 1);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect){
                    selectedIds = new ArrayList<>();
                    isMultiSelect = true;
                    if (actionMode == null){
                        actionMode = startActionMode(MainActivity.this);
                    }
                }
                multiSelect(position);
            }
        }));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sb != null) {
            sb.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCaptures();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            switch (resultCode) {
                case RESULT_OK:
                    sb = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.saved), Snackbar.LENGTH_LONG);
                    sb.show();
            }
        }
    }

    private void updateCaptures() {
        captureList.clear();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(CaptureContract.CaptureEntry.TABLE,
                new String[]{CaptureContract.CaptureEntry._ID,
                        CaptureContract.CaptureEntry.COL_TEXT,
                        CaptureContract.CaptureEntry.COL_CREATED},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idIdx =  cursor.getColumnIndex(CaptureContract.CaptureEntry._ID);
            int textIdx = cursor.getColumnIndex(CaptureContract.CaptureEntry.COL_TEXT);
            int createdIdx = cursor.getColumnIndex(CaptureContract.CaptureEntry.COL_CREATED);
            Capture capture = new Capture(cursor.getLong(idIdx),
                    cursor.getString(textIdx), cursor.getString(createdIdx));
            captureList.add(capture);
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            Log.d(TAG, "dataset changed");
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
                    // show selected item count on action mode
                    actionMode.setTitle(String.valueOf(selectedIds.size()));
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
        final SQLiteDatabase db = mHelper.getReadableDatabase();
        final String[] selectionArgs = new String[selectedIds.size()];
        final String selection = CaptureContract.CaptureEntry._ID + " IN ("
                + TextUtils.join(",", Collections.nCopies(selectionArgs.length, "?")) + ")";
        switch (menuItem.getItemId()){
            case R.id.action_delete:
                // build String array of selected captures
                int argIdx = 0;
                Iterator<Capture> itr = captureList.iterator();
                while (itr.hasNext()) {
                    Capture capture = itr.next();
                    long id = capture.getId();
                    if (selectedIds.contains(id)) {
                        selectionArgs[argIdx] = Long.toString(id);
                        argIdx++;
                        itr.remove();
                    }
                }
                mAdapter.notifyDataSetChanged();
                sb = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.delete, selectedIds.size()), Snackbar.LENGTH_LONG);
                sb.setAction(getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // did not delete; will restore items removed from the view
                        updateCaptures();
                    }
                }).addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int dismissType) {
                        super.onDismissed(snackbar, dismissType);
                        if (dismissType != DISMISS_EVENT_ACTION) {
                            db.delete(CaptureContract.CaptureEntry.TABLE, selection, selectionArgs);
                            db.close();
                        }
                    }
                }).show();
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        isMultiSelect = false;
        selectedIds = new ArrayList<>();
        mAdapter.setSelectedIds(new ArrayList<Long>());
    }
}
