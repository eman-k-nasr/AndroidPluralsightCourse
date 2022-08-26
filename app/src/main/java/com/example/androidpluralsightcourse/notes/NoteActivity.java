package com.example.androidpluralsightcourse.notes;

import static com.example.androidpluralsightcourse.notes.Constants.NOTE_POSITION;
import static com.example.androidpluralsightcourse.notes.Constants.POSITION_NOT_SET;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.androidpluralsightcourse.R;
import com.example.androidpluralsightcourse.notes.data.CourseInfo;
import com.example.androidpluralsightcourse.notes.data.DataManager;
import com.example.androidpluralsightcourse.notes.data.NoteInfo;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_INFO = "com.example.androidpluralsightcourse.notes.data.NOTE_INFO";
    private NoteInfo mNewNote;
    private boolean mIsNewNote;
    private EditText textNoteTitle;
    private EditText textNoteText;
    private Spinner spinnerCourses;
    private int mNewNotePosition;
    private boolean mIsCancelling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setUpViews();
        setUpCustomToolBar();
        initCoursesSpinner();
        readDisplayStateValues();

        if(!mIsNewNote) displayNote();


    }

    private void setUpViews(){
        textNoteTitle = findViewById(R.id.text_note_title);
        textNoteText = findViewById(R.id.text_note_text);
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

    private void displayNote() {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNewNote.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNewNote.getTitle());
        textNoteText.setText(mNewNote.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION,POSITION_NOT_SET);
        mIsNewNote = position == POSITION_NOT_SET;
        if(mIsNewNote){
            createNewNote();
        }else{
            mNewNote = DataManager.getInstance().getNotes().get(position);
        }
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNewNotePosition = dm.createNewNote();
        mNewNote = dm.getNotes().get(mNewNotePosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            if(mIsNewNote){
                DataManager.getInstance().removeNote(mNewNotePosition);
            }
        }else{
            saveNote();
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
            case R.id.action_cancel:
                mIsCancelling = true;
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) spinnerCourses.getSelectedItem();
        String subject = textNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n" + textNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    private void saveNote() {
        mNewNote.setCourse((CourseInfo) spinnerCourses.getSelectedItem());
        mNewNote.setTitle(textNoteTitle.getText().toString());
        mNewNote.setText(textNoteText.getText().toString());
    }
}