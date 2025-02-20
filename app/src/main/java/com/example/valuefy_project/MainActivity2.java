package com.example.valuefy_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

public class MainActivity2 extends AppCompatActivity {
    TextView viewText, dateText;
    Button saveKey, addToDo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        // Initialize Views
        viewText = findViewById(R.id.textView);
        dateText = findViewById(R.id.dateView);
        saveKey = findViewById(R.id.saveKey);
        addToDo = findViewById(R.id.addToDo);

        // Set Buttons Initially GONE
        saveKey.setVisibility(View.GONE);
        addToDo.setVisibility(View.GONE);

        // Get transcription from Intent
        Intent inten = getIntent();
        String trans = inten.getStringExtra(MainActivity.transtext);

        // Refined prompt
        String finestr = "Extract the important key points from this transcript: '" + trans +
                "'. At the end, provide important dates and times in this format:\n\n" +
                "Key Points:\n[point1, point2, ...]\n\nDates and Times:\n[date1, date2, ...]";

        // Call Gemini API
        modelCall(finestr);
        Button savedKeyButton = findViewById(R.id.savedkey);
        savedKeyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, SavedKeyPoints.class);
            startActivity(intent);
        });

        Button savedToDo = findViewById(R.id.savedTodo);
        savedToDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, SavedToDo.class);
                startActivity(intent);
            }
        });

        saveKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Key points have been saved", Toast.LENGTH_SHORT).show();
            }
        });
        addToDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "ToDo have been created", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void modelCall(String finalstr) {
        GenerativeModel gm = new GenerativeModel("gemini-pro", "AIzaSyAx1CCVTUffRMJsjmW2X7T-qITXHjaGKfs");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder().addText(finalstr).build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                System.out.println(resultText);

                // Simple parsing to separate key points and dates
                String[] parts = resultText.split("Dates and Times:");
                String resultText_key = parts.length > 0 ? parts[0].replace("Key Points:", "").trim() : "No key points found.";
                String resultText_date = parts.length > 1 ? parts[1].trim() : "No dates found.";

                // Update UI on the main thread
                runOnUiThread(() -> {
                    viewText.setText(resultText_key);
                    dateText.setText(resultText_date);

                    // Make Buttons VISIBLE after data is loaded
                    saveKey.setVisibility(View.VISIBLE);
                    addToDo.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> viewText.setText("API call failed: " + t.getMessage()));
            }
        }, MoreExecutors.directExecutor());
    }
}
