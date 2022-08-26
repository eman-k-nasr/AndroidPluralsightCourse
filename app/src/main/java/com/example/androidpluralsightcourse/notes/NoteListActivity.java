package com.example.androidpluralsightcourse.notes;

import static com.example.androidpluralsightcourse.notes.Constants.NOTE_POSITION;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.androidpluralsightcourse.R;
import com.example.androidpluralsightcourse.notes.data.DataManager;
import com.example.androidpluralsightcourse.notes.data.NoteInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {
    private ArrayAdapter<NoteInfo> adapterNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        setUpCustomToolBar();
        navigateToNoteActivity();
        initializeDisplayContent();
    }

    private void navigateToNoteActivity(){
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivity(new Intent(NoteListActivity.this, NoteActivity.class)));
    }

    private void setUpCustomToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initializeDisplayContent() {
        final ListView listNotes = findViewById(R.id.list_notes);

        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        adapterNotes = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, notes);

        listNotes.setAdapter(adapterNotes);

        listNotes.setOnItemClickListener((adapterView, view, position, l) -> {
            Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
            NoteInfo note = (NoteInfo) listNotes.getItemAtPosition(position);
//            intent.putExtra(NoteActivity.NOTE_INFO, note);
            intent.putExtra(NOTE_POSITION,position);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapterNotes.notifyDataSetChanged();
    }
}