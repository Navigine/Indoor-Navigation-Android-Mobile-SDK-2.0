package com.navigine.navigine.demo.adapters.venues;

import static com.navigine.navigine.demo.utils.Constants.KEY_VENUE_CATEGORY;
import static com.navigine.navigine.demo.utils.Constants.VENUE_FILTER_OFF;
import static com.navigine.navigine.demo.utils.Constants.VENUE_FILTER_ON;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.models.VenueIconObj;

import java.util.ArrayList;
import java.util.List;

public class VenuesIconsListAdapter extends VenuesListAdapterBase<VenueIconViewHolder> {

    private List<VenueIconObj> mVenueIconObjs = new ArrayList<>();
    private List<VenueIconObj> mSelectedVenueIconsObjs = new ArrayList<>();

    @NonNull
    @Override
    public VenueIconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_venue_icon, parent, false);
        return new VenueIconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueIconViewHolder holder, int position) {
        VenueIconObj venueIconObj = mVenueIconObjs.get(position);
        holder.icon. setImageResource(venueIconObj.getImageDrawable());
        holder.icon. setActivated(venueIconObj.isActivated());
        holder.title.setText(venueIconObj.getCategoryName());
        holder.icon. setOnClickListener(v -> {
            updateViewState(v);
            updateVenueIconObjState(venueIconObj, v.isActivated());
            updateSelectedVenueIconsList(venueIconObj, v.isActivated());
            sendBroadcast();
        });
    }

    @Override
    public int getItemCount() {
        return mVenueIconObjs.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void submit(List<VenueIconObj> venueIconObjs) {
        mVenueIconObjs.addAll(venueIconObjs);
        mRecyclerView.setActivated(!mVenueIconObjs.isEmpty());
        notifyDataSetChanged();
    }

    private void clear() {
        mSelectedVenueIconsObjs.clear();
        mVenueIconObjs.clear();
    }

    public void updateList(List<VenueIconObj> venueIconObjs) {
        clear();
        findAndAddSelectedVenueObjs(venueIconObjs);
        submit(venueIconObjs);
    }

    private void sendBroadcast() {
        Intent intent = new Intent(!mSelectedVenueIconsObjs.isEmpty() ? VENUE_FILTER_ON : VENUE_FILTER_OFF);
        intent.putExtra(KEY_VENUE_CATEGORY, ((ArrayList<VenueIconObj>) mSelectedVenueIconsObjs));
        mContext.sendBroadcast(intent);
    }

    private void updateViewState(View v) {
        v.setActivated(!v.isActivated());
    }

    private void updateVenueIconObjState(VenueIconObj venueIconObj, boolean isActivated) {
        venueIconObj.setActivated(isActivated);
    }

    private void updateSelectedVenueIconsList(VenueIconObj venueIconObj, boolean isActivated) {
        if (isActivated) mSelectedVenueIconsObjs.add(venueIconObj);
        else mSelectedVenueIconsObjs.remove(venueIconObj);
    }

    private void findAndAddSelectedVenueObjs(List<VenueIconObj> venueIconObjs) {
        for (VenueIconObj venueIconObj : venueIconObjs) {
            if (venueIconObj.isActivated())
                mSelectedVenueIconsObjs.add(venueIconObj);
        }
    }
}
