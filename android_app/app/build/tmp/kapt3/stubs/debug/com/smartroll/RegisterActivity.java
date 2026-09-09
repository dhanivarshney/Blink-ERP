package com.smartroll;

/**
 * BlinkERP — Registration Screen
 * Teacher: name, password, course, branch, section, subject
 * Student: name, password, course, branch, section
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0014R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/smartroll/RegisterActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "btechBranches", "", "", "courses", "otherBranches", "repository", "Lcom/smartroll/repository/MainRepository;", "role", "sections", "subjects2ndYear", "subjectsDefault", "years", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "app_debug"})
public final class RegisterActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.smartroll.repository.MainRepository repository;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String role = "teacher";
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> courses = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> years = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> btechBranches = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> otherBranches = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> sections = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> subjects2ndYear = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> subjectsDefault = null;
    
    public RegisterActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
}