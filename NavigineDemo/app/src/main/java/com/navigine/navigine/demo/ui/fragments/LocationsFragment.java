package com.navigine.navigine.demo.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationInfo;
import com.navigine.idl.java.LocationListListener;
import com.navigine.idl.java.LocationListener;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.application.NavigineApp;
import com.navigine.navigine.demo.ui.custom.CustomEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationsFragment extends Fragment {

    private ListView              mListView          = null;
    private List<LocationInfo>    mInfoList          = new ArrayList<>();
    private List<Integer>         mHiddenInfoIndices = new ArrayList<>();
    private LocationLoaderAdapter mLoaderAdapter     = null;
    private CustomEditText        mSearchField       = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locations, container, false);

        initViews(view);
        setViewsListeners();
        initAdapters();
        addListeners();

        return view;
    }


    private void addListeners() {
        /*
         * GET LIST OF LOADED LOCATIONS
         *
         * To get a list of downloaded locations, add LocationListListener to LocationListManager.
         * If list of location successfully loaded and is not empty, then incoming hasMap contains
         * location id as a key and location info (id, name, version) as a value.
         * If an error occurred while loading the list of locations, then the onLocationListFailed
         * will be called.
         */
        NavigineApp.LocationListManager.addLocationListListener(new LocationListListener() {

            @Override
            public void onLocationListLoaded(HashMap<Integer, LocationInfo> hashMap) {
                mInfoList.clear();
                mInfoList.addAll(hashMap.values());
                mLoaderAdapter.updateList();
            }

            @Override
            public void onLocationListFailed(Error error) {
                System.out.println("Error is " + error.getMessage());
            }
        });

    }

    private void initViews(View view) {
        mSearchField = view.findViewById(R.id.locations_fragment__search_field);
        mListView = view.findViewById(R.id.locations_fragment__list_view);
    }

    private void setViewsListeners() {
        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mHiddenInfoIndices.clear();
                if (!charSequence.equals("")) {
                    String cs = charSequence.toString().toLowerCase();
                    for (int j = 0; j < mInfoList.size(); ++j) {
                        if (!mInfoList.get(j).getName().toLowerCase().contains(cs))
                            mHiddenInfoIndices.add(j);
                    }
                }
                mLoaderAdapter.updateList();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void initAdapters() {
        mLoaderAdapter = new LocationLoaderAdapter();
        mListView.setAdapter(mLoaderAdapter);
    }

    // location loader to list view
    private class LocationLoaderAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mInfoList.size() - mHiddenInfoIndices.size();
        }

        @Override
        public Object getItem(int i) {
            return mInfoList.get(i);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        private void updateList() {
            notifyDataSetChanged();
        }


        @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            for (Integer hiddenInfoIndex : mHiddenInfoIndices)
                if (hiddenInfoIndex <= i)
                    ++i;

            final int position = i;

            final LocationInfo locationInfo = mInfoList.get(position);

            final int    locationId      = locationInfo.getId();
            final int    locationVersion = locationInfo.getVersion();
            final String locationTitle   = locationInfo.getName();

            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.location_list_item, null);
            }

            TextView titleTextView   = view.findViewById(R.id.locations_list_item__location_title);
            TextView versionTextView = view.findViewById(R.id.locations_list_item__location_version);

            CircularProgressIndicator loadingProgressBar     = view.findViewById(R.id.locations_list_item__progress_bar);
            TextView                  loadingProgressPercent = view.findViewById(R.id.locations_list_item__progress_percent);
            ImageView                 locationSelected       = view.findViewById(R.id.locations_list_item__location_selected);
            RelativeLayout            progressLayout         = view.findViewById(R.id.locations_list_item__progress_view);

            boolean isSelected = locationId == NavigineApp.LocationId;

            if (isSelected) {
                progressLayout.setVisibility(VISIBLE);
                loadingProgressBar.setProgress(100);
                locationSelected.setVisibility(VISIBLE);
                loadingProgressPercent.setVisibility(GONE);
            } else {
                progressLayout.setVisibility(GONE);
            }

            String titleText = locationTitle;
            if (titleText.length() > 30)
                titleText = titleText.substring(0, 28) + "...";
            titleTextView.setText(titleText);

            versionTextView.setText(String.valueOf(locationVersion));

            view.setOnClickListener(v -> {

                progressLayout.        setVisibility(VISIBLE);
                loadingProgressBar.    setVisibility(VISIBLE);
                loadingProgressPercent.setVisibility(VISIBLE);
                locationSelected      .setVisibility(GONE);

                loadingProgressBar.setProgress(0);
                loadingProgressPercent.setText("0%");


                /*
                 * CHECK IF SELECTED LOCATION LOADED OR GET DOWNLOAD PROGRESS
                 *
                 * To monitor the location loading process, add LocationListener to LocationManager.
                 * Location loading progress can be found in onDownloadProgress callback.
                 * params:
                 *          i  - location id
                 *          i1 - amount of loaded content (location map)
                 *          i2 - content (location map) size
                 * When location loading is complete, then onLocationLoaded called.
                 * params:
                 *          location - loaded location
                 *If an error occurs during download process, then  onLocationFailed will be called.
                 * params:
                 *          i - location id
                 *          error - an error occurred during loading
                 * If another location is selected during download, then onLocationCancelled
                 * will be called.
                 * params:
                 *          i - location id of new selected
                 */
                NavigineApp.LocationManager.addLocationListener(new LocationListener() {
                    @Override
                    public void onLocationLoaded(Location location) {
                        if (location.getId() == locationId) {
                            NavigineApp.LocationId = location.getId();
                            NavigineApp.CurrentLocation = location;
                            NavigineApp.CurrentSublocation = location.getSublocations().get(0);
                            NavigineApp.LocationManager.removeLocationListener(this);
                            updateList();
                        }
                    }

                    @Override
                    public void onDownloadProgress(int i, int i1, int i2) {
                        if (i == locationId) {
                            int state = (int) (i1 * 100.f / i2);
                            if (state >= 0 & state < 100) {
                                loadingProgressBar.setProgress(state);
                                loadingProgressPercent.setText(state + "%");
                            }
                        }
                    }

                    @Override
                    public void onLocationFailed(int i, Error error) {
                    }

                    @Override
                    public void onLocationCancelled(int i) {
                        if (i != locationId) {
                            NavigineApp.LocationManager.removeLocationListener(this);
                        }
                    }

                });

                NavigineApp.LocationManager.setLocationId(locationId);//set selected location as current
            });

            return view;
        }
    }
}