package com.navigine.examples.MeasurementManager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.navigine.examples.databinding.ActivityMeasurementBinding
import com.navigine.idl.java.MeasurementListener
import com.navigine.idl.java.MeasurementManager
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.SensorMeasurement
import com.navigine.idl.java.SensorType
import com.navigine.idl.java.SignalMeasurement
import com.navigine.sdk.Navigine
import java.util.HashMap

/**
 * Activity demonstrating how to use the Navigine SDK MeasurementManager
 * to collect sensor and signal measurements (Wi-Fi, beacons, Bluetooth, etc.).
 */
class MeasurementActivity : AppCompatActivity() {

    private var _binding: ActivityMeasurementBinding? = null
    private val binding get() = _binding!!

    private var measurementManager: MeasurementManager? = null
    private var measurementListener: MeasurementListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMeasurementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSdk()
        initListener()
        addListener()
    }

    override fun onResume() {
        super.onResume()
        addGenerators()
    }

    override fun onStop() {
        super.onStop()
        removeListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initSdk() {
        val sdk = NavigineSdk.getInstance()
        // your server URL
        sdk.setServer("https://ips.navigine.com")
        // your user hash
        sdk.setUserHash("0000-0000-0000-0000")

        val locationManager = sdk.locationManager
        measurementManager = sdk.getMeasurementManager(locationManager)
    }

    /**
     * Sets up the MeasurementListener to receive sensor and signal updates.
     */
    private fun initListener(){
        measurementListener = object : MeasurementListener() {
            override fun onSensorMeasurementDetected(signalMap: HashMap<SensorType, SensorMeasurement>?) {
                /**
                 * Called when raw sensor measurements are available.
                 *
                 * @param measurements Map of SensorType to SensorMeasurement containing:
                 *                     - data.vector3D: x, y, z values for the sensor
                 *                     - timestamp: time of measurement in milliseconds
                 *
                 * SensorType examples:
                 * - ACCELEROMETER: device acceleration
                 * - GYROSCOPE: angular velocity
                 * - MAGNETOMETER: magnetic field strength
                 * - BAROMETER: air pressure
                 * - ORIENTATION: device orientation angles
                 *
                 * Use this callback to process or visualize inertial sensor data.
                 */
                signalMap?.let { map ->
                    val text = buildString {
                        map.forEach { (sensorType, sensorMeasurement) ->
                            appendLine("$sensorType: $sensorMeasurement")
                        }
                    }
                    binding.measurementSensorTextView.text = text
                }
            }
            override fun onSignalMeasurementDetected(sensorMap: HashMap<String, SignalMeasurement>?) {
                /**
                 * Called when signal-based measurements (e.g., Wi-Fi, BLE, beacons) are detected.
                 *
                 * @param signals Map of signal identifiers to SignalMeasurement containing:
                 *                - rssi: Received Signal Strength Indicator (dBm)
                 *                - distance: estimated distance from source (meters)
                 *                - timestamp: detection time in milliseconds
                 *
                 * Signal identifiers:
                 * - Wi-Fi/Bluetooth: MAC address string ("00:11:22:33:44:55")
                 * - iBeacon: UUID_(major):(minor) ("uuid_1:2")
                 * - Eddystone: namespace + instance ID string
                 * - RTT: Wi-Fi RTT measurement sessions
                 *
                 * Use this callback to handle proximity or ranging data for nearby devices.
                 */
                sensorMap?.let { map ->
                    val text = buildString {
                        map.forEach { (id, sensorMeasurement) ->
                            appendLine("$id: $sensorMeasurement")
                        }
                    }
                    binding.measurementSignalTextView.text = text
                }
            }
        }
    }

    private fun addListener(){
        measurementManager?.addMeasurementListener(measurementListener)
    }

    private fun removeListener(){
        measurementManager?.removeMeasurementListener(measurementListener)
    }

    /**
     * You can add or remove generators with the following methods
     * addBeaconGenerator(uuid, major, minor, power, timeout, rssiMin, rssiMax)
     * addEddystoneGenerator(namespace, instanceId, power, timeout, rssiMin, rssiMax)
     * addWifiGenerator(mac, timeout, rssiMin, rssiMax)
     * addLocationGenerator(latMin, latMax, lonMin, lonMax, accMin, accMax, timeout)
     */
    private fun addGenerators(
    ){
        measurementManager?.addBeaconGenerator(
            "00000000-0000-0000-0000-000000000000",
            0,0,0,0,0,0
        )
        measurementManager?.addEddystoneGenerator(
            "namespace","instanceId",0,0,0,0
        )
        measurementManager?.addWifiGenerator(
            "mac",0,0,0
        )
        measurementManager?.addLocationGenerator(
            0.0,0.0,0.0,0.0,0.0,0.0,0
        )
    }
}