package com.example.androidpluralsightcourse.notes.views;

import static com.example.androidpluralsightcourse.notes.Constants.ID_NOT_SET;
import static com.example.androidpluralsightcourse.notes.Constants.LOADER_COURSES;
import static com.example.androidpluralsightcourse.notes.Constants.LOADER_NOTES;
import static com.example.androidpluralsightcourse.notes.Constants.NOTE_ID;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.androidpluralsightcourse.R;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperOpenHelper;
import com.example.androidpluralsightcourse.notes.models.CourseInfo;

public class NoteActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String NOTE_INFO = "com.example.androidpluralsightcourse.notes.data.NOTE_INFO";
    private int mNoteID;

    private boolean mIsNewNote;
    private boolean mIsCancelling;

    private EditText noteTitleEditText;
    private EditText noteTextEditText;
    private Spinner spinnerCourses;

    private SQLiteOpenHelper mDbOpenHelper;
    private boolean mCoursesQueryFinished;
    private boolean mNotesQueryFinished;
    private Cursor mNoteCursor;
    private SimpleCursorAdapter adapterCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setUpViews();
        initOpenHelper();
        initCoursesSpinner();
        readDisplayStateValues();
        setUpCustomToolBar();
    }

    private void setUpViews(){
        noteTitleEditText = findViewById(R.id.text_note_title);
        noteTextEditText = findViewById(R.id.text_note_text);
        spinnerCourses = findViewById(R.id.spinner_courses);
    }

    private void initOpenHelper() {
        mDbOpenHelper = new NoteKeeperOpenHelper(this);
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        int noteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mNoteID = noteId;
        mIsNewNote = mNoteID == ID_NOT_SET;
        if(mIsNewNote){
            createNewNote();
        }else{
            Bundle args = new Bundle();
            args.putInt(NOTE_ID,noteId);
            getLoaderManager().initLoader(LOADER_NOTES, args, this);
        }
    }

    private void setUpCustomToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initCoursesSpinner(){
        adapterCourses = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1},
                0);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapterCourses);
        getLoaderManager().initLoader(LOADER_COURSES, null, this);
    }

    private void createNewNote() {
        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "");

        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        mNoteID = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            if(mIsNewNote){
                deleteNoteFromDatabase();
            }
        }else{
            saveCurrentNote();
        }

    }

    private void deleteNoteFromDatabase() {
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteID)};
        @SuppressLint("StaticFieldLeak") AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                return null;
            }
        };
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_send_email:
                sendEmail();
                break;
            case R.id.action_cancel:
                mIsCancelling = true;
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) spinnerCourses.getSelectedItem();
        String subject = noteTitleEditText.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n" + noteTextEditText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    private void saveCurrentNote() {
        String courseId = selectedCourseId();
        String title = noteTitleEditText.getText().toString();
        String text = noteTextEditText.getText().toString();
        saveNoteToDatabase(courseId,title,text);
    }

    private String selectedCourseId() {
        int selectedPosition = spinnerCourses.getSelectedItemPosition();
        Cursor cursor = adapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        return  cursor.getString(courseIdPos);
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteID)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES)
            loader = createLoaderNotes(args.getInt(NOTE_ID));
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
                String[] courseColumns = {
                        CourseInfoEntry.COLUMN_COURSE_TITLE,
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry._ID
                };
                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                        null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);

            }
        };
    }

    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderNotes(int id) {
        mNotesQueryFinished = false;
        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

                String selection = NoteInfoEntry._ID + " = ?";
                String[] selectionArgs = {Integer.toString(id)};

                String[] noteColumns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT
                };
                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                        selection, selectionArgs, null, null, null);

            }
        };
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_NOTES)
            loadFinishedNotes(data);
        else if(loader.getId() == LOADER_COURSES) {
            adapterCourses.changeCursor(data);
            mCoursesQueryFinished = true;
            displayNoteWhenQueriesFinished(data);
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        mNotesQueryFinished = true;
        displayNoteWhenQueriesFinished(data);

    }

    private void displayNoteWhenQueriesFinished(Cursor cursor) {
        if(mNotesQueryFinished && mCoursesQueryFinished)
            displayNote(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES) {
            if(mNoteCursor != null)
                mNoteCursor.close();
        } else if(loader.getId() == LOADER_COURSES) {
            adapterCourses.changeCursor(null);
        }
    }

    private void displayNote(Cursor cursor) {
        int courseIdPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        int noteTitlePos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        boolean hasNext = cursor.moveToNext();
        if(hasNext){
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);
            String courseId = cursor.getString(courseIdPos);

            int selectedCourseIndex = getIndexOfCourseByID(courseId);
            spinnerCourses.setSelection(selectedCourseIndex);
            noteTitleEditText.setText(noteTitle);
            noteTextEditText.setText(noteText);
        }
    }


    private int getIndexOfCourseByID(String id) {
        Cursor cursor = adapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;
        boolean more = cursor.moveToFirst();
        while(more) {
            String courseId = cursor.getString(courseIdPos);
            if(id.equals(courseId))
                break;
            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }
}