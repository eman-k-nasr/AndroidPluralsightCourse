package com.example.androidpluralsightcourse.activityWithResults;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.androidpluralsightcourse.R;

public class SecondActivity extends AppCompatActivity {
    private EditText nameEditText;
    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        nameEditText = findViewById(R.id.editTextTextPersonName);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener( v-> {
            String message=nameEditText.getText().toString();
            Intent intent=new Intent();
            intent.putExtra("MESSAGE",message);
            setResult(2,intent);
            finish();
        });
    }
}