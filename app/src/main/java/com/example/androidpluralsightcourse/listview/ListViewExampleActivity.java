package com.example.androidpluralsightcourse.listview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.example.androidpluralsightcourse.R;
import com.example.androidpluralsightcourse.notes.data.CourseInfo;
import com.example.androidpluralsightcourse.notes.data.DataManager;

import java.util.ArrayList;
import java.util.List;

public class ListViewExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_example);
        ListView listView = findViewById(R.id.listview);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ListViewAdapter adapter = new ListViewAdapter(this,courses);
        listView.setAdapter(adapter);
    }
}