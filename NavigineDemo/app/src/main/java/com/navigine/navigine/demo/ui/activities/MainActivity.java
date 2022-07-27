package com.navigine.navigine.demo.ui.activities;


import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_LOCATION_ID;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_SUBLOCATION_ID;
import static com.navigine.navigine.demo.utils.Constants.KEY_ID_LOCATION;
import static com.navigine.navigine.demo.utils.Constants.KEY_ID_SUBLOCATION;
import static com.navigine.navigine.demo.utils.Constants.LOCATION_CHANGED;
import static com.navigine.navigine.demo.utils.Constants.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.models.BeaconMock;
import com.navigine.navigine.demo.ui.custom.navigation.SavedBottomNavigationView;
import com.navigine.navigine.demo.utils.NavigineSdkManager;
import com.navigine.navigine.demo.viewmodel.SharedViewModel;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SharedViewModel viewModel = null;

    private SavedBottomNavigationView mBottomNavigation = null;
    private List<Integer> navGraphIds = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewModel();
        initNavigationView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleIntent();
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
    }

    private void initNavigationView() {
        mBottomNavigation = findViewById(R.id.main__bottom_navigation);
        navGraphIds = Arrays.asList(
                R.navigation.navigation_locations,
                R.navigation.navigation_navigation,
                R.navigation.navigation_debug,
                R.navigation.navigation_profile);

        mBottomNavigation.setupWithNavController(
                navGraphIds,
                getSupportFragmentManager(),
                R.id.nav_host_fragment_activity_main);
    }

    private void handleIntent() {
        Uri qrData = getIntent().getData();
        if (qrData != null) {
            try {
                int locationId    = Integer.parseInt(qrData.getQueryParameter(DL_QUERY_LOCATION_ID));
                int sublocationId = Integer.parseInt(qrData.getQueryParameter(DL_QUERY_SUBLOCATION_ID));
                Intent intent = new Intent(LOCATION_CHANGED);
                intent.putExtra(KEY_ID_LOCATION, locationId);
                intent.putExtra(KEY_ID_SUBLOCATION, sublocationId);
                NavigineSdkManager.LocationManager.setLocationId(locationId);
                sendBroadcast(intent);
            } catch (NullPointerException npe) {
                Log.e(TAG, getString(R.string.err_deep_link));
            }
            getIntent().setData(null);
        }
    }


    private void addBeaconGenerator() {
        NavigineSdkManager.MeasurementManager.addBeaconGenerator(BeaconMock.UUID,
                BeaconMock.MAJOR,
                BeaconMock.MINOR,
                BeaconMock.POWER,
                BeaconMock.TIMEOUT,
                BeaconMock.RSS_MIN,
                BeaconMock.RSS_MAX
        );
    }
}