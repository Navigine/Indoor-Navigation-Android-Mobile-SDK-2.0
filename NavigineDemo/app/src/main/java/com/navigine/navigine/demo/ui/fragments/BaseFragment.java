package com.navigine.navigine.demo.ui.fragments;

import static com.navigine.navigine.demo.utils.Constants.LOCATION_CHANGED;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.utils.PermissionUtils;


public abstract class BaseFragment extends Fragment {

    private LocationManager  locationManager  = null;
    private BluetoothManager bluetoothManager = null;
    private BluetoothAdapter bluetoothAdapter = null;

    private StateReceiver receiver = null;
    private IntentFilter  filter   = null;

    protected String bluetoothState   = null;
    protected String geoLocationState = null;

    private BottomNavigationView mNavigationView = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSystemServices();
        initBroadcastReceiver();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mNavigationView = requireActivity().findViewById(R.id.main__bottom_navigation);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            updateStatusBar();
            updateUiState();
            updateWarningMessageState();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        updateGeolocationState();
        updateBluetoothState();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    protected abstract void updateStatusBar();

    protected void updateUiState() { }

    protected void updateWarningMessageState() { }

    protected void onGpsStateChanged() {
        updateGeolocationState();
        updateWarningMessageState();
    }

    protected void onBluetoothStateChanged() {
        updateBluetoothState();
        updateWarningMessageState();
    }

    private void updateGeolocationState() {
        geoLocationState = isGpsEnabled() ? getString(R.string.state_on) : getString(R.string.state_off);
    }

    private void updateBluetoothState() {
        bluetoothState = isBluetoothEnabled() ? getString(R.string.state_on) : getString(R.string.state_off);
    }

    private void initSystemServices() {
        locationManager  = (LocationManager)  requireActivity().getSystemService(Context.LOCATION_SERVICE);
        bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void initBroadcastReceiver() {
        receiver = new StateReceiver();
        filter   = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(LOCATION_CHANGED);
    }

    private void registerReceiver() {
        requireActivity().registerReceiver(receiver, filter);
    }

    private void unregisterReceiver() {
        requireActivity().unregisterReceiver(receiver);
    }

    protected boolean isGpsEnabled() {
        if (locationManager != null) {
            boolean isGpsEnabled     = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return  isGpsEnabled || isNetworkEnabled;
        }
        else
            return false;
    }

    protected boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    protected boolean hasLocationPermission() {
        return PermissionUtils.hasLocationPermission(requireActivity());
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    protected boolean hasBluetoothPermission() {
        return PermissionUtils.hasBluetoothPermission(requireActivity());
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected boolean hasBackgroundLocationPermission() {
        return PermissionUtils.hasLocationBackgroundPermission(requireActivity());
    }

    protected void openLocationsScreen() {
        mNavigationView.setSelectedItemId(R.id.navigation_locations);
    }

    private class StateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    onGpsStateChanged();
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    onBluetoothStateChanged();
                    break;
            }
        }
    }
}