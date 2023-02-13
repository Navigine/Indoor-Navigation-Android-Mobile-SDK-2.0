package com.navigine.navigine.demo.ui.fragments;


import static com.navigine.navigine.demo.utils.Constants.TAG;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationPoint;
import com.navigine.idl.java.MeasurementListener;
import com.navigine.idl.java.Position;
import com.navigine.idl.java.PositionListener;
import com.navigine.idl.java.SensorMeasurement;
import com.navigine.idl.java.SensorType;
import com.navigine.idl.java.SignalMeasurement;
import com.navigine.idl.java.Vector3d;
import com.navigine.navigine.demo.BuildConfig;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterBase;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterBeacons;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterBle;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterEddystone;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterInfo;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterRtt;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterSensors;
import com.navigine.navigine.demo.adapters.debug.DebugAdapterWifi;
import com.navigine.navigine.demo.utils.NavigineSdkManager;
import com.navigine.navigine.demo.viewmodel.SharedViewModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DebugFragment extends BaseFragment
{
    private static String OS_VERSION = "UNKNOWN";
    public static final int DEBUG_TIMEOUT_NO_SIGNAL = 5000;

    private SharedViewModel viewModel = null;

    private Window            window             = null;
    private NestedScrollView  mRootView          = null;
    private RecyclerView      mListViewInfo      = null;
    private RecyclerView      mListViewBeacons   = null;
    private RecyclerView      mListViewWifi      = null;
    private RecyclerView      mListViewEddystone = null;
    private RecyclerView      mListViewRtt       = null;
    private RecyclerView      mListViewBle       = null;
    private RecyclerView      mListViewSensors   = null;

    private ArrayList<String[]>     infoEntries   = new ArrayList<>();
    private List<SignalMeasurement> beaconEntries = new ArrayList<>();
    private List<SignalMeasurement> wifiEntries   = new ArrayList<>();
    private List<SignalMeasurement> eddyEntries   = new ArrayList<>();
    private List<SignalMeasurement> rttEntries    = new ArrayList<>();
    private List<SignalMeasurement> bleEntries    = new ArrayList<>();
    private List<String[]>          sensorEntries = new ArrayList<>();


    private Location mLocation = null;

    private DebugAdapterInfo      debugInfoAdapter      = null;
    private DebugAdapterBeacons   debugBeaconsAdapter   = null;
    private DebugAdapterWifi      debugWifiAdapter      = null;
    private DebugAdapterEddystone debugEddystoneAdapter = null;
    private DebugAdapterRtt       debugRttAdapter       = null;
    private DebugAdapterBle       debugBleAdapter       = null;
    private DebugAdapterSensors   debugSensorsAdapter   = null;

    private DividerItemDecoration mDivider  = null;

    private PositionListener    mPositionListener    = null;
    private MeasurementListener mMeasurementListener = null;

    private long timestampBeacons    = 0L;
    private long timestampEddystones = 0L;
    private long timestampBle        = 0L;

    private static final String TEST_DEVICE_ID = UUID.randomUUID().toString();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getOsVersion();
        initViewModels();
        initAdapters();
        initListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_debug, container, false);

        initViews(view);
        setViewsParams();
        setAdapters();
        setAdaptersParams();
        setObservers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        addListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeListeners();
    }


    @Override
    protected void updateStatusBar() {
        window.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.colorBackground));
    }

    private void getOsVersion() {
        Field[] fields = Build.VERSION_CODES.class.getFields();

        for (Field field : fields) {
            try {
                if (field.getInt(Build.VERSION_CODES.class) == Build.VERSION.SDK_INT) {
                    OS_VERSION = field.getName();
                }
            } catch (IllegalAccessException e) {
                Log.e(TAG, getString(R.string.err_debug_os_version));
            }
        }
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

    private void initListeners() {

        mPositionListener = new PositionListener() {

            @Override
            public void onPositionUpdated(Position position) { updateInfoGeneral(position); }

            @Override
            public void onPositionError(Error error) { updateInfoGeneral(null); }
        };

        mMeasurementListener = new MeasurementListener() {

            @Override
            public void onSensorMeasurementDetected(HashMap<SensorType, SensorMeasurement> hashMap) {

                sensorEntries.clear();

                if (hashMap.containsKey(SensorType.ACCELEROMETER))
                {
                    SensorMeasurement measurement = hashMap.get(SensorType.ACCELEROMETER);

                    if (measurement != null)
                    {
                        Vector3d values = measurement.getValues();
                        sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_1), String.format(Locale.ENGLISH, "%.4f, %.4f, %.4f", values.getX(), values.getY(), values.getZ())});
                    }
                    else
                        sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_1), "---"});
                }
                else
                    sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_1), "---"});

                if (hashMap.containsKey(SensorType.MAGNETOMETER))
                {
                    SensorMeasurement measurement = hashMap.get(SensorType.MAGNETOMETER);

                    if (measurement != null)
                    {
                        Vector3d values = measurement.getValues();
                        sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_2), String.format(Locale.ENGLISH, "%.4f, %.4f, %.4f", values.getX(), values.getY(), values.getZ())});
                    }
                    else
                        sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_2), "---"});
                }
                else
                    sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_2), "---"});

                if (hashMap.containsKey(SensorType.GYROSCOPE))
                {
                    SensorMeasurement measurement = hashMap.get(SensorType.GYROSCOPE);

                    if (measurement != null)
                    {
                        Vector3d values = measurement.getValues();
                        sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_3), String.format(Locale.ENGLISH, "%.4f, %.4f, %.4f", values.getX(), values.getY(), values.getZ())});
                    }
                    else
                        sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_3), "---"});
                }
                else
                    sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_3), "---"});

                if (hashMap.containsKey(SensorType.BAROMETER))
                {
                    SensorMeasurement measurement = hashMap.get(SensorType.BAROMETER);

                    if (measurement != null)
                    {
                        Vector3d values = measurement.getValues();
                        sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_4), String.format(Locale.ENGLISH, "%.2f", values.getX())});
                    }
                    else
                        sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_4), "---"});
                }
                else
                    sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_4), "---"});

                if (hashMap.containsKey(SensorType.ORIENTATION))
                {
                    SensorMeasurement measurement = hashMap.get(SensorType.ORIENTATION);

                    if (measurement != null)
                    {
                        Vector3d values = measurement.getValues();
                        sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_5), String.format(Locale.ENGLISH, "%.2f", values.getX() * 180 / 3.14159f)});
                    }
                    else
                        sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_5), "---"});
                }
                else
                    sensorEntries.add(new String[]{getString(R.string.debug_sensor_field_5), "---"});

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

                Collections.sort(wifiEntries,   (result1, result2) -> Float.compare(result2.getRssi(), result1.getRssi()));
                Collections.sort(rttEntries,    (result1, result2) -> Float.compare(result2.getRssi(), result1.getRssi()));
                Collections.sort(bleEntries,    (result1, result2) -> Float.compare(result2.getRssi(), result1.getRssi()));
                Collections.sort(beaconEntries, (result1, result2) -> Float.compare(result2.getRssi(), result1.getRssi()));
                Collections.sort(eddyEntries,   (result1, result2) -> Float.compare(result2.getRssi(), result1.getRssi()));

                if (!wifiEntries.isEmpty()) {
                    debugWifiAdapter.submit(wifiEntries);
                }
                if (!rttEntries.isEmpty()) {
                    debugRttAdapter.submit(rttEntries);
                }

                if (!beaconEntries.isEmpty()) {
                    timestampBeacons = System.currentTimeMillis();
                    debugBeaconsAdapter.submit(beaconEntries);
                } else if (System.currentTimeMillis() - timestampBeacons >= DEBUG_TIMEOUT_NO_SIGNAL)
                    debugBeaconsAdapter.submit(Collections.EMPTY_LIST);

                if (!bleEntries.isEmpty()) {
                    timestampBle = System.currentTimeMillis();
                    debugBleAdapter.submit(bleEntries);
                } else if (System.currentTimeMillis() - timestampBle >= DEBUG_TIMEOUT_NO_SIGNAL)
                    debugBleAdapter.submit(Collections.EMPTY_LIST);

                if (!eddyEntries.isEmpty()) {
                    timestampEddystones = System.currentTimeMillis();
                    debugEddystoneAdapter.submit(eddyEntries);
                } else if (System.currentTimeMillis() - timestampEddystones >= DEBUG_TIMEOUT_NO_SIGNAL)
                    debugEddystoneAdapter.submit(Collections.EMPTY_LIST);
            }
        };
    }

    private void initViews(View view) {
        window             = requireActivity().getWindow();
        mRootView          = view.findViewById(R.id.debug__root);
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
        mDivider.setDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.divider_transparent_list_item));
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

    private void setAdaptersParams() {
        DebugAdapterBase.setRootView(mRootView);
    }

    private void setObservers() {
        viewModel.mLocation.observe(getViewLifecycleOwner(), location -> mLocation = location);
    }

    private void addListeners() {
        NavigineSdkManager.NavigationManager.addPositionListener(mPositionListener);
        NavigineSdkManager.MeasurementManager.addMeasurementListener(mMeasurementListener);
    }

    private void removeListeners() {
        NavigineSdkManager.NavigationManager.removePositionListener(mPositionListener);
        NavigineSdkManager.MeasurementManager.removeMeasurementListener(mMeasurementListener);
    }

    private void updateInfoGeneral(@Nullable Position position) {
        infoEntries.clear();

        infoEntries.add(new String[]{getString(R.string.debug_info_field_1), String.format(Locale.ENGLISH, "%s", BuildConfig.VERSION_NAME)});
        infoEntries.add(new String[]{getString(R.string.debug_info_field_2), String.format("%s", TEST_DEVICE_ID)});

        if (mLocation != null)
        {
            infoEntries.add(new String[]{getString(R.string.debug_info_field_3), String.format(Locale.ENGLISH, "%s v. %s", mLocation.getName(), mLocation.getVersion())});
        }
        else
        {
            infoEntries.add(new String[]{getString(R.string.debug_info_field_3), "---"});
        }
        if (position != null)
        {
            LocationPoint lp = position.getLocationPoint();
            if (lp != null) {
                infoEntries.add(new String[]{getString(R.string.debug_info_field_4), String.format(Locale.ENGLISH, "%d/%d, x=%.1f, y=%.1f", lp.getLocationId(), lp.getSublocationId(),
                        lp.getPoint().getX(), lp.getPoint().getY())});
            } else {
                infoEntries.add(new String[]{getString(R.string.debug_info_field_4), String.format(Locale.ENGLISH, "-/-, lat=%.1f, lon=%.1f",
                        position.getPoint().getLatitude(), position.getPoint().getLongitude())});
            }
        }
        else
        {
            infoEntries.add(new String[]{getString(R.string.debug_info_field_4), "---"});
        }

        infoEntries.add(new String[]{String.format("%s: %s", getString(R.string.debug_info_field_5_1), bluetoothState), String.format("%s: %s", getString(R.string.debug_info_field_5_2), geoLocationState)});
        infoEntries.add(new String[]{getString(R.string.debug_info_field_6), String.format(Locale.ENGLISH, "%s [ %s (%s) ] ", Build.MODEL, Build.VERSION.RELEASE, OS_VERSION)});

        debugInfoAdapter.submit(infoEntries);
    }

}