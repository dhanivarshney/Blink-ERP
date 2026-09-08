package com.smartroll;

/**
 * SmartRoll — PYQ Papers Activity
 * Shows list of PYQ papers with Google Drive links
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\b\u001a\u00020\t2\b\u0010\n\u001a\u0004\u0018\u00010\tH\u0002J\b\u0010\u000b\u001a\u00020\fH\u0002J\b\u0010\r\u001a\u00020\fH\u0016J\u0012\u0010\u000e\u001a\u00020\f2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0014J\u0010\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\u0005H\u0002R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/smartroll/PyqActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "pyqs", "", "Lcom/smartroll/db/PyqEntity;", "repository", "Lcom/smartroll/repository/MainRepository;", "getPyqIcon", "", "fileName", "loadPyqs", "", "onBackPressed", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "openDriveLink", "pyq", "Companion", "app_debug"})
public final class PyqActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.smartroll.repository.MainRepository repository;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.smartroll.db.PyqEntity> pyqs;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GOOGLE_DRIVE_BASE = "https://drive.google.com/drive/folders/1eTxp4mDjGcuDexieUMYrfqAuKBG3S46n";
    @org.jetbrains.annotations.NotNull()
    public static final com.smartroll.PyqActivity.Companion Companion = null;
    
    public PyqActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void loadPyqs() {
    }
    
    private final void openDriveLink(com.smartroll.db.PyqEntity pyq) {
    }
    
    private final java.lang.String getPyqIcon(java.lang.String fileName) {
        return null;
    }
    
    @java.lang.Override()
    public void onBackPressed() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/smartroll/PyqActivity$Companion;", "", "()V", "GOOGLE_DRIVE_BASE", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}