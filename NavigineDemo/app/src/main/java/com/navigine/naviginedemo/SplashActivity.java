package com.navigine.naviginedemo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationListener;
import com.navigine.idl.java.LocationManager;
import com.navigine.idl.java.MeasurementManager;
import com.navigine.idl.java.NavigationManager;
import com.navigine.idl.java.NavigineSdk;
import com.navigine.idl.java.SensorMeasurement;
import com.navigine.idl.java.Vector3d;
import com.navigine.sdk.Navigine;

import static com.navigine.idl.java.SensorType.ACCELEROMETER;

public class SplashActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    TextView mStatusLabel;

    NavigineSdk mNavigineSdk;
    LocationListener mLocationListener;
    LocationManager mLocationManager;
    NavigationManager mNavigationManager;
    MeasurementManager mMeasurementManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        mStatusLabel = findViewById(R.id.splash__status_label);

        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        boolean permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean permissionStorage  = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (requestCode == 101) {
            if (!permissionLocation || (D.WRITE_LOGS && !permissionStorage))
                finish();
            else {
                Navigine.initialize(getApplicationContext());
                NavigineSdk.setServer(D.SERVER_URL);
                NavigineSdk.setUserHash(D.USER_HASH);
                mNavigineSdk = NavigineSdk.getInstance();
                mLocationManager = mNavigineSdk.getLocationManager();
                mNavigationManager = mNavigineSdk.getNavigationManager(mLocationManager);
                mLocationListener = new LocationListener() {
                    @Override
                    public void onLocationLoaded(Location location) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDownloadProgress(int progress, int total) {
                        mStatusLabel.setText("Downloading location: " + (progress * 100 / total) + "%");
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onLocationFailed(Error error) {
                        mStatusLabel.setText("Error initializing Navigine SDK! Please, contact technical support");
                    }
                };
                mLocationManager.addLocationListener(mLocationListener);
                mLocationManager.setLocationId(D.LOCATION_ID);

                mMeasurementManager = mNavigineSdk.getMeasurementManager();
                Vector3d values = new Vector3d(0, 0, 0);
                mMeasurementManager.addExternalSensorMeasurement(new SensorMeasurement(ACCELEROMETER, values));
            }
        }
    }
}
