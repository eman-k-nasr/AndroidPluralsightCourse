package com.example.androidpluralsightcourse.notes.views;

import static com.example.androidpluralsightcourse.notes.Constants.LOADER_NOTES;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidpluralsightcourse.R;
import com.example.androidpluralsightcourse.databinding.ActivityMainBinding;
import com.example.androidpluralsightcourse.notes.adapter.CoursesAdapter;
import com.example.androidpluralsightcourse.notes.adapter.NotesAdapter;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.androidpluralsightcourse.notes.local.NoteKeeperOpenHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
        , LoaderManager.LoaderCallbacks<Cursor> {
    private ActivityMainBinding binding;
    private NotesAdapter notesAdapter;
    private CoursesAdapter coursesAdapter;
    private NoteKeeperOpenHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initOpenHelper();
        setPrefDefaultValues();
        displayNotes();
        navigateToCreateNewNote();
        setUpSupportActionBar();
        setupDrawerToggle();
        setUpNavigationView();
    }

    private void initOpenHelper() {
        dbHelper = new NoteKeeperOpenHelper(this);
    }

    private void setPrefDefaultValues(){
        PreferenceManager.setDefaultValues(this,R.xml.general_preferences,false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_NOTES, null, this);
        updateNavHeader();
    }

    private Cursor getNotesCursor() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        final String[] noteColumns = {
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry._ID};

        String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
        return  db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                null, null, null, null, noteOrderBy);
    }

    private void updateNavHeader() {
        NavigationView navView = binding.navView;
        View header = navView.getHeaderView(0);
        TextView username = header.findViewById(R.id.usernameTextView);
        TextView email = header.findViewById(R.id.emailTextView);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String usernamePrefKey = getString(R.string.user_key);
        String emailPrefKey = getString(R.string.email_key);
        username.setText(pref.getString(usernamePrefKey,""));
        email.setText(pref.getString(emailPrefKey,""));
    }

    private void navigateToCreateNewNote(){
        binding.appBarMain.fab.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, NoteActivity.class));
        });
    }

    private void setUpSupportActionBar(){
        setSupportActionBar(binding.appBarMain.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
    }

    private void setupDrawerToggle(){
        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                MainActivity.this,
                drawer,
                binding.appBarMain.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void setUpNavigationView(){
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_courses_item:
                displayCourses();
                break;
            case R.id.menu_notes_item:
                 displayNotes();
                break;
            case R.id.menu_share:
                share();
                break;
            default:
                break;
        }
        closeDrawer(binding.drawerLayout);
        return true;
    }

    private void share() {
        String favoriteSocialMedia = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.social_key),"twitter");
        showSnackBar("share to "+favoriteSocialMedia);
    }

    private void showSnackBar(String message){
        View root = binding.appBarMain.mainRoot;
        Snackbar.make(root,message,Snackbar.LENGTH_LONG).show();
    }
    @Override
    public void onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            closeDrawer(binding.drawerLayout);
        }else{
            super.onBackPressed();
        }
    }

    private void closeDrawer(DrawerLayout drawer){
        drawer.close();
    }

    private void displayCourses() {
        RecyclerView recyclerView = binding.appBarMain.contentMain.contentRecyclerView;
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.courses_grid_span));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(coursesAdapter);
        setMenuItemChecked(R.id.menu_courses_item);
    }

    private void displayNotes(){
        RecyclerView recyclerView = binding.appBarMain.contentMain.contentRecyclerView;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        notesAdapter = new NotesAdapter(this, null);
        recyclerView.setAdapter(notesAdapter);
        setMenuItemChecked(R.id.menu_notes_item);
    }

    private void setMenuItemChecked(int menuId){
        NavigationView navView = binding.navView;
        navView.getMenu().findItem(menuId).setChecked(true);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES) {
            loader = new CursorLoader(this) {
                @Override
                public Cursor loadInBackground() {
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    String[] noteColumns = {
                            NoteInfoEntry.getQName(NoteInfoEntry._ID),
                            NoteInfoEntry.COLUMN_NOTE_TITLE,
                            CourseInfoEntry.COLUMN_COURSE_TITLE};

                    String noteOrderBy = CourseInfoEntry.COLUMN_COURSE_TITLE +
                            "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

                    String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
                            CourseInfoEntry.TABLE_NAME + " ON " +
                            NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                            CourseInfoEntry.getQName( CourseInfoEntry.COLUMN_COURSE_ID);

                    return db.query(tablesWithJoin, noteColumns,
                            null, null, null, null, noteOrderBy);
                }
            };
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_NOTES)  {
            notesAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES)  {
            notesAdapter.changeCursor(null);
        }
    }

}