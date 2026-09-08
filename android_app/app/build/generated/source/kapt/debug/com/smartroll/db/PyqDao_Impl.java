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
import java.lang.Long;
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
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class PyqDao_Impl implements PyqDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PyqEntity> __insertionAdapterOfPyqEntity;

  private final EntityDeletionOrUpdateAdapter<PyqEntity> __deletionAdapterOfPyqEntity;

  private final EntityDeletionOrUpdateAdapter<PyqEntity> __updateAdapterOfPyqEntity;

  public PyqDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPyqEntity = new EntityInsertionAdapter<PyqEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `pyqs` (`id`,`teacherName`,`branch`,`subject`,`title`,`semester`,`year`,`examType`,`content`,`filePath`,`fileName`,`driveLink`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PyqEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTeacherName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTeacherName());
        }
        if (entity.getBranch() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBranch());
        }
        if (entity.getSubject() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSubject());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getTitle());
        }
        if (entity.getSemester() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getSemester());
        }
        if (entity.getYear() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getYear());
        }
        if (entity.getExamType() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getExamType());
        }
        if (entity.getContent() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getContent());
        }
        if (entity.getFilePath() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getFilePath());
        }
        if (entity.getFileName() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getFileName());
        }
        if (entity.getDriveLink() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getDriveLink());
        }
        if (entity.getCreatedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getCreatedAt());
        }
      }
    };
    this.__deletionAdapterOfPyqEntity = new EntityDeletionOrUpdateAdapter<PyqEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `pyqs` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PyqEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPyqEntity = new EntityDeletionOrUpdateAdapter<PyqEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `pyqs` SET `id` = ?,`teacherName` = ?,`branch` = ?,`subject` = ?,`title` = ?,`semester` = ?,`year` = ?,`examType` = ?,`content` = ?,`filePath` = ?,`fileName` = ?,`driveLink` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PyqEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTeacherName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTeacherName());
        }
        if (entity.getBranch() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBranch());
        }
        if (entity.getSubject() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSubject());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getTitle());
        }
        if (entity.getSemester() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getSemester());
        }
        if (entity.getYear() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getYear());
        }
        if (entity.getExamType() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getExamType());
        }
        if (entity.getContent() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getContent());
        }
        if (entity.getFilePath() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getFilePath());
        }
        if (entity.getFileName() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getFileName());
        }
        if (entity.getDriveLink() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getDriveLink());
        }
        if (entity.getCreatedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getCreatedAt());
        }
        statement.bindLong(14, entity.getId());
      }
    };
  }

  @Override
  public Object insertPyq(final PyqEntity pyq, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPyqEntity.insertAndReturnId(pyq);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPyqs(final List<PyqEntity> pyqs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPyqEntity.insert(pyqs);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePyq(final PyqEntity pyq, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPyqEntity.handle(pyq);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePyq(final PyqEntity pyq, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPyqEntity.handle(pyq);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PyqEntity>> getAllPyqs() {
    final String _sql = "SELECT * FROM pyqs ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pyqs"}, new Callable<List<PyqEntity>>() {
      @Override
      @NonNull
      public List<PyqEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTeacherName = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherName");
          final int _cursorIndexOfBranch = CursorUtil.getColumnIndexOrThrow(_cursor, "branch");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfExamType = CursorUtil.getColumnIndexOrThrow(_cursor, "examType");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfDriveLink = CursorUtil.getColumnIndexOrThrow(_cursor, "driveLink");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<PyqEntity> _result = new ArrayList<PyqEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PyqEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTeacherName;
            if (_cursor.isNull(_cursorIndexOfTeacherName)) {
              _tmpTeacherName = null;
            } else {
              _tmpTeacherName = _cursor.getString(_cursorIndexOfTeacherName);
            }
            final String _tmpBranch;
            if (_cursor.isNull(_cursorIndexOfBranch)) {
              _tmpBranch = null;
            } else {
              _tmpBranch = _cursor.getString(_cursorIndexOfBranch);
            }
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpSemester;
            if (_cursor.isNull(_cursorIndexOfSemester)) {
              _tmpSemester = null;
            } else {
              _tmpSemester = _cursor.getString(_cursorIndexOfSemester);
            }
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpExamType;
            if (_cursor.isNull(_cursorIndexOfExamType)) {
              _tmpExamType = null;
            } else {
              _tmpExamType = _cursor.getString(_cursorIndexOfExamType);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final String _tmpFilePath;
            if (_cursor.isNull(_cursorIndexOfFilePath)) {
              _tmpFilePath = null;
            } else {
              _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            }
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final String _tmpDriveLink;
            if (_cursor.isNull(_cursorIndexOfDriveLink)) {
              _tmpDriveLink = null;
            } else {
              _tmpDriveLink = _cursor.getString(_cursorIndexOfDriveLink);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new PyqEntity(_tmpId,_tmpTeacherName,_tmpBranch,_tmpSubject,_tmpTitle,_tmpSemester,_tmpYear,_tmpExamType,_tmpContent,_tmpFilePath,_tmpFileName,_tmpDriveLink,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllPyqsList(final Continuation<? super List<PyqEntity>> $completion) {
    final String _sql = "SELECT * FROM pyqs ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PyqEntity>>() {
      @Override
      @NonNull
      public List<PyqEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTeacherName = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherName");
          final int _cursorIndexOfBranch = CursorUtil.getColumnIndexOrThrow(_cursor, "branch");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfExamType = CursorUtil.getColumnIndexOrThrow(_cursor, "examType");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfDriveLink = CursorUtil.getColumnIndexOrThrow(_cursor, "driveLink");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<PyqEntity> _result = new ArrayList<PyqEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PyqEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTeacherName;
            if (_cursor.isNull(_cursorIndexOfTeacherName)) {
              _tmpTeacherName = null;
            } else {
              _tmpTeacherName = _cursor.getString(_cursorIndexOfTeacherName);
            }
            final String _tmpBranch;
            if (_cursor.isNull(_cursorIndexOfBranch)) {
              _tmpBranch = null;
            } else {
              _tmpBranch = _cursor.getString(_cursorIndexOfBranch);
            }
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpSemester;
            if (_cursor.isNull(_cursorIndexOfSemester)) {
              _tmpSemester = null;
            } else {
              _tmpSemester = _cursor.getString(_cursorIndexOfSemester);
            }
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpExamType;
            if (_cursor.isNull(_cursorIndexOfExamType)) {
              _tmpExamType = null;
            } else {
              _tmpExamType = _cursor.getString(_cursorIndexOfExamType);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final String _tmpFilePath;
            if (_cursor.isNull(_cursorIndexOfFilePath)) {
              _tmpFilePath = null;
            } else {
              _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            }
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final String _tmpDriveLink;
            if (_cursor.isNull(_cursorIndexOfDriveLink)) {
              _tmpDriveLink = null;
            } else {
              _tmpDriveLink = _cursor.getString(_cursorIndexOfDriveLink);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new PyqEntity(_tmpId,_tmpTeacherName,_tmpBranch,_tmpSubject,_tmpTitle,_tmpSemester,_tmpYear,_tmpExamType,_tmpContent,_tmpFilePath,_tmpFileName,_tmpDriveLink,_tmpCreatedAt);
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
  public Flow<List<PyqEntity>> getPyqsByTeacher(final String teacherName) {
    final String _sql = "SELECT * FROM pyqs WHERE teacherName = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (teacherName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, teacherName);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pyqs"}, new Callable<List<PyqEntity>>() {
      @Override
      @NonNull
      public List<PyqEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTeacherName = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherName");
          final int _cursorIndexOfBranch = CursorUtil.getColumnIndexOrThrow(_cursor, "branch");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfExamType = CursorUtil.getColumnIndexOrThrow(_cursor, "examType");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfDriveLink = CursorUtil.getColumnIndexOrThrow(_cursor, "driveLink");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<PyqEntity> _result = new ArrayList<PyqEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PyqEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTeacherName;
            if (_cursor.isNull(_cursorIndexOfTeacherName)) {
              _tmpTeacherName = null;
            } else {
              _tmpTeacherName = _cursor.getString(_cursorIndexOfTeacherName);
            }
            final String _tmpBranch;
            if (_cursor.isNull(_cursorIndexOfBranch)) {
              _tmpBranch = null;
            } else {
              _tmpBranch = _cursor.getString(_cursorIndexOfBranch);
            }
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpSemester;
            if (_cursor.isNull(_cursorIndexOfSemester)) {
              _tmpSemester = null;
            } else {
              _tmpSemester = _cursor.getString(_cursorIndexOfSemester);
            }
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpExamType;
            if (_cursor.isNull(_cursorIndexOfExamType)) {
              _tmpExamType = null;
            } else {
              _tmpExamType = _cursor.getString(_cursorIndexOfExamType);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final String _tmpFilePath;
            if (_cursor.isNull(_cursorIndexOfFilePath)) {
              _tmpFilePath = null;
            } else {
              _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            }
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final String _tmpDriveLink;
            if (_cursor.isNull(_cursorIndexOfDriveLink)) {
              _tmpDriveLink = null;
            } else {
              _tmpDriveLink = _cursor.getString(_cursorIndexOfDriveLink);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new PyqEntity(_tmpId,_tmpTeacherName,_tmpBranch,_tmpSubject,_tmpTitle,_tmpSemester,_tmpYear,_tmpExamType,_tmpContent,_tmpFilePath,_tmpFileName,_tmpDriveLink,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPyqById(final long id, final Continuation<? super PyqEntity> $completion) {
    final String _sql = "SELECT * FROM pyqs WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PyqEntity>() {
      @Override
      @Nullable
      public PyqEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTeacherName = CursorUtil.getColumnIndexOrThrow(_cursor, "teacherName");
          final int _cursorIndexOfBranch = CursorUtil.getColumnIndexOrThrow(_cursor, "branch");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSemester = CursorUtil.getColumnIndexOrThrow(_cursor, "semester");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfExamType = CursorUtil.getColumnIndexOrThrow(_cursor, "examType");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfDriveLink = CursorUtil.getColumnIndexOrThrow(_cursor, "driveLink");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final PyqEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTeacherName;
            if (_cursor.isNull(_cursorIndexOfTeacherName)) {
              _tmpTeacherName = null;
            } else {
              _tmpTeacherName = _cursor.getString(_cursorIndexOfTeacherName);
            }
            final String _tmpBranch;
            if (_cursor.isNull(_cursorIndexOfBranch)) {
              _tmpBranch = null;
            } else {
              _tmpBranch = _cursor.getString(_cursorIndexOfBranch);
            }
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpSemester;
            if (_cursor.isNull(_cursorIndexOfSemester)) {
              _tmpSemester = null;
            } else {
              _tmpSemester = _cursor.getString(_cursorIndexOfSemester);
            }
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpExamType;
            if (_cursor.isNull(_cursorIndexOfExamType)) {
              _tmpExamType = null;
            } else {
              _tmpExamType = _cursor.getString(_cursorIndexOfExamType);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final String _tmpFilePath;
            if (_cursor.isNull(_cursorIndexOfFilePath)) {
              _tmpFilePath = null;
            } else {
              _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            }
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final String _tmpDriveLink;
            if (_cursor.isNull(_cursorIndexOfDriveLink)) {
              _tmpDriveLink = null;
            } else {
              _tmpDriveLink = _cursor.getString(_cursorIndexOfDriveLink);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _result = new PyqEntity(_tmpId,_tmpTeacherName,_tmpBranch,_tmpSubject,_tmpTitle,_tmpSemester,_tmpYear,_tmpExamType,_tmpContent,_tmpFilePath,_tmpFileName,_tmpDriveLink,_tmpCreatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
