package com.example.androidpluralsightcourse.notes.views;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.androidpluralsightcourse.notes.data.CourseInfo;
import com.example.androidpluralsightcourse.notes.data.DataManager;
import com.example.androidpluralsightcourse.notes.data.NoteInfo;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityMainBinding binding;
    private NotesAdapter notesAdapter;
    private CoursesAdapter coursesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setPrefDefaultValues();
        displayNotes();
        navigateToCreateNewNote();
        setUpSupportActionBar();
        setupDrawerToggle();
        setUpNavigationView();

    }

    private void setPrefDefaultValues(){
        PreferenceManager.setDefaultValues(this,R.xml.general_preferences,false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notesAdapter.notifyDataSetChanged();
        updateNavHeader();
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
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        coursesAdapter = new CoursesAdapter(this, courses);
        recyclerView.setAdapter(coursesAdapter);
        setMenuItemChecked(R.id.menu_courses_item);
    }

    private void displayNotes(){
        RecyclerView recyclerView = binding.appBarMain.contentMain.contentRecyclerView;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        notesAdapter = new NotesAdapter(this, notes);
        recyclerView.setAdapter(notesAdapter);
        setMenuItemChecked(R.id.menu_notes_item);
    }

    private void setMenuItemChecked(int menuId){
        NavigationView navView = binding.navView;
        navView.getMenu().findItem(menuId).setChecked(true);
    }
}