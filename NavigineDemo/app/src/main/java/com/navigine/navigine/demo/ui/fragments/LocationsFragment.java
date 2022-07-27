package com.navigine.navigine.demo.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.navigine.idl.java.LocationInfo;
import com.navigine.idl.java.LocationListListener;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.adapters.locations.LocationListAdapter;
import com.navigine.navigine.demo.utils.NavigineSdkManager;
import com.navigine.navigine.demo.viewmodel.SharedViewModel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class LocationsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {

    private SharedViewModel viewModel = null;

    private Window                    window                     = null;
    private SwipeRefreshLayout        mSwipeRefreshLayout        = null;
    private SearchView                mSearchField               = null;
    private RecyclerView              mListView                  = null;
    private FrameLayout               mCircularProgress          = null;
    private CircularProgressIndicator mCircularProgressIndicator = null;
    private DividerItemDecoration     mDivider                   = null;

    private SortedSet<LocationInfo>   mInfoList           = new TreeSet<>(new InfoComparator());

    private LocationListAdapter       mLocationsListAdapter = null;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewModel();
        checkNetworkConnection();
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
        setObservers();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addListeners();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            window.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.colorBackground));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mSearchField.setQuery("", true);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    private void checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        viewModel.checkNetworkConnection(connectivityManager);
    }

    private void initViews(View view) {
        window                     = requireActivity().getWindow();
        mSwipeRefreshLayout        = view.findViewById(R.id.locations_fragment__swipe_layout);
        mListView                  = view.findViewById(R.id.locations_fragment__list_view);
        mSearchField               = view.findViewById(R.id.locations_fragment__search_field);
        mCircularProgress          = view.findViewById(R.id.locations_fragment__progress_circular);
        mCircularProgressIndicator = view.findViewById(R.id.locations_fragment__progress_circular_indicator);
        mDivider                   = new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL);
    }

    private void setViewsParams() {
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireActivity(), R.color.colorPrimary));
        mDivider.setDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.li_divider_transparent));
        mListView.addItemDecoration(mDivider);
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
                if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setObservers() {
        viewModel.mNetworkAvailable.observe(getViewLifecycleOwner(), isAvailable -> {
            if (isAvailable)
                if (mCircularProgress.getVisibility() == VISIBLE)
                    NavigineSdkManager.LocationListManager.updateLocationList();
                else
                    updateList(NavigineSdkManager.LocationListManager.getLocationList());
        });
    }

    private void addListeners() {
        NavigineSdkManager.LocationListManager.addLocationListListener(new LocationListListener() {
            @Override
            public void onLocationListLoaded(HashMap<Integer, LocationInfo> hashMap) {
                updateList(hashMap);
            }

            @Override
            public void onLocationListFailed(Error error) {
                hideCircularProgress();
                if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
                Snackbar.make(getView(), R.string.err_locations_update, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(requireActivity(), R.color.colorError))
                        .setTextColor(Color.WHITE)
                        .setAnchorView(R.id.main__bottom_navigation)
                        .show();
            }
        });
    }

    @Override
    public void onRefresh() {
        NavigineSdkManager.LocationListManager.updateLocationList();
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

    private void updateList(HashMap<Integer, LocationInfo> hashMap) {
        mInfoList.clear();
        mInfoList.addAll(hashMap.values());
        mLocationsListAdapter.submitList(mInfoList);
    }

    private void hideCircularProgress() {
        mCircularProgressIndicator.hide();
        mHandler.postDelayed(() -> mCircularProgress.setVisibility(GONE), 700);
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