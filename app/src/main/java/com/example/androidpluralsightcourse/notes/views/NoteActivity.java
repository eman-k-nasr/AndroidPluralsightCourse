package com.example.androidpluralsightcourse.notes.views;

import static com.example.androidpluralsightcourse.notes.Constants.NOTE_ID;
import static com.example.androidpluralsightcourse.notes.Constants.ID_NOT_SET;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.androidpluralsightcourse.R;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperOpenHelper;
import com.example.androidpluralsightcourse.notes.models.CourseInfo;
import com.example.androidpluralsightcourse.notes.data.DataManager;
import com.example.androidpluralsightcourse.notes.models.NoteInfo;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_INFO = "com.example.androidpluralsightcourse.notes.data.NOTE_INFO";
    private NoteInfo mNote;
    private int mNoteID;

    private boolean mIsNewNote;
    private boolean mIsCancelling;

    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;

    private EditText noteTitleEditText;
    private EditText noteTextEditText;
    private Spinner spinnerCourses;

    private SQLiteOpenHelper mDbOpenHelper;

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
            displayNote(noteId);
        }
        saveOriginalNoteValues();
    }

    private void setUpCustomToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initCoursesSpinner(){
        SimpleCursorAdapter adapterCourses =
                new SimpleCursorAdapter(this,
                        android.R.layout.simple_spinner_item, null,
                        new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                        new int[]{android.R.id.text1},
                        0);
        adapterCourses.changeCursor(getCoursesTableCursor());
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapterCourses);
    }

    private Cursor getCoursesTableCursor() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        return db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
    }

    private void displayNote(int id) {
        Cursor cursor = getNoteCursor(id);
        mNote = getNoteData(cursor);
        setNoteData();
    }

    private Cursor getNoteCursor(int id) {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(id)};
        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
        };
        return db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                selection, selectionArgs, null, null, null);
    }

    private NoteInfo getNoteData(Cursor cursor) {
        int courseIdPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        int noteTitlePos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        boolean hasNext = cursor.moveToNext();
        if(hasNext){
            String courseId = cursor.getString(courseIdPos);
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);

            CourseInfo course = DataManager.getInstance().getCourse(courseId);
            return new NoteInfo(course,noteTitle,noteText);
        }

        return new NoteInfo(DataManager.getInstance().getCourse("android_intents"),"","");
    }

    private void setNoteData(){
        int selectedCourseIndex = getIndexOfCourseByID(mNote.getCourse().getCourseId());
        spinnerCourses.setSelection(selectedCourseIndex);
        noteTitleEditText.setText(mNote.getTitle());
        noteTextEditText.setText(mNote.getText());
    }

    private int getIndexOfCourseByID(String id) {
        int courseIdColumnIndex = getCoursesTableCursor().getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        Cursor cursor = getCoursesTableCursor();
        int courseRowIndex = 0;
        while(cursor.moveToNext()) {
            String courseId = cursor.getString(courseIdColumnIndex);
            if(courseId.equals(id))
                break;
            courseRowIndex++;
        }
        return courseRowIndex;
    }

    private void createNewNote() {
        mNoteID = DataManager.getInstance().createNewNote();
        mNote = getNoteByPosition(mNoteID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            if(mIsNewNote){
                DataManager.getInstance().removeNote(mNoteID);
            }else{
                storePreviousNoteValues();
            }
        }else{
            saveCurrentNote();
        }

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
            case R.id.action_next:
                moveToNextNote();
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

    private void moveToNextNote(){
        saveCurrentNote();
//        getNextNote();
        saveOriginalNoteValues();
//        displayNote();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem nextMenuItem = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() -1 ;
        nextMenuItem.setEnabled(mNoteID != lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }
    
    private void getNextNote(){
//        ++mNotePosition;
//        mNote = getNoteByPosition(mNotePosition);
    }

    private NoteInfo getNoteByPosition(int position){
        return DataManager.getInstance().getNotes().get(position);
    }

    private void saveCurrentNote() {
        mNote.setCourse((CourseInfo) spinnerCourses.getSelectedItem());
        mNote.setTitle(noteTitleEditText.getText().toString());
        mNote.setText(noteTextEditText.getText().toString());
    }

    private void saveOriginalNoteValues() {
        if(mIsNewNote) return;
        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);
    }
}