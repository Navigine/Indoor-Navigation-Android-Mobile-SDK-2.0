package com.navigine.navigine.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationInfo;
import com.navigine.idl.java.LocationListListener;
import com.navigine.idl.java.LocationListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class LocationsFragment extends Fragment {

    private ListView            mListView          = null;
    private List<LocationInfo>  mInfoList          = new ArrayList<>();
    private List<Integer>       mHiddenInfoIndices = new ArrayList<>();

    private LocationLoaderAdapter mLoaderAdapter   = null;

    private CustomEditText mSearchField = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locations, container, false);

        mSearchField = view.findViewById(R.id.locations_fragment__search_field);
        mSearchField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                mHiddenInfoIndices.clear();
                if (!charSequence.equals(""))
                {
                    String cs = charSequence.toString().toLowerCase();
                    for (int j = 0; j < mInfoList.size(); ++j)
                    {
                        if (!mInfoList.get(j).getName().toLowerCase().contains(cs))
                            mHiddenInfoIndices.add(j);
                    }
                }
                mLoaderAdapter.updateList();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        NavigineApp.LocationListManager.addLocationListListener(new LocationListListener() {

            @Override
            public void onLocationListLoaded(HashMap<Integer, LocationInfo> hashMap) {
                mInfoList.clear();
                mInfoList.addAll(hashMap.values());

                mLoaderAdapter = new LocationLoaderAdapter();

                mListView = view.findViewById(R.id.locations_fragment__list_view);
                mListView.setAdapter(mLoaderAdapter);
                mListView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLocationListFailed(Error error) {
                System.out.println("Error is " + error.getMessage());
            }
        });

        return view;
    }

    // location loader to list view
    private class LocationLoaderAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mInfoList.size() - mHiddenInfoIndices.size();
        }

        @Override
        public Object getItem(int i)
        {
            return mInfoList.get(i);
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
            for (Integer hiddenInfoIndex : mHiddenInfoIndices)
                if (hiddenInfoIndex <= i)
                    ++i;

            final int position = i;

            final LocationInfo locationInfo    = mInfoList.get(position);
            final int          locationId      = locationInfo.getId();
            final int          locationVersion = locationInfo.getVersion();
            final String       locationTitle   = locationInfo.getName();

            View view = convertView;
            if (view == null)
            {
                LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.location_list_item, null);
            }

            TextView  titleTextView    = view.findViewById(R.id.locations_list_item__location_title);
            TextView  versionTextView  = view.findViewById(R.id.locations_list_item__location_version);

            CircularProgressBar  loadingProgressBar     = view.findViewById(R.id.locations_list_item__progress_bar);
            TextView             loadingProgressPercent = view.findViewById(R.id.locations_list_item__progress_percent);
            ImageView            locationSelected       = view.findViewById(R.id.locations_list_item__location_selected);
            RelativeLayout       progressLayout         = view.findViewById(R.id.locations_list_item__progress_view);

            locationSelected.setVisibility(View.GONE);
            loadingProgressPercent.setVisibility(View.VISIBLE);
            loadingProgressPercent.setText("0%");

            String titleText = locationTitle;
            if (titleText.length() > 30)
                titleText = titleText.substring(0, 28) + "...";
            titleTextView.setText(titleText);

            versionTextView.setText(String.valueOf(locationVersion));

            progressLayout.setVisibility(locationId == NavigineApp.LocationId ? View.VISIBLE : View.INVISIBLE);

            view.setOnClickListener(v -> {
               NavigineApp.LocationManager.setLocationId(locationId);
               NavigineApp.LocationManager.addLocationListener(new LocationListener() {
                   @Override
                   public void onLocationLoaded(Location location) {
                       loadingProgressBar.setProgressAnimated(100);
                       loadingProgressPercent.setVisibility(View.GONE);
                       locationSelected.setVisibility(View.VISIBLE);
                       NavigineApp.LocationManager.removeLocationListener(this);
                   }

                   @Override
                   public void onDownloadProgress(int i, int i1) {
                       int state = (int) (i * 100.f / i1);
                       if (state >= 0 & state < 100) {
                           loadingProgressBar.setProgress(state);
                           loadingProgressPercent.setVisibility(View.VISIBLE);
                           loadingProgressPercent.setText(state + "%");
                           locationSelected.setVisibility(View.GONE);
                       }
                   }

                   @Override
                   public void onLocationFailed(Error error) {
                       loadingProgressBar.setProgressAnimated(100);
                       loadingProgressPercent.setVisibility(View.GONE);
                       locationSelected.setVisibility(View.GONE);
                       NavigineApp.LocationManager.removeLocationListener(this);
                   }
               });
               NavigineApp.LocationId = locationId;
               mLoaderAdapter.updateList();
            });

            return view;
        }
    }
}