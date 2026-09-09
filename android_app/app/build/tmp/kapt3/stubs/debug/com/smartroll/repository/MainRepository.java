package com.smartroll.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0094\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0019\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 P2\u00020\u0001:\u0001PB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017H\u0086@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\u00152\u0006\u0010\u001a\u001a\u00020\u001bH\u0086@\u00a2\u0006\u0002\u0010\u001cJ\u0016\u0010\u001d\u001a\u00020\u00152\u0006\u0010\u001e\u001a\u00020\u001fH\u0086@\u00a2\u0006\u0002\u0010 J \u0010!\u001a\u0004\u0018\u00010\u001f2\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020#H\u0086@\u00a2\u0006\u0002\u0010%J\u0014\u0010&\u001a\b\u0012\u0004\u0012\u00020\u001b0\'H\u0086@\u00a2\u0006\u0002\u0010(J\u0014\u0010)\u001a\b\u0012\u0004\u0012\u00020*0\'H\u0086@\u00a2\u0006\u0002\u0010(J\u0010\u0010+\u001a\u0004\u0018\u00010\u001bH\u0086@\u00a2\u0006\u0002\u0010(J8\u0010,\u001a\b\u0012\u0004\u0012\u00020-0\'2\n\b\u0002\u0010.\u001a\u0004\u0018\u00010#2\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010#2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010#H\u0086@\u00a2\u0006\u0002\u0010/J8\u00100\u001a\b\u0012\u0004\u0012\u0002010\'2\n\b\u0002\u0010.\u001a\u0004\u0018\u00010#2\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010#2\n\b\u0002\u00102\u001a\u0004\u0018\u00010#H\u0086@\u00a2\u0006\u0002\u0010/J,\u00103\u001a\b\u0012\u0004\u0012\u00020\u001b0\'2\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010#2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010#H\u0086@\u00a2\u0006\u0002\u0010%J4\u00104\u001a\b\u0012\u0004\u0012\u00020\u001b052\u0006\u00106\u001a\u00020#2\u0006\u00107\u001a\u00020#2\u0006\u00108\u001a\u00020#H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b9\u0010/J\u0006\u0010:\u001a\u00020\u0015JJ\u0010;\u001a\b\u0012\u0004\u0012\u00020*052\u0006\u0010<\u001a\u00020#2\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020#2\b\b\u0002\u0010=\u001a\u00020#2\n\b\u0002\u0010>\u001a\u0004\u0018\u00010#H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b?\u0010@J^\u0010A\u001a\b\u0012\u0004\u0012\u00020\u001b052\u0006\u00106\u001a\u00020#2\u0006\u00107\u001a\u00020#2\u0006\u00108\u001a\u00020#2\u0006\u0010B\u001a\u00020#2\u0006\u0010C\u001a\u00020#2\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020#2\b\u00102\u001a\u0004\u0018\u00010#H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\bD\u0010EJ\u0010\u0010F\u001a\u00020\u00152\u0006\u0010G\u001a\u00020#H\u0002J\u0016\u0010H\u001a\u00020\u00152\u0006\u0010I\u001a\u00020-H\u0086@\u00a2\u0006\u0002\u0010JJ\u000e\u0010K\u001a\u00020\u00152\u0006\u0010\u001a\u001a\u00020\u001bJ.\u0010L\u001a\u00020\u001f2\u0006\u0010.\u001a\u00020#2\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020#2\u0006\u00102\u001a\u00020#H\u0086@\u00a2\u0006\u0002\u0010MJ\u000e\u0010N\u001a\u00020OH\u0086@\u00a2\u0006\u0002\u0010(R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n \r*\u0004\u0018\u00010\f0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006Q"}, d2 = {"Lcom/smartroll/repository/MainRepository;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attendanceDao", "Lcom/smartroll/db/AttendanceDao;", "db", "Lcom/smartroll/db/AppDatabase;", "noteDao", "Lcom/smartroll/db/NoteDao;", "prefs", "Landroid/content/SharedPreferences;", "kotlin.jvm.PlatformType", "pyqDao", "Lcom/smartroll/db/PyqDao;", "sessionDao", "Lcom/smartroll/db/SessionDao;", "userDao", "Lcom/smartroll/db/UserDao;", "deleteNote", "", "noteId", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteUser", "user", "Lcom/smartroll/db/UserEntity;", "(Lcom/smartroll/db/UserEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "endSession", "session", "Lcom/smartroll/db/SessionEntity;", "(Lcom/smartroll/db/SessionEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getActiveSession", "branch", "", "section", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllLocalUsers", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAttendanceHistory", "Lcom/smartroll/db/AttendanceRecordEntity;", "getCurrentUser", "getNotes", "Lcom/smartroll/db/NoteEntity;", "teacherName", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPyqs", "Lcom/smartroll/db/PyqEntity;", "subject", "getRegisteredUsers", "login", "Lkotlin/Result;", "name", "password", "role", "login-BWLJW6A", "logout", "markAttendance", "studentName", "mode", "sessionId", "markAttendance-hUnOzRk", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "register", "course", "year", "register-tZkwj4A", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveCurrentUserId", "id", "saveNote", "note", "(Lcom/smartroll/db/NoteEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveUserSession", "startSession", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncPendingData", "Lcom/smartroll/repository/SyncResult;", "Companion", "app_debug"})
public final class MainRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.smartroll.db.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final com.smartroll.db.UserDao userDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.smartroll.db.SessionDao sessionDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.smartroll.db.AttendanceDao attendanceDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.smartroll.db.PyqDao pyqDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.smartroll.db.NoteDao noteDao = null;
    @org.jetbrains.annotations.Nullable()
    private static com.smartroll.db.SessionEntity currentSession;
    private static boolean isScanningOrAdvertising = false;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> detectedDevices = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> attendanceStatus = null;
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.smartroll.repository.MainRepository.Companion Companion = null;
    
    public MainRepository(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final void saveUserSession(@org.jetbrains.annotations.NotNull()
    com.smartroll.db.UserEntity user) {
    }
    
    private final void saveCurrentUserId(java.lang.String id) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCurrentUser(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smartroll.db.UserEntity> $completion) {
        return null;
    }
    
    public final void logout() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getPyqs(@org.jetbrains.annotations.Nullable()
    java.lang.String teacherName, @org.jetbrains.annotations.Nullable()
    java.lang.String branch, @org.jetbrains.annotations.Nullable()
    java.lang.String subject, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.smartroll.db.PyqEntity>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getNotes(@org.jetbrains.annotations.Nullable()
    java.lang.String teacherName, @org.jetbrains.annotations.Nullable()
    java.lang.String branch, @org.jetbrains.annotations.Nullable()
    java.lang.String section, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.smartroll.db.NoteEntity>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveNote(@org.jetbrains.annotations.NotNull()
    com.smartroll.db.NoteEntity note, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteNote(int noteId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getRegisteredUsers(@org.jetbrains.annotations.Nullable()
    java.lang.String branch, @org.jetbrains.annotations.Nullable()
    java.lang.String section, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.smartroll.db.UserEntity>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAllLocalUsers(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.smartroll.db.UserEntity>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteUser(@org.jetbrains.annotations.NotNull()
    com.smartroll.db.UserEntity user, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAttendanceHistory(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.smartroll.db.AttendanceRecordEntity>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getActiveSession(@org.jetbrains.annotations.NotNull()
    java.lang.String branch, @org.jetbrains.annotations.NotNull()
    java.lang.String section, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smartroll.db.SessionEntity> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object startSession(@org.jetbrains.annotations.NotNull()
    java.lang.String teacherName, @org.jetbrains.annotations.NotNull()
    java.lang.String branch, @org.jetbrains.annotations.NotNull()
    java.lang.String section, @org.jetbrains.annotations.NotNull()
    java.lang.String subject, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smartroll.db.SessionEntity> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object endSession(@org.jetbrains.annotations.NotNull()
    com.smartroll.db.SessionEntity session, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncPendingData(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smartroll.repository.SyncResult> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010#\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0019\u0010\u0003\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u001c\u0010\b\u001a\u0004\u0018\u00010\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00050\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u001a\u0010\u0012\u001a\u00020\u0013X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0014\"\u0004\b\u0015\u0010\u0016\u00a8\u0006\u0017"}, d2 = {"Lcom/smartroll/repository/MainRepository$Companion;", "", "()V", "attendanceStatus", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "getAttendanceStatus", "()Lkotlinx/coroutines/flow/MutableStateFlow;", "currentSession", "Lcom/smartroll/db/SessionEntity;", "getCurrentSession", "()Lcom/smartroll/db/SessionEntity;", "setCurrentSession", "(Lcom/smartroll/db/SessionEntity;)V", "detectedDevices", "", "getDetectedDevices", "()Ljava/util/Set;", "isScanningOrAdvertising", "", "()Z", "setScanningOrAdvertising", "(Z)V", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.smartroll.db.SessionEntity getCurrentSession() {
            return null;
        }
        
        public final void setCurrentSession(@org.jetbrains.annotations.Nullable()
        com.smartroll.db.SessionEntity p0) {
        }
        
        public final boolean isScanningOrAdvertising() {
            return false;
        }
        
        public final void setScanningOrAdvertising(boolean p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Set<java.lang.String> getDetectedDevices() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> getAttendanceStatus() {
            return null;
        }
    }
}