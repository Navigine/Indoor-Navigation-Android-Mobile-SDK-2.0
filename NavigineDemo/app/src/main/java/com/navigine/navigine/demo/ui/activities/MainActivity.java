package com.navigine.navigine.demo.ui.activities;


import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_SERVER;
import static com.navigine.navigine.demo.utils.Constants.DL_QUERY_USERHASH;
import static com.navigine.navigine.demo.utils.Constants.TAG;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.models.BeaconMock;
import com.navigine.navigine.demo.models.UserSession;
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
        handleDeepLink();
        setContentView(R.layout.activity_main);
        initViewModel();
        initNavigationView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void handleDeepLink() {
        Uri data = getIntent().getData();
        if (data != null) {
            try {
                String locationServer = data.getQueryParameter(DL_QUERY_SERVER);
                String userHash = data.getQueryParameter(DL_QUERY_USERHASH);

                UserSession.LOCATION_SERVER = locationServer;
                UserSession.USER_HASH = userHash;

                NavigineSdkManager.initializeSdk();

            } catch (UnsupportedOperationException | NullPointerException e) {
                Log.e(TAG, getString(R.string.err_deep_link_parse));
            }
        }
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
                R.id.nav_host_fragment_activity_main,
                getIntent());
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