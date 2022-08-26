package com.example.androidpluralsightcourse.counter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.androidpluralsightcourse.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class CounterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        increaseCount();
    }

    private void increaseCount(){
        FloatingActionButton btn = findViewById(R.id.counter_btn);
        btn.setOnClickListener((view) -> {
            TextView counter = findViewById(R.id.counter_text_view);
            int oldValue = Integer.parseInt(counter.getText().toString());
            int newValue = Worker.doubleValue(oldValue);
            counter.setText(Integer.toString(newValue));
            Snackbar.make(view,"value changed from "+oldValue+" to "+newValue,Snackbar.LENGTH_LONG)
                    .setAction("Action",null).show();
        });
    }
}