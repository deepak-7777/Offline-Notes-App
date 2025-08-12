package com.example.note.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.note.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddActivity extends AppCompatActivity {

    private EditText noteTitleInput, noteContentInput;
    private TextView tickBtn;
    private String noteId = null;
    private boolean isNew = true;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        noteTitleInput = findViewById(R.id.noteTitleInput);
        noteContentInput = findViewById(R.id.noteContentInput);
        tickBtn = findViewById(R.id.tick);
        sharedPreferences = getSharedPreferences("Notes", Context.MODE_PRIVATE);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            isNew = false;
            noteId = intent.getStringExtra("id");
            noteTitleInput.setText(intent.getStringExtra("title"));
            noteContentInput.setText(intent.getStringExtra("content"));
        }

        tickBtn.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = noteTitleInput.getText().toString();
        String content = noteContentInput.getText().toString();

        if (title.isEmpty() && content.isEmpty()) {
            finish();
            return;
        }

        String dateTime = new SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault()).format(new Date());
        String id = isNew ? UUID.randomUUID().toString() : noteId;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(id + "_title", title.isEmpty() ? "Untitled" : title);
        editor.putString(id + "_content", content);
        editor.putString(id + "_datetime", dateTime);
        editor.apply();

        Toast.makeText(this, isNew ? "Note saved" : "Note updated", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void back_Btn(View view) {
        saveNote();
    }

    @Override
    public void onBackPressed() {
        saveNote();
    }
}