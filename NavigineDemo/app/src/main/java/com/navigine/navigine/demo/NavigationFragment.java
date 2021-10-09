package com.navigine.navigine.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.navigine.idl.java.AnimationType;
import com.navigine.idl.java.CircleMapObject;
import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationListener;
import com.navigine.idl.java.Notification;
import com.navigine.idl.java.NavigationManager;
import com.navigine.idl.java.NotificationListener;
import com.navigine.idl.java.NotificationManager;
import com.navigine.idl.java.Point;
import com.navigine.idl.java.Position;
import com.navigine.idl.java.PositionListener;
import com.navigine.idl.java.Sublocation;
import com.navigine.idl.java.MapObject;
import com.navigine.view.TouchInput;
import com.navigine.view.LocationView;
//import com.navigine.view.internal.TouchInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NavigationFragment extends Fragment {

    Button swap;
    Button record;
    Button attachToPosition;
    GridView gvMain;
    SublocationsAdapter mAdapter;
    Location mLocation = null;
    List<Sublocation> mSublocations = new ArrayList<>();
    CircleMapObject mSelectedObject;

    LocationView locationView;
    ConstraintLayout mVenueBottomSheet;
    BottomSheetBehavior mVenueBehavior;

    NotificationManager mNotificationManager = null;

    MapObject mPosition = null;

    Button showBeacons;
    Button showEddys;
    Button showWifis;
    Button showVenues;

    boolean mBeaconsVisibility = false;
    boolean mEddysVisibility = false;
    boolean mWifisVisibility = false;
    boolean mVenuesVisibility = false;
    boolean mAttachedToPosition = false;

    boolean isRecording = false;

    Context mContext;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        swap = view.findViewById(R.id.swap_sublocations);
        swap.setOnClickListener(view1 -> {
            if (gvMain.getVisibility() == View.VISIBLE)
                gvMain.setVisibility(View.GONE);
            else if (gvMain.getVisibility() == View.GONE)
                gvMain.setVisibility(View.VISIBLE);
        });

        locationView = view.findViewById(R.id.location_view);

        record = view.findViewById(R.id.record);
        record.setOnClickListener(view1 -> {
            if (isRecording) {
                NavigineApp.NavigationManager.stopLogRecording();
                isRecording = false;
                record.setText("Rec");
                record.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            } else {
                NavigineApp.NavigationManager.startLogRecording();
                isRecording = true;
                record.setText("Stop");
                record.setTextColor(getResources().getColor(R.color.removeColor));
            }
        });

        mPosition = locationView.getLocationViewController().addMapObject();

        NavigineApp.NavigationManager.addPositionListener(new PositionListener() {
            @Override
            public void onPositionUpdated(Position position) {
                mPosition.setPositionAnimated(position.getPoint(), 1.0f, AnimationType.CUBIC);
            }

            @Override
            public void onPositionError(Error error) {

            }
        });

        attachToPosition = view.findViewById(R.id.attach_to_position);
        attachToPosition.setOnClickListener(view1 -> {
            mAttachedToPosition = !mAttachedToPosition;
//            locationView.attachToPosition(mAttachedToPosition);
        });

        gvMain = view.findViewById(R.id.sub_loc_list);

        NavigineApp.LocationManager.addLocationListener(new LocationListener() {
            @Override
            public void onLocationLoaded(Location location) {
                mLocation = location;

                mSublocations.clear();
                mSublocations.addAll(location.getSublocations());

                gvMain.setVisibility(View.VISIBLE);
                mAdapter = new SublocationsAdapter();
                gvMain.setAdapter(mAdapter);
                mAdapter.updateList();
            }

            @Override
            public void onDownloadProgress(int i, int i1) {

            }

            @Override
            public void onLocationFailed(Error error) {
                System.out.println(error.getMessage());
            }
        });

        mVenueBottomSheet = view.findViewById(R.id.navigation_fragment__venue_sheet);
        mVenueBehavior = BottomSheetBehavior.from(mVenueBottomSheet);
        mVenueBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mVenueBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && mSelectedObject != null)
                {
                    mSelectedObject.setRadius(0.01f);
                    mSelectedObject = null;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        TextView name = mVenueBottomSheet.findViewById(R.id.venue_dialog__title);
        TextView phone = mVenueBottomSheet.findViewById(R.id.venue_dialog__phone_tv);
        TextView category = mVenueBottomSheet.findViewById(R.id.venue_dialog__category_tv);
        TextView description = mVenueBottomSheet.findViewById(R.id.venue_dialog__description);

        locationView.getLocationViewController().getTouchInput().setTapResponder(new TouchInput.TapResponder() {
            @Override
            public boolean onSingleTapUp(float v, float v1) {
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(float x, float y) {
//                CircleMapObject object = locationView.getObjectAt(new Point(x, y));
                return true;
            }
        });

//        locationView.getTouchInput().setLongPressResponder((x, y) -> locationView.setTargetPoint(new Point(x, y)));

        showBeacons = view.findViewById(R.id.beacons);
        showBeacons.setOnClickListener(view1 ->
        {
            mBeaconsVisibility = !mBeaconsVisibility;
//            locationView.showBeacons(mBeaconsVisibility);
        });
        showEddys = view.findViewById(R.id.eddystones);
        showEddys.setOnClickListener(view1 ->
        {
            mEddysVisibility = !mEddysVisibility;
//            locationView.showEddystones(mEddysVisibility);
        });
        showWifis = view.findViewById(R.id.wifis);
        showWifis.setOnClickListener(view1 ->
        {
            mWifisVisibility = !mWifisVisibility;
//            locationView.showWifis(mWifisVisibility);
        });
        showVenues = view.findViewById(R.id.venues);
        showVenues.setOnClickListener(view1 ->
        {
            mVenuesVisibility = !mVenuesVisibility;
//            locationView.showVenues(mVenuesVisibility);
        });

        mNotificationManager = NavigineApp.NotificationManager;

        mNotificationManager.addNotificationListener(new NotificationListener() {
            @Override
            public void onNotificationLoaded(Notification notification) {
                Log.d("Notification", "onNotificationLoaded: " + notification.getTitle());
            }

            @Override
            public void onNotificationFailed(Error error) {
                Log.d("Notification", "onNotificationLoaded: " + error.getMessage());
            }
        });

        return view;
    }

    // location loader to list view
    private class SublocationsAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mSublocations.size();
        }

        @Override
        public Object getItem(int i)
        {
            return mSublocations.get(i);
        }

        @Override
        public long getItemId(int pos)
        {
            return pos;
        }

        private void updateList()
        {
            notifyDataSetChanged();
        }

        @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup)
        {
            Sublocation sublocation     = mSublocations.get(i);
            String      sublocationName = sublocation.getName();

            View view = convertView;
            if (view == null)
            {
                LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.item, null);
            }

            TextView titleTextView = view.findViewById(R.id.tvText);

            if (sublocationName.length() >= 12)
                sublocationName = sublocationName.substring(0, 10) + "...";

            titleTextView.setText(sublocationName);

            view.setOnClickListener(v -> {
                locationView.getLocationViewController().setSublocationId(sublocation.getId());
                gvMain.setVisibility(View.GONE);
//                NavigineApp.MeasurementManager.addBeaconGenerator("F7826DA6-4FA2-4E98-8024-BC5B71E0893E", 1, 1, -72, 1000, -75, -55);
//                NavigineApp.MeasurementManager.addBeaconGenerator("F7826DA6-4FA2-4E98-8024-BC5B71E089AA", 47213, 11741, -72, 1000, -75, -55);
//                NavigineApp.MeasurementManager.addEddystoneGenerator("11111111111111111111", "000000000ffd", -72, 1000, -75, -55);
//                NavigineApp.MeasurementManager.addWifiGenerator("703ACBBDE624", 1000, -75, -55);
//                NavigineApp.MeasurementManager.addWifiRttGenerator("703ACBBDE624", 0.3f, 1000, -75, -55);
            });

            return view;
        }
    }
}
