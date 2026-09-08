package com.smartroll.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0010\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J8\u0010\f\u001a\u0004\u0018\u00010\r2\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u00062\u0006\u0010\u0012\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u0006J\u0010\u0010\u0014\u001a\u0004\u0018\u00010\r2\u0006\u0010\u0015\u001a\u00020\u0016J\u0010\u0010\u0017\u001a\u0004\u0018\u00010\r2\u0006\u0010\u0018\u001a\u00020\u0016J\u0012\u0010\u0019\u001a\u0004\u0018\u00010\r2\u0006\u0010\u001a\u001a\u00020\u0006H\u0002J\u0012\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\u0006\u0010\u001a\u001a\u00020\u0006H\u0002J,\u0010\u001d\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0006J\u0014\u0010\u001f\u001a\u0004\u0018\u00010\u001c2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0006J\b\u0010 \u001a\u0004\u0018\u00010\u001cJ\b\u0010!\u001a\u0004\u0018\u00010\rJ \u0010\"\u001a\u0004\u0018\u00010\u001c2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0006J\b\u0010#\u001a\u0004\u0018\u00010\u001cJ\b\u0010$\u001a\u0004\u0018\u00010\rJ\u000e\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(J \u0010)\u001a\u0004\u0018\u00010\r2\u0006\u0010*\u001a\u00020\u00062\u0006\u0010+\u001a\u00020\u00062\u0006\u0010,\u001a\u00020\u0006J*\u0010-\u001a\u0004\u0018\u00010\r2\u0006\u0010.\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0010\u001a\u00020\u00062\b\b\u0002\u0010/\u001a\u00020\u0006J\u001a\u00100\u001a\u0004\u0018\u00010\r2\u0006\u0010\u001a\u001a\u00020\u00062\u0006\u00101\u001a\u00020\rH\u0002J\\\u00102\u001a\u0004\u0018\u00010\r2\u0006\u0010*\u001a\u00020\u00062\u0006\u0010+\u001a\u00020\u00062\u0006\u0010,\u001a\u00020\u00062\n\b\u0002\u00103\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u00104\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0006J(\u00105\u001a\u0004\u0018\u00010\r2\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0006J\u0016\u00106\u001a\u00020&2\u0006\u0010\'\u001a\u00020(2\u0006\u00107\u001a\u00020\u0006R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\t\u001a\u00020\u00068F\u00a2\u0006\u0006\u001a\u0004\b\n\u0010\u000b\u00a8\u00068"}, d2 = {"Lcom/smartroll/api/ApiService;", "", "()V", "JSON", "Lokhttp3/MediaType;", "_serverUrl", "", "client", "Lokhttp3/OkHttpClient;", "serverUrl", "getServerUrl", "()Ljava/lang/String;", "addNote", "Lorg/json/JSONObject;", "teacherName", "branch", "section", "subject", "title", "content", "deleteNote", "noteId", "", "endSession", "sessionId", "get", "path", "getArray", "Lorg/json/JSONArray;", "getNotes", "teacher", "getPyqs", "getSessions", "getStats", "getStudents", "getUsers", "healthCheck", "init", "", "context", "Landroid/content/Context;", "login", "name", "password", "role", "markAttendance", "studentName", "mode", "post", "body", "register", "course", "year", "startSession", "updateUrl", "newIp", "app_debug"})
public final class ApiService {
    @org.jetbrains.annotations.Nullable()
    private static java.lang.String _serverUrl;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.MediaType JSON = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.smartroll.api.ApiService INSTANCE = null;
    
    private ApiService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getServerUrl() {
        return null;
    }
    
    public final void updateUrl(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String newIp) {
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONObject healthCheck() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONObject register(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String role, @org.jetbrains.annotations.Nullable()
    java.lang.String course, @org.jetbrains.annotations.Nullable()
    java.lang.String year, @org.jetbrains.annotations.Nullable()
    java.lang.String branch, @org.jetbrains.annotations.Nullable()
    java.lang.String section, @org.jetbrains.annotations.Nullable()
    java.lang.String subject) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONObject login(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String role) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONObject startSession(@org.jetbrains.annotations.NotNull()
    java.lang.String teacherName, @org.jetbrains.annotations.NotNull()
    java.lang.String branch, @org.jetbrains.annotations.NotNull()
    java.lang.String section, @org.jetbrains.annotations.NotNull()
    java.lang.String subject) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONObject endSession(int sessionId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONObject markAttendance(@org.jetbrains.annotations.NotNull()
    java.lang.String studentName, @org.jetbrains.annotations.NotNull()
    java.lang.String branch, @org.jetbrains.annotations.NotNull()
    java.lang.String section, @org.jetbrains.annotations.NotNull()
    java.lang.String mode) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONArray getStudents(@org.jetbrains.annotations.Nullable()
    java.lang.String branch, @org.jetbrains.annotations.Nullable()
    java.lang.String section) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONArray getSessions() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONObject getStats() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONArray getUsers() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONObject getNotes(@org.jetbrains.annotations.Nullable()
    java.lang.String teacher, @org.jetbrains.annotations.Nullable()
    java.lang.String branch, @org.jetbrains.annotations.Nullable()
    java.lang.String section) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONObject addNote(@org.jetbrains.annotations.NotNull()
    java.lang.String teacherName, @org.jetbrains.annotations.NotNull()
    java.lang.String branch, @org.jetbrains.annotations.NotNull()
    java.lang.String section, @org.jetbrains.annotations.NotNull()
    java.lang.String subject, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String content) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONObject deleteNote(int noteId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final org.json.JSONArray getPyqs(@org.jetbrains.annotations.Nullable()
    java.lang.String teacherName) {
        return null;
    }
    
    private final org.json.JSONObject post(java.lang.String path, org.json.JSONObject body) {
        return null;
    }
    
    private final org.json.JSONObject get(java.lang.String path) {
        return null;
    }
    
    private final org.json.JSONArray getArray(java.lang.String path) {
        return null;
    }
}