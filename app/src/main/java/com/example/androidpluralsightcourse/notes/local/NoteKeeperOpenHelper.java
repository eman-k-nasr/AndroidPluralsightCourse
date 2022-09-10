package com.example.androidpluralsightcourse.notes.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteKeeperOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "NoteKeeper.db";
    public static final int DATABASE_VERSION = 2;
    public NoteKeeperOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CourseInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(NoteInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(CourseInfoEntry.SQL_CREATE_COURSE_TITLE_INDEX);
        db.execSQL(NoteInfoEntry.SQL_CREATE_NOTE_TITLE_INDEX);
        DatabaseDataWorker worker = new DatabaseDataWorker(db);
        worker.insertCourses();
        worker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 2){
            db.execSQL(CourseInfoEntry.SQL_CREATE_COURSE_TITLE_INDEX);
            db.execSQL(NoteInfoEntry.SQL_CREATE_NOTE_TITLE_INDEX);
        }
    }
}