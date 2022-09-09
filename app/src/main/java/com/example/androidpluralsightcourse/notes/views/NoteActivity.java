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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.androidpluralsightcourse.R;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperOpenHelper;
import com.example.androidpluralsightcourse.notes.models.CourseInfo;
import com.example.androidpluralsightcourse.notes.data.DataManager;
import com.example.androidpluralsightcourse.notes.models.NoteInfo;

import java.util.List;

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
    private Cursor mNoteCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setUpViews();
        initOpenHelper();
        readDisplayStateValues();
        setUpCustomToolBar();
        initCoursesSpinner();
    }

    private void initOpenHelper() {
        mDbOpenHelper = new NoteKeeperOpenHelper(this);
    }

    private void loadNoteData(int id) {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String selection = NoteInfoEntry._ID + " = ?";

        String[] selectionArgs = {Integer.toString(id)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
        };

        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                selection, selectionArgs, null, null, null);

        displayNote(mNoteCursor);
    }

    private void setUpViews(){
        noteTitleEditText = findViewById(R.id.text_note_title);
        noteTextEditText = findViewById(R.id.text_note_text);
        spinnerCourses = findViewById(R.id.spinner_courses);
    }

    private void setUpCustomToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initCoursesSpinner(){
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapterCourses);
    }

    private void displayNote(Cursor cursor) {
        mNote = loadNoteFromCursor(cursor);
        spinnerCourses.setSelection(getCourseIndex(mNote.getCourse()));
        noteTitleEditText.setText(mNote.getTitle());
        noteTextEditText.setText(mNote.getText());
    }

    private NoteInfo loadNoteFromCursor(Cursor cursor) {
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

    private int getCourseIndex (CourseInfo course){
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        return  courses.indexOf(course);
    }

    private void readDisplayStateValues() {
        //get called when activity is first displayed
        Intent intent = getIntent();
        int noteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mNoteID = noteId;
        mIsNewNote = mNoteID == ID_NOT_SET;
        if(mIsNewNote){
            createNewNote();
        }else{
            loadNoteData(noteId);
        }
        saveOriginalNoteValues();
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
        displayNote(mNoteCursor);
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