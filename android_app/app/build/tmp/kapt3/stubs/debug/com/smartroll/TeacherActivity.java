package com.smartroll;

/**
 * BlinkERP — Teacher Dashboard
 * Start Class → BLE advertise → students detected → End Class
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010#\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\b\u0018\u0000 32\u00020\u00012\u00020\u0002:\u00013B\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u0019\u001a\u00020\u001aH\u0002J\b\u0010\u001b\u001a\u00020\u001aH\u0002J\b\u0010\u001c\u001a\u00020\u001aH\u0002J\u0012\u0010\u001d\u001a\u00020\u001a2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0014J\b\u0010 \u001a\u00020\u001aH\u0014J(\u0010!\u001a\u00020\u001a2\u0006\u0010\"\u001a\u00020\f2\u0006\u0010#\u001a\u00020\f2\u0006\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\fH\u0016J-\u0010\'\u001a\u00020\u001a2\u0006\u0010(\u001a\u00020%2\u000e\u0010)\u001a\n\u0012\u0006\b\u0001\u0012\u00020\f0*2\u0006\u0010+\u001a\u00020,H\u0016\u00a2\u0006\u0002\u0010-J\b\u0010.\u001a\u00020\u001aH\u0016J\b\u0010/\u001a\u00020\u001aH\u0002J\b\u00100\u001a\u00020\u001aH\u0002J\b\u00101\u001a\u00020\u001aH\u0002J\b\u00102\u001a\u00020\u001aH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00064"}, d2 = {"Lcom/smartroll/TeacherActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "Lcom/smartroll/ble/BleManager$DeviceCallback;", "()V", "bleManager", "Lcom/smartroll/ble/BleManager;", "currentSession", "Lcom/smartroll/db/SessionEntity;", "currentUser", "Lcom/smartroll/db/UserEntity;", "detectedStudentNames", "", "", "detectedStudents", "", "isScanningActive", "", "livePollJob", "Lkotlinx/coroutines/Job;", "repository", "Lcom/smartroll/repository/MainRepository;", "scanHandler", "Landroid/os/Handler;", "scanRunnable", "Ljava/lang/Runnable;", "checkPermissions", "", "cleanupBleSystem", "endClass", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onDeviceFound", "name", "address", "rssi", "", "id", "onRequestPermissionsResult", "requestCode", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "onScanStopped", "startClass", "startTeacherLivePolling", "syncData", "updateUi", "Companion", "app_debug"})
public final class TeacherActivity extends androidx.appcompat.app.AppCompatActivity implements com.smartroll.ble.BleManager.DeviceCallback {
    private com.smartroll.repository.MainRepository repository;
    private com.smartroll.ble.BleManager bleManager;
    @org.jetbrains.annotations.Nullable()
    private com.smartroll.db.UserEntity currentUser;
    @org.jetbrains.annotations.Nullable()
    private com.smartroll.db.SessionEntity currentSession;
    @org.jetbrains.annotations.NotNull()
    private java.util.Set<java.lang.String> detectedStudents;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<java.lang.String> detectedStudentNames;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler scanHandler = null;
    private boolean isScanningActive = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Runnable scanRunnable = null;
    private static final int PERMISSION_REQUEST_CODE = 101;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job livePollJob;
    @org.jetbrains.annotations.NotNull()
    public static final com.smartroll.TeacherActivity.Companion Companion = null;
    
    public TeacherActivity() {
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
    
    private final void startClass() {
    }
    
    private final void startTeacherLivePolling() {
    }
    
    private final void endClass() {
    }
    
    private final void cleanupBleSystem() {
    }
    
    private final void syncData() {
    }
    
    @java.lang.Override()
    public void onDeviceFound(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String address, int rssi, @org.jetbrains.annotations.NotNull()
    java.lang.String id) {
    }
    
    @java.lang.Override()
    public void onScanStopped() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @java.lang.Override()
    public void onScanFailed(int errorCode) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/smartroll/TeacherActivity$Companion;", "", "()V", "PERMISSION_REQUEST_CODE", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}