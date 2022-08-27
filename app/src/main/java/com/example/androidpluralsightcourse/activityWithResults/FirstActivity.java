package com.example.androidpluralsightcourse.activityWithResults;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.androidpluralsightcourse.R;

public class FirstActivity extends AppCompatActivity {
    private TextView nameTextView;
    private Button goBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        nameTextView = findViewById(R.id.nameTextView);
        goBtn = findViewById(R.id.goBtn);
        goBtn.setOnClickListener(v -> {
            Intent intent=new Intent(FirstActivity.this,SecondActivity.class);
            startActivityForResult(intent, 2);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2){
            String message=data.getStringExtra("MESSAGE");
            nameTextView.setText(message);
        }
    }
}