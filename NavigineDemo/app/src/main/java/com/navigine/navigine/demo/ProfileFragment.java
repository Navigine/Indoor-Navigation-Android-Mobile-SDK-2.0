package com.navigine.navigine.demo;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.navigine.idl.java.BitmapRegionDecoder;
import com.navigine.idl.java.Image;
import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationListener;
import com.navigine.idl.java.Position;
import com.navigine.idl.java.PositionListener;
import com.navigine.idl.java.Rectangle;
import com.navigine.idl.java.ResourceListener;
import com.navigine.idl.java.ResourceUploadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ProfileFragment extends Fragment {

    private android.widget.ListView mLogListView;
    private LogsAdapter mLogsAdapter;
    List<String> mLogList;

    private BottomSheetBehavior mBehavior    = null;
    private ConstraintLayout mBottomSheet = null;


    class LogsAdapter extends BaseAdapter
    {
        public int getCount()
        {
            return mLogList.size();
        }

        public String getItem(int i)
        {
            return mLogList.get(i);
        }

        public long getItemId(int i)
        {
            return i;
        }

        public View getView(int i, View convertView, ViewGroup viewGroup)
        {
            View view = convertView;

            if (view == null)
            {
                LayoutInflater layoutInflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.logs_list_item, null);
            }

            view.setOnTouchListener(null);

            TextView nameTextView = view.findViewById(R.id.logs_list_item__name);

            nameTextView.setText(getItem(i));

            view.setOnClickListener(view1 ->
            {
                TextView title = mBottomSheet.findViewById(R.id.logs_dialog__title);
                title.setText(getItem(i));
                mBottomSheet.findViewById(R.id.logs_dialog__share).setOnClickListener(view2 ->
                {
                    emailLogs(getItem(i));
                    mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                });
                mBottomSheet.findViewById(R.id.logs_dialog__delete).setOnClickListener(view2 -> {
                    deleteLogs(getItem(i));
                    mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                });

                mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            });

            return view;
        }
    }

    private void deleteLogs(String item) {
        NavigineApp.ResourceManager.removeLogFile(item);
        loadLogs();
    }

    private void emailLogs(String item) {
        NavigineApp.ResourceManager.uploadLogFile(item, new ResourceUploadListener() {
            @Override
            public void onUploaded() {
                Toast.makeText(getContext(), "Log uploaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Error error) {
                Toast.makeText(getContext(), "Logfile uploading failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mLogListView = view.findViewById(R.id.logs__log_list);

        mBottomSheet = view.findViewById(R.id.logs__bottom_sheet);
        mBehavior    = BottomSheetBehavior.from(mBottomSheet);
        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mLogList = new ArrayList<>();
        mLogsAdapter = new LogsAdapter();
        mLogListView.setAdapter(mLogsAdapter);

        Button refresh = view.findViewById(R.id.logs_update_button);
        refresh.setOnClickListener(view1 -> loadLogs());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLogs();
    }

    private void loadLogs() {
        mLogList.clear();
        mLogList.addAll(NavigineApp.ResourceManager.getLogsList());
        mLogsAdapter.notifyDataSetChanged();
    }
}