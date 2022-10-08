package com.navigine.navigine.demo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationListener;
import com.navigine.navigine.demo.utils.NavigineSdkManager;

public class SharedViewModel extends ViewModel {

    public MutableLiveData<Location> mLocation = new MutableLiveData<>(null);

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

    @Override
    protected void onCleared() {
        super.onCleared();
        NavigineSdkManager.LocationManager.removeLocationListener(locationListener);
    }
}
