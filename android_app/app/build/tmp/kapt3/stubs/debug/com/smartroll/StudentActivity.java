package com.smartroll;

/**
 * BlinkERP — Student Dashboard
 * Join Class → BLE scan → detect teacher → auto-mark attendance
 * Works offline too (saves locally)
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010#\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\t\u0018\u0000 +2\u00020\u00012\u00020\u0002:\u0001+B\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u0011\u001a\u00020\u0012H\u0002J\b\u0010\u0013\u001a\u00020\u0012H\u0002J\u0012\u0010\u0014\u001a\u00020\u00122\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0014J\b\u0010\u0017\u001a\u00020\u0012H\u0014J(\u0010\u0018\u001a\u00020\u00122\u0006\u0010\u0019\u001a\u00020\n2\u0006\u0010\u001a\u001a\u00020\n2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\nH\u0016J-\u0010\u001e\u001a\u00020\u00122\u0006\u0010\u001f\u001a\u00020\u001c2\u000e\u0010 \u001a\n\u0012\u0006\b\u0001\u0012\u00020\n0!2\u0006\u0010\"\u001a\u00020#H\u0016\u00a2\u0006\u0002\u0010$J\u0010\u0010%\u001a\u00020\u00122\u0006\u0010&\u001a\u00020\u001cH\u0016J\b\u0010\'\u001a\u00020\u0012H\u0016J\b\u0010(\u001a\u00020\u0012H\u0002J\b\u0010)\u001a\u00020\u0012H\u0002J\b\u0010*\u001a\u00020\u0012H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lcom/smartroll/StudentActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "Lcom/smartroll/ble/BleManager$DeviceCallback;", "()V", "bleManager", "Lcom/smartroll/ble/BleManager;", "currentUser", "Lcom/smartroll/db/UserEntity;", "detectedDevices", "", "", "repository", "Lcom/smartroll/repository/MainRepository;", "safetyHandler", "Landroid/os/Handler;", "safetyRunnable", "Ljava/lang/Runnable;", "checkPermissions", "", "joinClass", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onDeviceFound", "name", "address", "rssi", "", "id", "onRequestPermissionsResult", "requestCode", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "onScanFailed", "errorCode", "onScanStopped", "stopBleOperations", "syncData", "updateUi", "Companion", "app_debug"})
public final class StudentActivity extends androidx.appcompat.app.AppCompatActivity implements com.smartroll.ble.BleManager.DeviceCallback {
    private com.smartroll.repository.MainRepository repository;
    private com.smartroll.ble.BleManager bleManager;
    @org.jetbrains.annotations.Nullable()
    private com.smartroll.db.UserEntity currentUser;
    @org.jetbrains.annotations.NotNull()
    private java.util.Set<java.lang.String> detectedDevices;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler safetyHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Runnable safetyRunnable = null;
    private static final int PERMISSION_REQUEST_CODE = 102;
    @org.jetbrains.annotations.NotNull()
    public static final com.smartroll.StudentActivity.Companion Companion = null;
    
    public StudentActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void checkPermissions() {
    }
    
    @java.lang.Override()
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull()
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull()
    int[] grantResults) {
    }
    
    private final void updateUi() {
    }
    
    private final void joinClass() {
    }
    
    private final void syncData() {
    }
    
    private final void stopBleOperations() {
    }
    
    @java.lang.Override()
    public void onDeviceFound(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String address, int rssi, @org.jetbrains.annotations.NotNull()
    java.lang.String id) {
    }
    
    @java.lang.Override()
    public void onScanFailed(int errorCode) {
    }
    
    @java.lang.Override()
    public void onScanStopped() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/smartroll/StudentActivity$Companion;", "", "()V", "PERMISSION_REQUEST_CODE", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}