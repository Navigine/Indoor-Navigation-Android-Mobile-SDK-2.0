package com.navigine.navigine.demo.ui.fragments;

import static com.navigine.navigine.demo.utils.Constants.LOCATION_CHANGED;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.navigine.idl.java.Location;
import com.navigine.idl.java.MeasurementListener;
import com.navigine.idl.java.Position;
import com.navigine.idl.java.PositionListener;
import com.navigine.idl.java.SensorMeasurement;
import com.navigine.idl.java.SensorType;
import com.navigine.idl.java.SignalMeasurement;
import com.navigine.idl.java.Vector3d;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterBeacons;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterBle;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterEddystone;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterInfo;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterRtt;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterSensors;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterWifi;
import com.navigine.navigine.demo.utils.DeviceInfoProvider;
import com.navigine.navigine.demo.utils.NavigineSdkManager;
import com.navigine.navigine.demo.viewmodel.SharedViewModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DebugFragment extends Fragment
{
    private static String OS_VERSION = "UNKNOWN";

    // Constants
    private static final String TAG                    = "NAVIGINE.Debug";
    private static final int    UPDATE_TIMEOUT         = 500;
    private static final long   SIGNALS_UPDATE_TIMEOUT = 1_000;

    private SharedViewModel viewModel = null;

    // UI parameters
    private Window              window                 = null;
    private RecyclerView        mListViewInfo          = null;
    private RecyclerView        mListViewBeacons       = null;
    private RecyclerView        mListViewWifi          = null;
    private RecyclerView        mListViewEddystone     = null;
    private RecyclerView        mListViewRtt           = null;
    private RecyclerView        mListViewBle           = null;
    private RecyclerView        mListViewSensors       = null;
    private TimerTask           mInfoTimerTask         = null;
    private Handler             mHandler               = new Handler();
    private Timer               mTimer                 = new Timer();

    private ArrayList<String[]>     infoEntries   = new ArrayList<>();
    private List<SignalMeasurement> beaconEntries = new ArrayList<>();
    private List<SignalMeasurement> wifiEntries   = new ArrayList<>();
    private List<SignalMeasurement> eddyEntries   = new ArrayList<>();
    private List<SignalMeasurement> rttEntries    = new ArrayList<>();
    private List<SignalMeasurement> bleEntries    = new ArrayList<>();
    private List<String[]>          sensorEntries = new ArrayList<>();

    private Runnable mInfoRunnable;


    private StateReceiver receiver = null;
    private IntentFilter  filter   = null;

    private LocationManager  locationManager  = null;
    private BluetoothManager bluetoothManager = null;
    private BluetoothAdapter bluetoothAdapter = null;

    private String bluetoothState   = "off";
    private String geoLocationState = "off";

    private Location mLocation = null;


    private DebugAdapterInfo      debugInfoAdapter      = null;
    private DebugAdapterBeacons   debugBeaconsAdapter   = null;
    private DebugAdapterWifi      debugWifiAdapter      = null;
    private DebugAdapterEddystone debugEddystoneAdapter = null;
    private DebugAdapterRtt       debugRttAdapter       = null;
    private DebugAdapterBle       debugBleAdapter       = null;
    private DebugAdapterSensors   debugSensorsAdapter   = null;

    private DividerItemDecoration mDivider  = null;

    private static String AppVersion = "undefined";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getOsVersion();
        getAppVersion();
        initSystemServices();
        initViewModels();
        initAdapters();
        initBroadcastReceiver();
    }

    @SuppressLint("UseRequireInsteadOfGet")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_debug, container, false);

        initViews(view);
        setViewsParams();
        setAdapters();
        setObservers();
        setListeners();
        createRunnables();

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        addListeners();
        tasksCancel();
        tasksInit();
        tasksRun();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            window.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.colorBackground));
    }

    @Override
    public void onResume() {
        super.onResume();
        checkGpsState();
        checkBluetoothState();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        removeListeners();
        tasksCancel();
    }

    private void getOsVersion() {
        Field[] fields = Build.VERSION_CODES.class.getFields();

        for (Field field : fields) {
            try {
                if (field.getInt(Build.VERSION_CODES.class) == Build.VERSION.SDK_INT) {
                    OS_VERSION = field.getName();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void getAppVersion() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
            AppVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error while getting app version");
        }
    }

    private void initSystemServices() {
        locationManager  = (LocationManager)requireActivity().getSystemService(Context.LOCATION_SERVICE);
        bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void initViewModels() {
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    private void initAdapters() {
        debugInfoAdapter      = new DebugAdapterInfo();
        debugBeaconsAdapter   = new DebugAdapterBeacons();
        debugWifiAdapter      = new DebugAdapterWifi();
        debugEddystoneAdapter = new DebugAdapterEddystone();
        debugRttAdapter       = new DebugAdapterRtt();
        debugBleAdapter       = new DebugAdapterBle();
        debugSensorsAdapter   = new DebugAdapterSensors();
    }

    private void initBroadcastReceiver() {
        receiver = new StateReceiver();
        filter   = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(LOCATION_CHANGED);
    }

    private void addListeners() {
        requireActivity().registerReceiver(receiver, filter);
    }

    private void removeListeners() {
        requireActivity().unregisterReceiver(receiver);
    }

    private void initViews(View view) {
        window             = requireActivity().getWindow();
        mListViewInfo      = view.findViewById(R.id.debug__info);
        mListViewBeacons   = view.findViewById(R.id.debug__beacons);
        mListViewWifi      = view.findViewById(R.id.debug__wifi);
        mListViewEddystone = view.findViewById(R.id.debug__eddystone);
        mListViewRtt       = view.findViewById(R.id.debug__rtt);
        mListViewBle       = view.findViewById(R.id.debug__ble);
        mListViewSensors   = view.findViewById(R.id.debug__sensors);
        mDivider           = new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL);
    }

    private void setViewsParams() {
        mDivider.setDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.li_divider_transparent));
        mListViewInfo.     addItemDecoration(mDivider);
        mListViewBeacons.  addItemDecoration(mDivider);
        mListViewWifi.     addItemDecoration(mDivider);
        mListViewEddystone.addItemDecoration(mDivider);
        mListViewRtt.      addItemDecoration(mDivider);
        mListViewBle.      addItemDecoration(mDivider);
        mListViewSensors.  addItemDecoration(mDivider);

        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(requireActivity(), R.anim.layout_animation_fall_down);

        mListViewBeacons.  setLayoutAnimation(animationController);
        mListViewWifi.     setLayoutAnimation(animationController);
        mListViewEddystone.setLayoutAnimation(animationController);
        mListViewRtt.      setLayoutAnimation(animationController);
        mListViewBle.      setLayoutAnimation(animationController);

        updateInfoGeneral(null);
    }

    private void setAdapters() {
        mListViewInfo.     setAdapter(debugInfoAdapter);
        mListViewWifi.     setAdapter(debugWifiAdapter);
        mListViewRtt.      setAdapter(debugRttAdapter);
        mListViewBeacons.  setAdapter(debugBeaconsAdapter);
        mListViewBle.      setAdapter(debugBleAdapter);
        mListViewEddystone.setAdapter(debugEddystoneAdapter);
        mListViewSensors.  setAdapter(debugSensorsAdapter);
    }



    private void setObservers() {
        viewModel.mLocation.observe(getViewLifecycleOwner(), location -> {
            mLocation = location;
            updateInfoGeneral(null);
        });
    }

    private void setListeners() {

        requireActivity().registerReceiver(receiver, filter);

        NavigineSdkManager.NavigationManager.addPositionListener(new PositionListener() {

            @Override
            public void onPositionUpdated(Position position) {
                updateInfoGeneral(position);
            }

            @Override
            public void onPositionError(Error error) {
                updateInfoGeneral(null);
            }
        });

        NavigineSdkManager.MeasurementManager.addMeasurementListener(new MeasurementListener() {

            @Override
            public void onSensorMeasurementDetected(HashMap<SensorType, SensorMeasurement> hashMap) {

                sensorEntries.clear();

                if (hashMap.containsKey(SensorType.ACCELEROMETER))
                {
                    SensorMeasurement measurement = hashMap.get(SensorType.ACCELEROMETER);

                    if (measurement != null)
                    {
                        Vector3d values = measurement.getValues();
                        sensorEntries.add(new String[]{"Accelerometer", String.format(Locale.ENGLISH, "(%.4f, %.4f, %.4f)", values.getX(), values.getY(), values.getZ())});
                    }
                    else
                        sensorEntries.add(new String[]{"Accelerometer", "---"});
                }
                else
                    sensorEntries.add(new String[]{"Accelerometer", "---"});

                if (hashMap.containsKey(SensorType.MAGNETOMETER))
                {
                    SensorMeasurement measurement = hashMap.get(SensorType.MAGNETOMETER);

                    if (measurement != null)
                    {
                        Vector3d values = measurement.getValues();
                        sensorEntries.add(new String[]{"Magnetometer", String.format(Locale.ENGLISH, "(%.4f, %.4f, %.4f)", values.getX(), values.getY(), values.getZ())});
                    }
                    else
                        sensorEntries.add(new String[]{"Magnetometer", "---"});
                }
                else
                    sensorEntries.add(new String[]{"Magnetometer", "---"});

                if (hashMap.containsKey(SensorType.GYROSCOPE))
                {
                    SensorMeasurement measurement = hashMap.get(SensorType.GYROSCOPE);

                    if (measurement != null)
                    {
                        Vector3d values = measurement.getValues();
                        sensorEntries.add(new String[]{"Gyroscope", String.format(Locale.ENGLISH, "(%.4f, %.4f, %.4f)", values.getX(), values.getY(), values.getZ())});
                    }
                    else
                        sensorEntries.add(new String[]{"Gyroscope", "---"});
                }
                else
                    sensorEntries.add(new String[]{"Gyroscope", "---"});

                if (hashMap.containsKey(SensorType.BAROMETER))
                {
                    SensorMeasurement measurement = hashMap.get(SensorType.BAROMETER);

                    if (measurement != null)
                    {
                        Vector3d values = measurement.getValues();
                        sensorEntries.add(new String[]{"Barometer", String.format(Locale.ENGLISH, "%.2f", values.getX())});
                    }
                    else
                        sensorEntries.add(new String[]{"Barometer", "---"});
                }
                else
                    sensorEntries.add(new String[]{"Barometer", "---"});

                if (hashMap.containsKey(SensorType.ORIENTATION))
                {
                    SensorMeasurement measurement = hashMap.get(SensorType.ORIENTATION);

                    if (measurement != null)
                    {
                        Vector3d values = measurement.getValues();
                        sensorEntries.add(new String[]{"Orientation", String.format(Locale.ENGLISH, "%.2f", values.getX() * 180 / 3.14159f)});
                    }
                    else
                        sensorEntries.add(new String[]{"Orientation", "---"});
                }
                else
                    sensorEntries.add(new String[]{"Orientation", "---"});

                if (!sensorEntries.isEmpty())
                {
                    debugSensorsAdapter.submit(sensorEntries);
                }
            }

            @Override
            public void onSignalMeasurementDetected(HashMap<String, SignalMeasurement> hashMap) {

                wifiEntries.  clear();
                rttEntries.   clear();
                bleEntries.   clear();
                beaconEntries.clear();
                eddyEntries.  clear();

                for (SignalMeasurement signal : hashMap.values())
                {

                    switch (signal.getType())
                    {
                        case WIFI:
                            wifiEntries.add(signal);
                            break;
                        case WIFI_RTT:
                            rttEntries.add(signal);
                            break;
                        case BEACON:
                            beaconEntries.add(signal);
                            break;
                        case BLUETOOTH:
                            bleEntries.add(signal);
                            break;
                        case EDDYSTONE:
                            eddyEntries.add(signal);
                            break;
                    }
                }

                Collections.sort(wifiEntries, (result1, result2) ->   Float.compare(result2.getRssi(), result1.getRssi()));
                Collections.sort(rttEntries, (result1, result2) ->    Float.compare(result2.getRssi(), result1.getRssi()));
                Collections.sort(bleEntries, (result1, result2) ->    Float.compare(result2.getRssi(), result1.getRssi()));
                Collections.sort(beaconEntries, (result1, result2) -> Float.compare(result2.getRssi(), result1.getRssi()));
                Collections.sort(eddyEntries, (result1, result2) ->   Float.compare(result2.getRssi(), result1.getRssi()));

                if (!wifiEntries.isEmpty()) {
                    debugWifiAdapter.submit(wifiEntries);
                }
                if (!rttEntries.isEmpty()) {
                    debugRttAdapter.submit(rttEntries);
                }
                if (!beaconEntries.isEmpty()) {
                    debugBeaconsAdapter.submit(beaconEntries);
                }
                if (!bleEntries.isEmpty()) {
                    debugBleAdapter.submit(bleEntries);
                }
                if (!eddyEntries.isEmpty()) {
                    debugEddystoneAdapter.submit(eddyEntries);
                }
            }
        });
    }

    private void updateInfoGeneral(@Nullable Position position) {
        infoEntries.clear();

        infoEntries.add(new String[]{"App version", String.format(Locale.ENGLISH, "%s", AppVersion)});
        infoEntries.add(new String[]{"Device ID", String.format("%s", DeviceInfoProvider.getDeviceId(requireActivity()))});

        if (mLocation != null)
        {
            infoEntries.add(new String[]{"Location", mLocation.getName() + " v. " + mLocation.getVersion()});
        }
        else
        {
            infoEntries.add(new String[]{"Location", "---"});
        }
        if (position != null)
        {
            infoEntries.add(new String[]{"Position", String.format(Locale.ENGLISH, "%d/%d, x=%.1f, y=%.1f", position.getLocationId(), position.getSublocationId(),
                    position.getPoint().getX(), position.getPoint().getY())});
        }
        else
        {
            infoEntries.add(new String[]{"Position", "---"});
        }

        infoEntries.add(new String[]{String.format("%s: %s", "Bluetooth", bluetoothState), String.format("%s: %s", "Geolocation", geoLocationState)});
        infoEntries.add(new String[]{"Device model", Build.MODEL + " [ " + Build.VERSION.RELEASE + "(" + OS_VERSION + ")" + " ] "});
    }


    private void createRunnables() {
        mInfoRunnable = () ->
        {
            if (!infoEntries.isEmpty()) {
                debugInfoAdapter.submit(infoEntries);
                infoEntries.clear();
            }
        };
    }

    private void tasksCancel() {
        if (mInfoTimerTask != null) {
            mInfoTimerTask.cancel();
            mInfoTimerTask = null;
        }
    }

    private void tasksInit() {
        mInfoTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(mInfoRunnable);
            }
        };
    }

    private void tasksRun() {
        mTimer.schedule(mInfoTimerTask, UPDATE_TIMEOUT, UPDATE_TIMEOUT);
    }

    private void checkGpsState() {
        if (locationManager != null) {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled || !isNetworkEnabled) {
                geoLocationState = "off";
            } else
                geoLocationState = "on";

            updateInfoGeneral(null);
        }
    }

    private void checkBluetoothState() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothState = "off";
            } else
                bluetoothState = "on";

            updateInfoGeneral(null);
        }
    }

    private class StateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    checkGpsState();
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    checkBluetoothState();
                    break;
            }
        }
    }

}