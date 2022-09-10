package com.example.androidpluralsightcourse.notes.adapter;

import static android.provider.BaseColumns._ID;
import static com.example.androidpluralsightcourse.notes.Constants.NOTE_ID;
import static com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.CourseInfoEntry.COLUMN_COURSE_TITLE;
import static com.example.androidpluralsightcourse.notes.local.NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_NOTE_TITLE;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidpluralsightcourse.R;
import com.example.androidpluralsightcourse.notes.views.NoteActivity;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mNotesCursor;
    private final LayoutInflater mLayoutInflater;
    private int mCoursePos;
    private int mNoteTitlePos;
    private int mIdPos;

    public NotesAdapter(Context context,Cursor cursor) {
        mContext = context;
        mNotesCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);
        populateColumnPositions();
    }

    private void populateColumnPositions() {
        if(mNotesCursor == null)
            return;
        mCoursePos = mNotesCursor.getColumnIndex(COLUMN_COURSE_TITLE);
        mNoteTitlePos = mNotesCursor.getColumnIndex(COLUMN_NOTE_TITLE);
        mIdPos = mNotesCursor.getColumnIndex(_ID);
    }

    public void changeCursor(Cursor cursor) {
        if(mNotesCursor != null)
            mNotesCursor.close();
        mNotesCursor = cursor;
        populateColumnPositions();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.note_item_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mNotesCursor.moveToPosition(position);
        String courseId = mNotesCursor.getString(mCoursePos);
        String noteTitle = mNotesCursor.getString(mNoteTitlePos);
        int noteId = mNotesCursor.getInt(mIdPos);

        holder.mTextCourse.setText(courseId);
        holder.mTextTitle.setText(noteTitle);
        holder.noteId = noteId;
    }


    @Override
    public int getItemCount() {
        return mNotesCursor == null ? 0 : mNotesCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int noteId;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextCourse = (TextView) itemView.findViewById(R.id.courseTitleTextView);
            mTextTitle = (TextView) itemView.findViewById(R.id.noteTextView);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, NoteActivity.class);
                intent.putExtra(NOTE_ID, noteId);
                mContext.startActivity(intent);
            });
        }
    }
}
