package com.navigine.navigine.demo;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navigine.idl.java.MeasurementListener;
import com.navigine.idl.java.SensorMeasurement;
import com.navigine.idl.java.SensorType;
import com.navigine.idl.java.SignalMeasurement;
import com.navigine.idl.java.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DebugFragment extends Fragment {

    private TextView mSignalsTextView;
    private TextView mSensorsTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debug, container, false);

        mSensorsTextView = view.findViewById(R.id.debug_fragment__sensors);
        mSignalsTextView = view.findViewById(R.id.debug_fragment__signals);

        NavigineApp.MeasurementManager.addMeasurementListener(new MeasurementListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onSensorMeasurementDetected(HashMap<SensorType, SensorMeasurement> hashMap) {

                StringBuilder sensorsStringBuilder = new StringBuilder();

                if (hashMap.containsKey(SensorType.ACCELEROMETER)) {
                    SensorMeasurement measurement = hashMap.get(SensorType.ACCELEROMETER);
                    appendSensorMessage(sensorsStringBuilder, "Accelerometer:  ", Objects.requireNonNull(measurement).getValues());
                }
                else
                    sensorsStringBuilder.append("Accelerometer:  ---\n");

                if (hashMap.containsKey(SensorType.MAGNETOMETER)) {
                    SensorMeasurement measurement = hashMap.get(SensorType.MAGNETOMETER);
                    appendSensorMessage(sensorsStringBuilder, "Magnetometer:  ", Objects.requireNonNull(measurement).getValues());
                }
                else
                    sensorsStringBuilder.append("Magnetometer:  ---\n");

                if (hashMap.containsKey(SensorType.GYROSCOPE)) {
                    SensorMeasurement measurement = hashMap.get(SensorType.GYROSCOPE);
                    appendSensorMessage(sensorsStringBuilder, "Gyroscope:          ", Objects.requireNonNull(measurement).getValues());
                }
                else
                    sensorsStringBuilder.append("Gyroscope:          ---\n");

                if (hashMap.containsKey(SensorType.BAROMETER)) {
                    SensorMeasurement measurement = hashMap.get(SensorType.BAROMETER);
                    sensorsStringBuilder.append(String.format(Locale.ENGLISH, "Barometer:           (%.2f)\n",
                            Objects.requireNonNull(measurement).getValues().getX()));
                }
                else
                    sensorsStringBuilder.append("Barometer:           ---\n");

                if (hashMap.containsKey(SensorType.ORIENTATION)) {
                    SensorMeasurement measurement = hashMap.get(SensorType.ORIENTATION);
                    sensorsStringBuilder.append(String.format(Locale.ENGLISH, "Orientation:         (%.2f)\n",
                            Objects.requireNonNull(measurement).getValues().getX() * 180 / 3.14159f));
                }
                else
                    sensorsStringBuilder.append("Orientation:         ---\n");

                sensorsStringBuilder.append("\n");
                mSensorsTextView.setText(sensorsStringBuilder.toString());
            }

            @Override
            public void onSignalMeasurementDetected(HashMap<String, SignalMeasurement> hashMap) {
                List<SignalMeasurement> wifiEntries   = new ArrayList<>();
                List<SignalMeasurement> rttEntries    = new ArrayList<>();
                List<SignalMeasurement> bleEntries    = new ArrayList<>();
                List<SignalMeasurement> beaconEntries = new ArrayList<>();
                List<SignalMeasurement> eddyEntries   = new ArrayList<>();

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

                Collections.sort(wifiEntries, (result1, result2) -> result1.getRssi() > result2.getRssi() ? -1 : result1.getRssi() < result2.getRssi() ? 1 : 0);
                Collections.sort(rttEntries, (result1, result2) -> result1.getRssi() > result2.getRssi() ? -1 : result1.getRssi() < result2.getRssi() ? 1 : 0);
                Collections.sort(bleEntries, (result1, result2) -> result1.getRssi() > result2.getRssi() ? -1 : result1.getRssi() < result2.getRssi() ? 1 : 0);
                Collections.sort(beaconEntries, (result1, result2) -> result1.getRssi() > result2.getRssi() ? -1 : result1.getRssi() < result2.getRssi() ? 1 : 0);
                Collections.sort(eddyEntries, (result1, result2) -> result1.getRssi() > result2.getRssi() ? -1 : result1.getRssi() < result2.getRssi() ? 1 : 0);

                StringBuilder signalsMessageBuilder = new StringBuilder();

                signalsMessageBuilder.append(String.format(Locale.ENGLISH, "Wi-Fi networks (%d)\n",
                        wifiEntries.size()));

                for (int i = 0; i < 5; i++)
                {
                    try
                    {
                        SignalMeasurement result = wifiEntries.get(i);
                        String name = result.getId().length() <= 13 ? result.getId() : result.getId().substring(0, 12) + "…";
                        signalsMessageBuilder.append(String.format(Locale.ENGLISH, "%s  %.1f  %s\n", result.getId(),
                                (float) result.getRssi(),
                                name));
                    } catch (IndexOutOfBoundsException e)
                    {
                        signalsMessageBuilder.append("---\n");
                    }
                }
                signalsMessageBuilder.append(wifiEntries.size() > 5 ? "...\n\n" : "\n\n");

                signalsMessageBuilder.append(String.format(Locale.ENGLISH, "Rtt (%d)\n",
                        rttEntries.size()));

                for (int i = 0; i < 5; i++)
                {
                    try
                    {
                        SignalMeasurement result = rttEntries.get(i);
                        signalsMessageBuilder.append(String.format(Locale.ENGLISH, "%s  %.1f (%.1fm)\n", result.getId(),
                                (float) result.getRssi(), result.getDistance()));
                    } catch (IndexOutOfBoundsException e)
                    {
                        signalsMessageBuilder.append("---\n");
                    }
                }
                signalsMessageBuilder.append(rttEntries.size() > 5 ? "...\n\n" : "\n\n");

                signalsMessageBuilder.append(String.format(Locale.ENGLISH, "BEACONS (%d)\n",
                        beaconEntries.size()));

                for (int i = 0; i < 5; i++)
                {
                    try
                    {
                        SignalMeasurement result = beaconEntries.get(i);
                        String address = result.getId().substring(1, 17) + "…";
                        signalsMessageBuilder.append(
                                String.format(Locale.ENGLISH, "%s  %.1f  %.1fm\n", address,
                                        (float) result.getRssi(), result.getDistance()));
                    } catch (IndexOutOfBoundsException e)
                    {
                        signalsMessageBuilder.append("---\n");
                    }
                }
                signalsMessageBuilder.append(beaconEntries.size() > 5 ? "...\n\n" : "\n\n");

                signalsMessageBuilder.append(String.format(Locale.ENGLISH, "BLE devices (%d)\n",
                        bleEntries.size()));

                for (int i = 0; i < 5; i++)
                {
                    try
                    {
                        SignalMeasurement result = bleEntries.get(i);
                        signalsMessageBuilder.append(
                                String.format(Locale.ENGLISH, "%s  %.1f\n", result.getId(), (float) result.getRssi()));
                    } catch (IndexOutOfBoundsException e)
                    {
                        signalsMessageBuilder.append("---\n");
                    }
                }
                signalsMessageBuilder.append(bleEntries.size() > 5 ? "...\n\n" : "\n\n");

                signalsMessageBuilder.append(String.format(Locale.ENGLISH, "EDDYSTONE (%d)\n",
                        eddyEntries.size()));

                for (int i = 0; i < 5; i++)
                {
                    try
                    {
                        SignalMeasurement result = eddyEntries.get(i);
                        String[] ids = result.getId().split(",");
                        String address = ids[0].substring(1, 15 - ids[1].length()) + "…, " + ids[1].substring(0, ids[1].length() - 1);
                        signalsMessageBuilder.append(
                                String.format(Locale.ENGLISH, "%s  %.1f  %.1fm\n", address,
                                        (float) result.getRssi(), result.getDistance()));
                    } catch (IndexOutOfBoundsException e)
                    {
                        signalsMessageBuilder.append("---\n");
                    }
                }
                signalsMessageBuilder.append(eddyEntries.size() > 5 ? "...\n" : "\n");

                mSignalsTextView.setText(signalsMessageBuilder.toString());
            }
        });

        return view;
    }

    private void appendSensorMessage(StringBuilder stringBuilder, String sensorName, Vector3d values)
    {
        stringBuilder.append(String.format(Locale.ENGLISH, "%s(%.4f, %.4f, %.4f)\n",
                sensorName, values.getX(), values.getY(), values.getZ()));

    }
}
