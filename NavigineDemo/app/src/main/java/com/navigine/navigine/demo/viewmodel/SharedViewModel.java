package com.navigine.navigine.demo.viewmodel;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationListener;
import com.navigine.navigine.demo.utils.NavigineSdkManager;

public class SharedViewModel extends ViewModel {

    public MutableLiveData<Location> mLocation                  = new MutableLiveData<>(null);
    public MutableLiveData<Boolean>  mNetworkAvailable          = new MutableLiveData<>(false);

    private LocationListener locationListener = null;

    public SharedViewModel() {

        locationListener = new LocationListener() {
            @Override
            public void onLocationLoaded(Location location) {
                mLocation.postValue(location);
            }

            @Override
            public void onLocationFailed(int i, Error error) {
            }

            @Override
            public void onLocationUploaded(int i) {
            }

        };

        NavigineSdkManager.LocationManager.addLocationListener(locationListener);
    }

    public void checkNetworkConnection(ConnectivityManager connectivityManager) {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                mNetworkAvailable.postValue(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                mNetworkAvailable.postValue(false);
            }
        };

        connectivityManager.requestNetwork(networkRequest, networkCallback);

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        NavigineSdkManager.LocationManager.removeLocationListener(locationListener);
    }
}
