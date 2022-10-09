package com.navigine.navigine.demo.adapters.locations;

import static com.navigine.navigine.demo.utils.Constants.CHECK_FRAME_SELECTED;
import static com.navigine.navigine.demo.utils.Constants.LOCATION_CHANGED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.navigine.idl.java.LocationInfo;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.utils.DimensionUtils;
import com.navigine.navigine.demo.utils.NavigineSdkManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LocationListAdapter extends RecyclerView.Adapter<LocationViewHolder> {

    private static Context mContext = null;

    private static final int TYPE_ROUNDED_TOP    = 0;
    private static final int TYPE_ROUNDED_BOTTOM = 1;
    private static final int TYPE_RECT           = 2;

    private List<LocationInfo> currentList  = new ArrayList<>();
    private List<LocationInfo> filteredList = new ArrayList<>();

    private LocationViewHolder mSelectedHolder = null;


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mContext = recyclerView.getContext();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_ROUNDED_TOP;
        if (position == currentList.size() - 1) return TYPE_ROUNDED_BOTTOM;
        return TYPE_RECT;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_round_top_locations, parent, false);
                break;
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_round_bottom_locations, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_locations, parent, false);
                break;
        }
        return new LocationViewHolder(view);
    }

    @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, @SuppressLint("RecyclerView") int position) {
        LocationInfo info = currentList.get(position);
        setViewHolderParams(holder, info.getName(), info.getId());
    }

    private void setViewHolderParams(@NonNull LocationViewHolder holder, String locationName, int locationId) {
        if (isCurrentLocation(locationId)) {
            setSelectedViewHolder(holder);
            markViewHolderAsChecked(holder, false);
        }
        else markViewHolderAsUnchecked(holder);

        holder.titleTextView.setText(locationName);
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {

            private static final int TOUCH_SHORT_TIMEOUT = 200;
            private static final int TOUCH_SENSITIVITY = 20;

            private long mTouchTime = 0;
            private float mTouchLength = 0.0f;

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                long timeNow = System.currentTimeMillis();
                int actionMask = event.getActionMasked();

                switch (actionMask) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchTime = timeNow;
                        mTouchLength = 0.0f;
                        break;

                    case MotionEvent.ACTION_UP:
                        if (mTouchTime > 0 &&
                                mTouchTime + TOUCH_SHORT_TIMEOUT > timeNow &&
                                mTouchLength < TOUCH_SENSITIVITY * DimensionUtils.DisplayDensity)
                            onHandleTouchEvent(locationId, holder);
                        mTouchTime = 0;
                        mTouchLength = 0.0f;
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull LocationViewHolder holder) {
        holder.check.cancelAnimation();
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(Collection<LocationInfo> locationInfos) {
        currentList.clear();
        currentList.addAll(locationInfos);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        if (TextUtils.isEmpty(query)) {
            filteredList.clear();
            filteredList.addAll(NavigineSdkManager.LocationListManager.getLocationList().values());
        } else {
            filteredList.clear();
            for (LocationInfo info : NavigineSdkManager.LocationListManager.getLocationList().values()) {
                if (info.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(info);
                }
            }
        }
        submitList(filteredList);
    }

    private void onHandleTouchEvent(int locationId, @NonNull LocationViewHolder holder) {
        if (!isCurrentLocation(locationId)) {
            markViewHolderSelected(holder);
            selectLocation(locationId);
        }
    }

    private boolean isCurrentLocation(int locationId) {
        return NavigineSdkManager.LocationManager.getLocationId() == locationId;
    }

    private void markViewHolderSelected(@NonNull LocationViewHolder holder) {
        if (getSelectedViewHolder() != null) {
            markViewHolderAsUnchecked(getSelectedViewHolder());
        }
        setSelectedViewHolder(holder);
        markViewHolderAsChecked(holder, true);
    }

    private void markViewHolderAsChecked(@NonNull LocationViewHolder holder, boolean animate) {
        holder.itemContainer.setSelected(true);
        holder.check.setVisibility(View.VISIBLE);
        if (animate) holder.check.playAnimation();
        else holder.check.setProgress(CHECK_FRAME_SELECTED);
    }
    private void markViewHolderAsUnchecked(@NonNull LocationViewHolder holder) {
        holder.itemContainer.setSelected(false);
        holder.check.setVisibility(View.GONE);
    }

    private void selectLocation(int locationId) {
        NavigineSdkManager.LocationManager.setLocationId(locationId);
        mContext.sendBroadcast(new Intent(LOCATION_CHANGED));
    }

    private void setSelectedViewHolder(LocationViewHolder holder) {
        mSelectedHolder = holder;
    }

    private LocationViewHolder getSelectedViewHolder() {
        return mSelectedHolder;
    }
}