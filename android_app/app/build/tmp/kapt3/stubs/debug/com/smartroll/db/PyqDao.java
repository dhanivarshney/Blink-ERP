package com.smartroll.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\bH\'J\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\tH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0018\u0010\f\u001a\u0004\u0018\u00010\u00052\u0006\u0010\r\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u001c\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\b2\u0006\u0010\u0011\u001a\u00020\u0012H\'J\u0016\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0014\u001a\u00020\u00032\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00050\tH\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u0016\u0010\u0017\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0018"}, d2 = {"Lcom/smartroll/db/PyqDao;", "", "deletePyq", "", "pyq", "Lcom/smartroll/db/PyqEntity;", "(Lcom/smartroll/db/PyqEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllPyqs", "Lkotlinx/coroutines/flow/Flow;", "", "getAllPyqsList", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPyqById", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPyqsByTeacher", "teacherName", "", "insertPyq", "insertPyqs", "pyqs", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updatePyq", "app_debug"})
@androidx.room.Dao()
public abstract interface PyqDao {
    
    @androidx.room.Query(value = "SELECT * FROM pyqs ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.smartroll.db.PyqEntity>> getAllPyqs();
    
    @androidx.room.Query(value = "SELECT * FROM pyqs ORDER BY createdAt DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllPyqsList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.smartroll.db.PyqEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM pyqs WHERE teacherName = :teacherName ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.smartroll.db.PyqEntity>> getPyqsByTeacher(@org.jetbrains.annotations.NotNull()
    java.lang.String teacherName);
    
    @androidx.room.Query(value = "SELECT * FROM pyqs WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getPyqById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smartroll.db.PyqEntity> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertPyq(@org.jetbrains.annotations.NotNull()
    com.smartroll.db.PyqEntity pyq, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertPyqs(@org.jetbrains.annotations.NotNull()
    java.util.List<com.smartroll.db.PyqEntity> pyqs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updatePyq(@org.jetbrains.annotations.NotNull()
    com.smartroll.db.PyqEntity pyq, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deletePyq(@org.jetbrains.annotations.NotNull()
    com.smartroll.db.PyqEntity pyq, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}