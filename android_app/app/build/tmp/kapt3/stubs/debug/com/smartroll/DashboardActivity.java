package com.smartroll;

/**
 * BlinkERP — Main Dashboard (role-based home)
 *
 * This is the screen the hackathon demo runs on:
 * - TEACHER: "START CLASS" creates the session on the server FIRST (so the
 *   laptop dashboard sees it live), then BLE-advertises as teacher + scans
 *   for student phones.
 * - STUDENT: "JOIN CLASS" BLE-scans for the teacher; when the teacher's
 *   beacon is detected, attendance is auto-marked on the server and shows
 *   up on the laptop dashboard within seconds.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\b\u0018\u00002\u00020\u00012\u00020\u0002B\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u000e\u001a\u00020\u000fH\u0002J\u0012\u0010\u0010\u001a\u00020\u000f2\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0014J\b\u0010\u0013\u001a\u00020\u000fH\u0014J(\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0016H\u0016J-\u0010\u001b\u001a\u00020\u000f2\u0006\u0010\u001c\u001a\u00020\u00192\u000e\u0010\u001d\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00160\u001e2\u0006\u0010\u001f\u001a\u00020 H\u0016\u00a2\u0006\u0002\u0010!J\u0010\u0010\"\u001a\u00020\u000f2\u0006\u0010#\u001a\u00020\u0019H\u0016J\b\u0010$\u001a\u00020\u000fH\u0016J\u0006\u0010%\u001a\u00020\u000fJ\b\u0010&\u001a\u00020\u000fH\u0002J\u0006\u0010\'\u001a\u00020\u000fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lcom/smartroll/DashboardActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "Lcom/smartroll/ble/BleManager$DeviceCallback;", "()V", "bleManager", "Lcom/smartroll/ble/BleManager;", "currentSession", "Lcom/smartroll/db/SessionEntity;", "currentUser", "Lcom/smartroll/db/UserEntity;", "livePollJob", "Lkotlinx/coroutines/Job;", "repository", "Lcom/smartroll/repository/MainRepository;", "checkPermissions", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onDeviceFound", "name", "", "address", "rssi", "", "id", "onRequestPermissionsResult", "requestCode", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "onScanFailed", "errorCode", "onScanStopped", "startBleAction", "startTeacherLivePolling", "stopBleAction", "app_debug"})
public final class DashboardActivity extends androidx.appcompat.app.AppCompatActivity implements com.smartroll.ble.BleManager.DeviceCallback {
    private com.smartroll.ble.BleManager bleManager;
    private com.smartroll.repository.MainRepository repository;
    @org.jetbrains.annotations.Nullable()
    private com.smartroll.db.UserEntity currentUser;
    @org.jetbrains.annotations.Nullable()
    private com.smartroll.db.SessionEntity currentSession;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job livePollJob;
    
    public DashboardActivity() {
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
    
    public final void startBleAction() {
    }
    
    private final void startTeacherLivePolling() {
    }
    
    public final void stopBleAction() {
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
}