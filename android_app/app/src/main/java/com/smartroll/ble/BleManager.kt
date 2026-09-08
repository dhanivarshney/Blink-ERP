package com.smartroll.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log

class BleManager(private val context: Context) {

    companion object {
        val SMARTROLL_UUID: java.util.UUID = java.util.UUID.fromString("12345678-1234-1234-1234-123456789abc")
        val DATA_UUID: ParcelUuid = ParcelUuid.fromString("0000FF01-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bleScanner: BluetoothLeScanner? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var activeScanCallback: ScanCallback? = null
    private var activeAdvertiseCallback: AdvertiseCallback? = null
    private var isScanning = false
    private var isAdvertising = false

    private var lastFoundTime = 0L

    interface DeviceCallback {
        fun onDeviceFound(name: String, address: String, rssi: Int, id: String)
        fun onScanStopped()
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        stopAdvertising()

        Handler(Looper.getMainLooper()).postDelayed({
            advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
            if (advertiser == null) {
                onError(if (bluetoothAdapter?.isEnabled == false) "Bluetooth is OFF" else "Hardware unsupported")
                return@postDelayed
            }

            if (bluetoothAdapter?.isEnabled != true) {
                onError("Bluetooth OFF hai")
                return@postDelayed
            }

            val data = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(SMARTROLL_UUID))
                .addServiceData(DATA_UUID, userId.take(20).toByteArray()) // Truncate to avoid size limit
                .setIncludeDeviceName(false) // Disable name to save packet space
                .build()

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()

            activeAdvertiseCallback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    isAdvertising = true
                    Log.d("BleManager", "Advertising started with ID: $userId")
                    onSuccess()
                }
                override fun onStartFailure(errorCode: Int) {
                    Log.e("BleManager", "Advertising failed: $errorCode")
                    isAdvertising = false
                    activeAdvertiseCallback = null
                    onError("Error $errorCode")
                }
            }

            try {
                advertiser?.startAdvertising(settings, data, activeAdvertiseCallback)
            } catch (e: SecurityException) {
                onError("Permission denied")
            }
        }, 200) // Fast start
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        if (activeAdvertiseCallback != null) {
            try { advertiser?.stopAdvertising(activeAdvertiseCallback) } catch (_: Exception) {}
            activeAdvertiseCallback = null
        }
        isAdvertising = false
    }

    @SuppressLint("MissingPermission")
    fun startScan(callback: DeviceCallback) {
        bleScanner = bluetoothAdapter?.bluetoothLeScanner
        if (bleScanner == null) {
            callback.onScanStopped()
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SMARTROLL_UUID))
            .build()

        activeScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFoundTime < 2000) return 
                lastFoundTime = currentTime

                val data = result.scanRecord?.getServiceData(DATA_UUID)
                if (data != null && data.isNotEmpty()) {
                    callback.onDeviceFound(
                        result.device.name ?: "Unknown",
                        result.device.address,
                        result.rssi,
                        String(data)
                    )
                }
            }
        }

        try {
            bleScanner?.startScan(listOf(filter), settings, activeScanCallback)
            isScanning = true
        } catch (e: SecurityException) {
            callback.onScanStopped()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan(callback: DeviceCallback) {
        if (!isScanning) return
        try { bleScanner?.stopScan(activeScanCallback) } catch (_: Exception) {}
        isScanning = false
        callback.onScanStopped()
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
}
