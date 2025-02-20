package com.example.valuefy_project;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SavedKeyPoints extends AppCompatActivity {

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_key_points);

        TextView textView = findViewById(R.id.savedKeyPointsText);
        textView.setText("Here are your saved key points.");
    }
}