package com.smartroll.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0011\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/smartroll/utils/PermissionManager;", "", "()V", "getRequiredBlePermissions", "", "", "()[Ljava/lang/String;", "hasPermissions", "", "context", "Landroid/content/Context;", "requestPermissions", "", "activity", "Landroid/app/Activity;", "requestCode", "", "app_debug"})
public final class PermissionManager {
    @org.jetbrains.annotations.NotNull()
    public static final com.smartroll.utils.PermissionManager INSTANCE = null;
    
    private PermissionManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String[] getRequiredBlePermissions() {
        return null;
    }
    
    public final boolean hasPermissions(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void requestPermissions(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity, int requestCode) {
    }
}