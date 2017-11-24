package com.gjdevera.ocrreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gjdevera.ocrreader.db.Capture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Jacob on 11/10/2017.
 */

public class CaptureViewAdapter extends RecyclerView.Adapter<CaptureViewAdapter.CaptureViewHolder> {
    private Context context;
    private List<Capture> captureList;
    private List<Integer> selectedIds;

    public CaptureViewAdapter(Context context, List<Capture> captureList) {
        this.context = context;
        this.captureList = captureList;
        selectedIds = new ArrayList<>();
    }

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
        TextView tv = vh.itemView.findViewById(R.id.text1);
        ImageView iv = vh.itemView.findViewById(R.id.image);
        Capture capture = captureList.get(position);
        String s = capture.getText();
        s = s.replace("\n"," ");
        tv.setText(s);
        tv = vh.itemView.findViewById(R.id.text2);
        tv.setText(getDate(capture.getCreated()));

        Glide.with(context).load(new File(capture.getPath())).into(iv);
        vh.rootView.setForeground(
                selectedIds.contains(position) ?
                        new ColorDrawable(ContextCompat.getColor(context,R.color.colorControlActivated))
                        : new ColorDrawable(ContextCompat.getColor(context,android.R.color.transparent)));
    }

    public void setSelectedIds(List<Integer> selectedIds) {
        this.selectedIds = selectedIds;
    }

    @Override
    public long getItemId(int position) {
        return captureList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return captureList.size(); // db size
    }

    class CaptureViewHolder
            extends RecyclerView.ViewHolder {
        FrameLayout rootView;

        private CaptureViewHolder(View v) {
            super(v);
            rootView = v.findViewById(R.id.root_view);
        }
    }

    // attempt to format UTC date to user's local time
    private String getDate(String date) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = formatter.parse(date);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy hh:mm a", Locale.getDefault());
            date = dateFormatter.format(value);
        }
        catch (Exception e) {
            date = "00-00-0000 00:00";
        }
        return date;
    }
}
