package com.smartroll.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u001d\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010\u000bJ\u001a\u0010\f\u001a\u00020\u00042\u0006\u0010\r\u001a\u00020\u000e2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\tJ\u000e\u0010\u000f\u001a\u00020\u00042\u0006\u0010\r\u001a\u00020\u000eJ\"\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0012\u001a\u00020\u00132\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\tJ\u000e\u0010\u0014\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0015"}, d2 = {"Lcom/smartroll/utils/PermissionManager;", "", "()V", "checkAndPromptLocation", "", "activity", "Landroid/app/Activity;", "getRequiredBlePermissions", "", "", "role", "(Ljava/lang/String;)[Ljava/lang/String;", "hasPermissions", "context", "Landroid/content/Context;", "isLocationEnabled", "requestPermissions", "", "requestCode", "", "showOemScanPermissionMessage", "app_debug"})
public final class PermissionManager {
    @org.jetbrains.annotations.NotNull()
    public static final com.smartroll.utils.PermissionManager INSTANCE = null;
    
    private PermissionManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String[] getRequiredBlePermissions(@org.jetbrains.annotations.Nullable()
    java.lang.String role) {
        return null;
    }
    
    public final boolean hasPermissions(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    java.lang.String role) {
        return false;
    }
    
    public final void requestPermissions(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity, int requestCode, @org.jetbrains.annotations.Nullable()
    java.lang.String role) {
    }
    
    /**
     * Checks if system-level Location Services (GPS/Network) are enabled.
     * Crucial for Android 11 and below (API <= 30), where BLE scanning silently returns nothing if OFF.
     */
    public final boolean isLocationEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Verifies system location services on Android 11 and below.
     * Prompts the user to turn on Location if it's disabled.
     * Returns true if location is enabled (or not required on this Android version).
     */
    public final boolean checkAndPromptLocation(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity) {
        return false;
    }
    
    /**
     * Helpful dialog for OEM Android skins (Xiaomi, Vivo, Oppo) that may have broken BLUETOOTH_SCAN dialogs.
     */
    public final void showOemScanPermissionMessage(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity) {
    }
}