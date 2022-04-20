package com.navigine.navigine.demo.ui.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.application.NavigineApp;
import com.navigine.navigine.demo.model.BeaconMock;
import com.navigine.navigine.demo.ui.custom.SaveStateBottomNavigation;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SaveStateBottomNavigation mBottomNavigation = null;
    private NavHostFragment           navHostFragment   = null;
    private List<Integer>             navGraphIds       = null;
    private LiveData<NavController>   mNavController    = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (NavigineApp.mNavigineSdk == null) {
            finish();
            return;
        }

        if (savedInstanceState == null)
            initNavigationView();

        // if you do not have a real beacon device, you can simulate the operation of it's
//         addBeaconGenerator();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        initNavigationView();
    }


    private void initNavigationView() {
        mBottomNavigation = findViewById(R.id.main__bottom_navigation);
        navGraphIds = Arrays.asList(
                R.navigation.navigation_locations,
                R.navigation.navigation_navigation,
                R.navigation.navigation_debug,
                R.navigation.navigation_profile);

        mNavController = mBottomNavigation.setupWithNavController(
                navGraphIds,
                getSupportFragmentManager(),
                R.id.nav_host_fragment_activity_main,
                getIntent());
    }

    /*
     * Add a beacon generator, that emulates the operation of a real beacon.
     * Identifiers (uuid, major, minor) of a specific beacon are taken from your location on the client.navigine.com.
     * Params: s  - beacon uuid
     *         i  - beacon major value
     *         i1 - beacon minor value
     *         i2 - beacon power
     *         i3 - timeout
     *         i4 - rssi min
     *         i5 - rssi max
     */
    private void addBeaconGenerator() {
        NavigineApp.MeasurementManager.addBeaconGenerator(
                BeaconMock.UUID,
                BeaconMock.MAJOR,
                BeaconMock.MINOR,
                BeaconMock.POWER,
                BeaconMock.TIMEOUT,
                BeaconMock.RSSI_MIN,
                BeaconMock.RSSI_MAX);
    }
}
