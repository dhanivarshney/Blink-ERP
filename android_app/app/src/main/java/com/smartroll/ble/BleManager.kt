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
        val SMARTROLL_SERVICE_UUID: java.util.UUID = java.util.UUID.fromString("0000b00b-0000-1000-8000-00805f9b34fb")
        val SMARTROLL_UUID: java.util.UUID = SMARTROLL_SERVICE_UUID
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bleScanner: BluetoothLeScanner? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var activeScanCallback: ScanCallback? = null
    private var activeAdvertiseCallback: AdvertiseCallback? = null
    private var isScanning = false
    private var isAdvertising = false

    private val detectedAddresses = mutableSetOf<String>()

    fun alreadySeen(address: String): Boolean = address in detectedAddresses
    fun markSeen(address: String) { detectedAddresses.add(address) }
    fun clearSeen() { detectedAddresses.clear() }

    interface DeviceCallback {
        fun onDeviceFound(name: String, address: String, rssi: Int, id: String)
        fun onScanStopped()
        fun onScanFailed(errorCode: Int) {}
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(userId: String = "", onSuccess: () -> Unit, onError: (String) -> Unit) {
        stopAdvertising()

        Handler(Looper.getMainLooper()).postDelayed({
            advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
            if (advertiser == null) {
                onError(if (bluetoothAdapter?.isEnabled == false) "Bluetooth is OFF" else "Hardware unsupported")
                return@postDelayed
            }

            // Fix for Teacher (advertiser) side:
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build()

            val data = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(SMARTROLL_SERVICE_UUID))
                .setIncludeDeviceName(false)   // MUST be false — this is what fixes error 1
                .build()

            activeAdvertiseCallback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    isAdvertising = true
                    Log.d("BleManager", "Teacher beacon advertising started successfully")
                    onSuccess()
                }
                override fun onStartFailure(errorCode: Int) {
                    isAdvertising = false
                    activeAdvertiseCallback = null
                    val errorDetail = when (errorCode) {
                        ADVERTISE_FAILED_DATA_TOO_LARGE -> "ADVERTISE_FAILED_DATA_TOO_LARGE (error 1)"
                        ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS (error 2)"
                        ADVERTISE_FAILED_ALREADY_STARTED -> "ADVERTISE_FAILED_ALREADY_STARTED (error 3)"
                        ADVERTISE_FAILED_INTERNAL_ERROR -> "ADVERTISE_FAILED_INTERNAL_ERROR (error 4)"
                        ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "ADVERTISE_FAILED_FEATURE_UNSUPPORTED (error 5)"
                        else -> "errorCode: $errorCode"
                    }
                    Log.e("BleManager", "Advertising failed: $errorDetail")
                    onError(errorDetail)
                }
            }

            try {
                advertiser?.startAdvertising(settings, data, activeAdvertiseCallback)
            } catch (e: SecurityException) {
                onError("Permission denied: ${e.message}")
            }
        }, 200)
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

        clearSeen()

        // Fix for Student (scanner) side:
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        // Scan with NO filter (emptyList()) to avoid OEM 128-bit filter drop issue
        activeScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val isOurs = result.scanRecord?.serviceUuids?.any {
                    it.uuid == SMARTROLL_SERVICE_UUID
                } == true
                if (!isOurs) return  // ignore unrelated nearby BLE devices

                // de-duplicate so we don't fire multiple times for the same device
                val addr = result.device.address
                if (alreadySeen(addr)) return
                markSeen(addr)

                val deviceName = try {
                    result.device.name ?: "Teacher"
                } catch (_: SecurityException) {
                    "Teacher"
                }

                callback.onDeviceFound(
                    deviceName,
                    addr,
                    result.rssi,
                    "TEACHER"
                )
            }

            override fun onScanFailed(errorCode: Int) {
                val errorDetail = when (errorCode) {
                    SCAN_FAILED_ALREADY_STARTED -> "SCAN_FAILED_ALREADY_STARTED (error 1)"
                    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED (error 2)"
                    SCAN_FAILED_INTERNAL_ERROR -> "SCAN_FAILED_INTERNAL_ERROR (error 3)"
                    SCAN_FAILED_FEATURE_UNSUPPORTED -> "SCAN_FAILED_FEATURE_UNSUPPORTED (error 4)"
                    else -> "errorCode: $errorCode"
                }
                Log.e("BleManager", "Scan failed: $errorDetail")
                callback.onScanFailed(errorCode)
            }
        }

        try {
            bleScanner?.startScan(emptyList(), settings, activeScanCallback)  // emptyList() = no filter
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
