package com.navigine.navigine.demo.ui.fragments;

import static android.view.View.GONE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.navigine.idl.java.LocationInfo;
import com.navigine.idl.java.LocationListListener;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.adapters.locations.LocationListAdapter;
import com.navigine.navigine.demo.utils.Constants;
import com.navigine.navigine.demo.utils.NavigineSdkManager;
import com.navigine.navigine.demo.utils.NetworkUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class LocationsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {

    private static final int DELAY_UPDATE   = 3000;

    private Window                    window                     = null;
    private SwipeRefreshLayout        mSwipeRefreshLayout        = null;
    private SearchView                mSearchField               = null;
    private RecyclerView              mListView                  = null;
    private FrameLayout               mCircularProgress          = null;
    private CircularProgressIndicator mCircularProgressIndicator = null;
    private TextView                  mWarningTv                 = null;
    private DividerItemDecoration     mDivider                   = null;

    private SortedSet<LocationInfo> mInfoList = new TreeSet<>(new InfoComparator());

    private LocationListAdapter mLocationsListAdapter = null;

    private LocationListListener mLocationListListener = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListeners();
        addListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_locations, container, false);

        initViews(view);
        setViewsParams();
        initAdapters();
        setAdapters();
        setViewsListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateLocationListDeferred();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            updateStatusBar();
            if (isProgressShown()) updateLocationListForce();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        removeListeners();
    }

    @Override
    public void onRefresh() {
        updateLocationListForce();
        hideRefreshViewDeferred();
    }

    private void initViews(View view) {
        window                     = requireActivity().getWindow();
        mSwipeRefreshLayout        = view.findViewById(R.id.locations_fragment__swipe_layout);
        mListView                  = view.findViewById(R.id.locations_fragment__list_view);
        mSearchField               = view.findViewById(R.id.locations_fragment__search_field);
        mCircularProgress          = view.findViewById(R.id.locations_fragment__progress_circular);
        mCircularProgressIndicator = view.findViewById(R.id.locations_fragment__progress_circular_indicator);
        mWarningTv                 = view.findViewById(R.id.locations_fragment__warning);
        mDivider                   = new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL);
    }

    private void setViewsParams() {
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireActivity(), R.color.colorPrimary));
        mDivider.setDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.li_divider_transparent));
        mListView.addItemDecoration(mDivider);
        mWarningTv.setText(R.string.err_locations_update);
    }

    private void initAdapters() { mLocationsListAdapter = new LocationListAdapter(); }

    private void setAdapters() { mListView.setAdapter(mLocationsListAdapter); }

    private void setViewsListeners() {
        mSwipeRefreshLayout.  setOnRefreshListener(this);
        mSearchField.         setOnQueryTextListener(this);
        mSearchField.         setOnQueryTextFocusChangeListener((v, hasFocus) -> mSearchField.setBackgroundResource(hasFocus ? R.drawable.bg_rounded_search_light : R.drawable.bg_rounded_search));
        mLocationsListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                hideCircularProgress();
                hideRefreshView();
                hideWarningMessage();
            }
        });
    }

    private void initListeners() {
        mLocationListListener = new LocationListListener() {
            @Override
            public void onLocationListLoaded(HashMap<Integer, LocationInfo> hashMap) {
                if (hashMap != null)
                    if (isVisible()) {
                        updateLocationList(hashMap);
                    }
            }

            @Override
            public void onLocationListFailed(Error error) {
                if (isVisible()) {
                    hideCircularProgress();
                    hideRefreshView();
                    showWarningMessage();
                }
            }
        };
    }

    private void addListeners() {
        NavigineSdkManager.LocationListManager.addLocationListListener(mLocationListListener);
    }

    private void removeListeners() {
        NavigineSdkManager.LocationListManager.removeLocationListListener(mLocationListListener);
    }

    private void updateStatusBar() {
        window.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.colorBackground));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mLocationsListAdapter.filter(newText);
        return true;
    }

    private void updateLocationList(Map<Integer, LocationInfo> locationInfoMap) {
        mInfoList.clear();
        mInfoList.addAll(locationInfoMap.values());
        mLocationsListAdapter.submitList(mInfoList);
    }

    private void updateLocationListForce() {
        if (NetworkUtils.isNetworkActive(requireActivity()))
            NavigineSdkManager.LocationListManager.updateLocationList();
        else
            updateLocationList(NavigineSdkManager.LocationListManager.getLocationList());
    }

    private void updateLocationListDeferred() {
        if (getView() != null) {
            getView().postDelayed(() -> {
                if (isVisible() && isProgressShown()) updateLocationListForce();
            }, DELAY_UPDATE);
        }
    }

    private void hideCircularProgress() {
        mCircularProgressIndicator.hide();
        mCircularProgressIndicator.postDelayed(() -> mCircularProgress.setVisibility(GONE), Constants.CIRCULAR_PROGRESS_DELAY_HIDE);
    }

    private void hideRefreshView() {
        if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
    }

    private void hideRefreshViewDeferred() {
        if (getView() != null)
            getView().postDelayed(this::hideRefreshView, DELAY_UPDATE);
    }

    private boolean isProgressShown() {
        return mCircularProgress.getVisibility() == View.VISIBLE;
    }

    private void showWarningMessage() {
        mWarningTv.setVisibility(View.VISIBLE);
    }

    private void hideWarningMessage() {
        mWarningTv.setVisibility(GONE);
    }

    private class InfoComparator implements Comparator<LocationInfo>
    {
        @Override
        public int compare(LocationInfo o1, LocationInfo o2) {
            final String locationTitle1 = o1.getName().toLowerCase();
            final String locationTitle2 = o2.getName().toLowerCase();

            if (locationTitle1.length() == 0)
                return -1;
            if (locationTitle2.length() == 0)
                return 1;

            return locationTitle1.compareTo(locationTitle2);
        }
    }

}