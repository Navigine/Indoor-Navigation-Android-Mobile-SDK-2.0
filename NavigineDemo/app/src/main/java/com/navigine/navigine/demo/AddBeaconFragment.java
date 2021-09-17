package com.navigine.navigine.demo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationEditListener;
import com.navigine.idl.java.LocationListener;
import com.navigine.idl.java.Point;

import static com.navigine.navigine.demo.NavigineApp.LocationManager;

public class AddBeaconFragment extends Fragment {

    Location mLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_beacon, container, false);

//        LocationEditManager.addLocationEditListener(new LocationEditListener() {
//            @Override
//            public void onLocationUploaded() {
//                System.out.println("Location upload finished!");
//            }
//
//            @Override
//            public void onUploadProgress(int i, int i1) {
//
//            }
//
//            @Override
//            public void onLocationEditError(Error error) {
//                System.out.println("Error while editing location: " + error.getMessage());
//            }
//        });
//
//        LocationManager.addLocationListener(new LocationListener() {
//            @Override
//            public void onLocationLoaded(Location location) {
//                mLocation = location;
//                //LocationEditManager.removeWifi(location.getSublocations().get(0).getId(), "001122334466");
//                LocationEditManager.editWifi(location.getSublocations().get(0).getId(), "001122334466", new Point(0.1f, 0.1f), "Mod");
//                LocationEditManager.commitChanges();
//            }
//
//            @Override
//            public void onUploadProgress(int i, int i1) { }
//
//            @Override
//            public void onLocationFailed(Error error) { }
//        });

        return view;
    }
}
