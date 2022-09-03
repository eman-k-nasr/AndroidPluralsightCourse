package com.example.androidpluralsightcourse.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.androidpluralsightcourse.R;
import com.example.androidpluralsightcourse.notes.data.CourseInfo;

import java.util.List;

public class ListViewAdapter extends ArrayAdapter<CourseInfo> {

    public ListViewAdapter(@NonNull Context context, List<CourseInfo> list) {
        super(context, 0,list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View currentItemView = convertView;

        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.course_item_layout, parent, false);
        }

        CourseInfo course = getItem(position);
        TextView textView = currentItemView.findViewById(R.id.courseTitleTextView);
        textView.setText(course.getTitle());

        return currentItemView;
    }
}
