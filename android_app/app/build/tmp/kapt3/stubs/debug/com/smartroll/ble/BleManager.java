package com.smartroll.ble;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010#\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u0000 (2\u00020\u0001:\u0002()B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0017\u001a\u00020\u00152\u0006\u0010\u0018\u001a\u00020\u0013J\u0006\u0010\u0019\u001a\u00020\u001aJ\u0006\u0010\u001b\u001a\u00020\u0015J\u000e\u0010\u001c\u001a\u00020\u001a2\u0006\u0010\u0018\u001a\u00020\u0013J4\u0010\u001d\u001a\u00020\u001a2\b\b\u0002\u0010\u001e\u001a\u00020\u00132\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u001a0 2\u0012\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u001a0\"H\u0007J\u0010\u0010#\u001a\u00020\u001a2\u0006\u0010$\u001a\u00020%H\u0007J\b\u0010&\u001a\u00020\u001aH\u0007J\u0010\u0010\'\u001a\u00020\u001a2\u0006\u0010$\u001a\u00020%H\u0007R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/smartroll/ble/BleManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "activeAdvertiseCallback", "Landroid/bluetooth/le/AdvertiseCallback;", "activeScanCallback", "Landroid/bluetooth/le/ScanCallback;", "advertiser", "Landroid/bluetooth/le/BluetoothLeAdvertiser;", "bleScanner", "Landroid/bluetooth/le/BluetoothLeScanner;", "bluetoothAdapter", "Landroid/bluetooth/BluetoothAdapter;", "bluetoothManager", "Landroid/bluetooth/BluetoothManager;", "detectedAddresses", "", "", "isAdvertising", "", "isScanning", "alreadySeen", "address", "clearSeen", "", "isBluetoothEnabled", "markSeen", "startAdvertising", "userId", "onSuccess", "Lkotlin/Function0;", "onError", "Lkotlin/Function1;", "startScan", "callback", "Lcom/smartroll/ble/BleManager$DeviceCallback;", "stopAdvertising", "stopScan", "Companion", "DeviceCallback", "app_debug"})
public final class BleManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.UUID SMARTROLL_SERVICE_UUID = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.UUID SMARTROLL_UUID = null;
    @org.jetbrains.annotations.NotNull()
    private final android.bluetooth.BluetoothManager bluetoothManager = null;
    @org.jetbrains.annotations.Nullable()
    private final android.bluetooth.BluetoothAdapter bluetoothAdapter = null;
    @org.jetbrains.annotations.Nullable()
    private android.bluetooth.le.BluetoothLeScanner bleScanner;
    @org.jetbrains.annotations.Nullable()
    private android.bluetooth.le.BluetoothLeAdvertiser advertiser;
    @org.jetbrains.annotations.Nullable()
    private android.bluetooth.le.ScanCallback activeScanCallback;
    @org.jetbrains.annotations.Nullable()
    private android.bluetooth.le.AdvertiseCallback activeAdvertiseCallback;
    private boolean isScanning = false;
    private boolean isAdvertising = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.String> detectedAddresses = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.smartroll.ble.BleManager.Companion Companion = null;
    
    public BleManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final boolean alreadySeen(@org.jetbrains.annotations.NotNull()
    java.lang.String address) {
        return false;
    }
    
    public final void markSeen(@org.jetbrains.annotations.NotNull()
    java.lang.String address) {
    }
    
    public final void clearSeen() {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void startAdvertising(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onError) {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void stopAdvertising() {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void startScan(@org.jetbrains.annotations.NotNull()
    com.smartroll.ble.BleManager.DeviceCallback callback) {
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    public final void stopScan(@org.jetbrains.annotations.NotNull()
    com.smartroll.ble.BleManager.DeviceCallback callback) {
    }
    
    public final boolean isBluetoothEnabled() {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0011\u0010\u0007\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0006\u00a8\u0006\t"}, d2 = {"Lcom/smartroll/ble/BleManager$Companion;", "", "()V", "SMARTROLL_SERVICE_UUID", "Ljava/util/UUID;", "getSMARTROLL_SERVICE_UUID", "()Ljava/util/UUID;", "SMARTROLL_UUID", "getSMARTROLL_UUID", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.UUID getSMARTROLL_SERVICE_UUID() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.UUID getSMARTROLL_UUID() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J(\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0005H&J\u0010\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\bH\u0016J\b\u0010\f\u001a\u00020\u0003H&\u00a8\u0006\r"}, d2 = {"Lcom/smartroll/ble/BleManager$DeviceCallback;", "", "onDeviceFound", "", "name", "", "address", "rssi", "", "id", "onScanFailed", "errorCode", "onScanStopped", "app_debug"})
    public static abstract interface DeviceCallback {
        
        public abstract void onDeviceFound(@org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String address, int rssi, @org.jetbrains.annotations.NotNull()
        java.lang.String id);
        
        public abstract void onScanStopped();
        
        public abstract void onScanFailed(int errorCode);
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
        public static final class DefaultImpls {
            
            public static void onScanFailed(@org.jetbrains.annotations.NotNull()
            com.smartroll.ble.BleManager.DeviceCallback $this, int errorCode) {
            }
        }
    }
}