package com.example.androidpluralsightcourse.notes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidpluralsightcourse.R;
import com.example.androidpluralsightcourse.notes.models.CourseInfo;

import java.util.List;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.ViewHolder> {

    private final Context mContext;
    private final List<CourseInfo> mCourses;
    private final LayoutInflater mLayoutInflater;

    public CoursesAdapter(Context context, List<CourseInfo> notes) {
        mContext = context;
        mCourses = notes;
        mLayoutInflater = LayoutInflater.from(mContext);
    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.course_item_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseInfo courseInfo = mCourses.get(position);
        holder.courseTitleTextView.setText(courseInfo.getTitle());
    }


    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView courseTitleTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            courseTitleTextView = itemView.findViewById(R.id.courseTitleTextView);
            itemView.setOnClickListener(v -> {

            });
        }
    }
}
