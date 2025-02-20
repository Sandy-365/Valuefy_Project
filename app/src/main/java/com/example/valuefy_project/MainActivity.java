package com.example.valuefy_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.database.Cursor;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.files.types.UploadedFile;
import com.assemblyai.api.resources.transcripts.requests.TranscriptParams;
import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "e3288e102c124087944a4e2f7cc8880e"; // API key
    private static final int PICK_AUDIO_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_SPEECH_INPUT = 2000;
    private TextView textView;
    private Button pickFileButton;
    private Button extract;
    private ImageButton recordButton;
    public static final String transtext = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.demotext);
        extract = findViewById(R.id.extract);
        pickFileButton = findViewById(R.id.pickFileButton);
        recordButton = findViewById(R.id.record);

        // Request permissions for storage and microphone
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }

        // File Picker
        pickFileButton.setOnClickListener(v -> openFileChooser());

        // Extract Button
        extract.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Opening Gemini API", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            String t = textView.getText().toString();
            intent.putExtra(transtext, t);
            startActivity(intent);
        });

        // Start Live Speech-to-Text
        recordButton.setOnClickListener(v -> startSpeechToText());

        // Navigate to Saved Key Points
        Button savedKeyButton = findViewById(R.id.savedkey);
        savedKeyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SavedKeyPoints.class);
            startActivity(intent);
        });

        // Navigate to Saved ToDo
        Button savedToDo = findViewById(R.id.savedTodo);
        savedToDo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SavedToDo.class);
            startActivity(intent);
        });
    }

    // Open File Picker
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(Intent.createChooser(intent, "Select Audio File"), PICK_AUDIO_REQUEST);
    }

    // Start Live Speech Recognition
    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        // Increase pause time for silence detection
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000); // 5 sec
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000); // 3 sec
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 15000); // 15 sec

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle File Picker & Speech Results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                String filePath = getFilePathFromUri(fileUri);
                audioTrans(filePath);
            }
        } else if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                textView.setText("Transcription:\n" + result.get(0));
            }
        }
    }

    // Get File Path from URI
    private String getFilePathFromUri(Uri uri) {
        String fileName = "selected_audio.mp3";

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }

        File file = new File(getCacheDir(), fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

        } catch (Exception e) {
            runOnUiThread(() -> textView.setText("Error reading file: " + e.getMessage()));
        }

        return file.getAbsolutePath();
    }

    // Transcribe Audio using AssemblyAI
    public void audioTrans(String localFilePath) {
        new Thread(() -> {
            try {
                File file = new File(localFilePath);
                if (!file.exists()) {
                    runOnUiThread(() -> textView.setText("File not found: " + localFilePath));
                    return;
                }

                AssemblyAI client = AssemblyAI.builder()
                        .apiKey(API_KEY)
                        .build();

                UploadedFile uploadedFile = client.files().upload(file);
                String uploadedFileUrl = uploadedFile.getUploadUrl();

                TranscriptParams transcriptParams = TranscriptParams.builder()
                        .audioUrl(uploadedFileUrl)
                        .build();

                Transcript transcript = client.transcripts().transcribe(transcriptParams);

                if (transcript.getStatus() == TranscriptStatus.ERROR) {
                    throw new Exception("Transcript failed: " + transcript.getError().orElse("Unknown error"));
                }

                runOnUiThread(() -> textView.setText("Transcription:\n" + transcript.getText()));

            } catch (Exception e) {
                runOnUiThread(() -> textView.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
}
