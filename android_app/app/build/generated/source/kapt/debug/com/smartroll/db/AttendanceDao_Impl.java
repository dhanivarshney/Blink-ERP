package com.smartroll.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AttendanceDao_Impl implements AttendanceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AttendanceRecordEntity> __insertionAdapterOfAttendanceRecordEntity;

  private final EntityDeletionOrUpdateAdapter<AttendanceRecordEntity> __updateAdapterOfAttendanceRecordEntity;

  public AttendanceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAttendanceRecordEntity = new EntityInsertionAdapter<AttendanceRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `attendance_records` (`id`,`remoteId`,`sessionId`,`studentName`,`branch`,`section`,`mode`,`timestamp`,`syncStatus`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AttendanceRecordEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getRemoteId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getRemoteId());
        }
        if (entity.getSessionId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSessionId());
        }
        if (entity.getStudentName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getStudentName());
        }
        if (entity.getBranch() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBranch());
        }
        if (entity.getSection() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getSection());
        }
        if (entity.getMode() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getMode());
        }
        statement.bindLong(8, entity.getTimestamp());
        if (entity.getSyncStatus() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getSyncStatus());
        }
      }
    };
    this.__updateAdapterOfAttendanceRecordEntity = new EntityDeletionOrUpdateAdapter<AttendanceRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `attendance_records` SET `id` = ?,`remoteId` = ?,`sessionId` = ?,`studentName` = ?,`branch` = ?,`section` = ?,`mode` = ?,`timestamp` = ?,`syncStatus` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AttendanceRecordEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getRemoteId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getRemoteId());
        }
        if (entity.getSessionId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSessionId());
        }
        if (entity.getStudentName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getStudentName());
        }
        if (entity.getBranch() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBranch());
        }
        if (entity.getSection() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getSection());
        }
        if (entity.getMode() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getMode());
        }
        statement.bindLong(8, entity.getTimestamp());
        if (entity.getSyncStatus() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getSyncStatus());
        }
        if (entity.getId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getId());
        }
      }
    };
  }

  @Override
  public Object insertRecord(final AttendanceRecordEntity record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAttendanceRecordEntity.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRecord(final AttendanceRecordEntity record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAttendanceRecordEntity.handle(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecord(final String studentName, final String sessionId,
      final Continuation<? super AttendanceRecordEntity> $completion) {
    final String _sql = "SELECT * FROM attendance_records WHERE studentName = ? AND sessionId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (studentName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, studentName);
    }
    _argIndex = 2;
    if (sessionId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, sessionId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AttendanceRecordEntity>() {
      @Override
      @Nullable
      public AttendanceRecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRemoteId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteId");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfStudentName = CursorUtil.getColumnIndexOrThrow(_cursor, "studentName");
          final int _cursorIndexOfBranch = CursorUtil.getColumnIndexOrThrow(_cursor, "branch");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final AttendanceRecordEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final Integer _tmpRemoteId;
            if (_cursor.isNull(_cursorIndexOfRemoteId)) {
              _tmpRemoteId = null;
            } else {
              _tmpRemoteId = _cursor.getInt(_cursorIndexOfRemoteId);
            }
            final String _tmpSessionId;
            if (_cursor.isNull(_cursorIndexOfSessionId)) {
              _tmpSessionId = null;
            } else {
              _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            }
            final String _tmpStudentName;
            if (_cursor.isNull(_cursorIndexOfStudentName)) {
              _tmpStudentName = null;
            } else {
              _tmpStudentName = _cursor.getString(_cursorIndexOfStudentName);
            }
            final String _tmpBranch;
            if (_cursor.isNull(_cursorIndexOfBranch)) {
              _tmpBranch = null;
            } else {
              _tmpBranch = _cursor.getString(_cursorIndexOfBranch);
            }
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpMode;
            if (_cursor.isNull(_cursorIndexOfMode)) {
              _tmpMode = null;
            } else {
              _tmpMode = _cursor.getString(_cursorIndexOfMode);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpSyncStatus;
            if (_cursor.isNull(_cursorIndexOfSyncStatus)) {
              _tmpSyncStatus = null;
            } else {
              _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            }
            _result = new AttendanceRecordEntity(_tmpId,_tmpRemoteId,_tmpSessionId,_tmpStudentName,_tmpBranch,_tmpSection,_tmpMode,_tmpTimestamp,_tmpSyncStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getUnsyncedRecords(
      final Continuation<? super List<AttendanceRecordEntity>> $completion) {
    final String _sql = "SELECT * FROM attendance_records WHERE syncStatus = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AttendanceRecordEntity>>() {
      @Override
      @NonNull
      public List<AttendanceRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRemoteId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteId");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfStudentName = CursorUtil.getColumnIndexOrThrow(_cursor, "studentName");
          final int _cursorIndexOfBranch = CursorUtil.getColumnIndexOrThrow(_cursor, "branch");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final List<AttendanceRecordEntity> _result = new ArrayList<AttendanceRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttendanceRecordEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final Integer _tmpRemoteId;
            if (_cursor.isNull(_cursorIndexOfRemoteId)) {
              _tmpRemoteId = null;
            } else {
              _tmpRemoteId = _cursor.getInt(_cursorIndexOfRemoteId);
            }
            final String _tmpSessionId;
            if (_cursor.isNull(_cursorIndexOfSessionId)) {
              _tmpSessionId = null;
            } else {
              _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            }
            final String _tmpStudentName;
            if (_cursor.isNull(_cursorIndexOfStudentName)) {
              _tmpStudentName = null;
            } else {
              _tmpStudentName = _cursor.getString(_cursorIndexOfStudentName);
            }
            final String _tmpBranch;
            if (_cursor.isNull(_cursorIndexOfBranch)) {
              _tmpBranch = null;
            } else {
              _tmpBranch = _cursor.getString(_cursorIndexOfBranch);
            }
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpMode;
            if (_cursor.isNull(_cursorIndexOfMode)) {
              _tmpMode = null;
            } else {
              _tmpMode = _cursor.getString(_cursorIndexOfMode);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpSyncStatus;
            if (_cursor.isNull(_cursorIndexOfSyncStatus)) {
              _tmpSyncStatus = null;
            } else {
              _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            }
            _item = new AttendanceRecordEntity(_tmpId,_tmpRemoteId,_tmpSessionId,_tmpStudentName,_tmpBranch,_tmpSection,_tmpMode,_tmpTimestamp,_tmpSyncStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllRecords(
      final Continuation<? super List<AttendanceRecordEntity>> $completion) {
    final String _sql = "SELECT * FROM attendance_records ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AttendanceRecordEntity>>() {
      @Override
      @NonNull
      public List<AttendanceRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRemoteId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteId");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfStudentName = CursorUtil.getColumnIndexOrThrow(_cursor, "studentName");
          final int _cursorIndexOfBranch = CursorUtil.getColumnIndexOrThrow(_cursor, "branch");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final List<AttendanceRecordEntity> _result = new ArrayList<AttendanceRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttendanceRecordEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final Integer _tmpRemoteId;
            if (_cursor.isNull(_cursorIndexOfRemoteId)) {
              _tmpRemoteId = null;
            } else {
              _tmpRemoteId = _cursor.getInt(_cursorIndexOfRemoteId);
            }
            final String _tmpSessionId;
            if (_cursor.isNull(_cursorIndexOfSessionId)) {
              _tmpSessionId = null;
            } else {
              _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            }
            final String _tmpStudentName;
            if (_cursor.isNull(_cursorIndexOfStudentName)) {
              _tmpStudentName = null;
            } else {
              _tmpStudentName = _cursor.getString(_cursorIndexOfStudentName);
            }
            final String _tmpBranch;
            if (_cursor.isNull(_cursorIndexOfBranch)) {
              _tmpBranch = null;
            } else {
              _tmpBranch = _cursor.getString(_cursorIndexOfBranch);
            }
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            final String _tmpMode;
            if (_cursor.isNull(_cursorIndexOfMode)) {
              _tmpMode = null;
            } else {
              _tmpMode = _cursor.getString(_cursorIndexOfMode);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpSyncStatus;
            if (_cursor.isNull(_cursorIndexOfSyncStatus)) {
              _tmpSyncStatus = null;
            } else {
              _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            }
            _item = new AttendanceRecordEntity(_tmpId,_tmpRemoteId,_tmpSessionId,_tmpStudentName,_tmpBranch,_tmpSection,_tmpMode,_tmpTimestamp,_tmpSyncStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
