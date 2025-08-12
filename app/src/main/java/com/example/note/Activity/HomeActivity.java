package com.example.note.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.Adapter.NoteAdapter;
import com.example.note.Model.NoteModel;
import com.example.note.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private NoteAdapter noteAdapter;
    private List<NoteModel> noteList;
    private SharedPreferences sharedPreferences;
    private TextView noNotes;
    private TextView title;
    private ImageButton menuButton;
    private int sortType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        noNotes = findViewById(R.id.no_notes);
        menuButton = findViewById(R.id.menu_button);
        title = findViewById(R.id.title);

        sharedPreferences = getSharedPreferences("Notes", Context.MODE_PRIVATE);
        noteList = new ArrayList<>();

        noteAdapter = new NoteAdapter(this, noteList, new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(NoteModel note) {
                Intent intent = new Intent(HomeActivity.this, AddActivity.class);
                intent.putExtra("noteId", note.getId());
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                intent.putExtra("dateTime", note.getDateTime());
                startActivity(intent);
            }

            @Override
            public void onNoteLongClick(NoteModel note, View anchorView) {
                showNoteOptions(note, anchorView);
            }
        });

        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(noteAdapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddActivity.class)));

        menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v, Gravity.END, 0, R.style.CustomPopupMenu);
            popupMenu.getMenuInflater().inflate(R.menu.my_menu, popupMenu.getMenu());
            applyWhiteMenuText(popupMenu);
            popupMenu.setOnMenuItemClickListener(this::handleMenuClick);
            popupMenu.show();
        });

        title.setOnClickListener(v -> showSortOptions());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        noteList.clear();
        Map<String, ?> all = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().endsWith("_title")) {
                String id = entry.getKey().replace("_title", "");
                String title = (String) entry.getValue();
                String content = sharedPreferences.getString(id + "_content", "");
                String dateTime = sharedPreferences.getString(id + "_datetime", "");
                boolean pinned = sharedPreferences.getBoolean(id + "_pinned", false);
                noteList.add(new NoteModel(id, title, content, dateTime, pinned));
            }
        }

        List<NoteModel> pinnedList = new ArrayList<>();
        List<NoteModel> unpinnedList = new ArrayList<>();

        for (NoteModel note : noteList) {
            if (note.isPinned()) pinnedList.add(note);
            else unpinnedList.add(note);
        }

        Comparator<NoteModel> comparator = null;
        if (sortType == 1) comparator = Comparator.comparing(NoteModel::getDateTime);
        else if (sortType == 2) comparator = Comparator.comparingInt(n -> n.getTitle().length());

        if (comparator != null) {
            Collections.sort(pinnedList, comparator);
            Collections.sort(unpinnedList, comparator);
        }

        noteList.clear();
        noteList.addAll(pinnedList);
        noteList.addAll(unpinnedList);

        noteAdapter.notifyDataSetChanged();
        noNotes.setVisibility(noteList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showNoteOptions(NoteModel note, View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView, Gravity.END, 0, R.style.CustomPopupMenu);
        popupMenu.getMenu().add("Delete");
        popupMenu.getMenu().add("Share");
        popupMenu.getMenu().add(note.isPinned() ? "Unpin" : "Pin");

        applyWhiteMenuText(popupMenu);

        popupMenu.setOnMenuItemClickListener(item -> {
            String action = item.getTitle().toString();
            if (action.equals("Delete")) deleteNote(note);
            else if (action.equals("Share")) shareNote(note);
            else togglePin(note);
            return true;
        });

        popupMenu.show();
    }

    private void deleteNote(NoteModel note) {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.BlackAlertDialog)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(note.getId() + "_title");
                    editor.remove(note.getId() + "_content");
                    editor.remove(note.getId() + "_datetime");
                    editor.remove(note.getId() + "_pinned");
                    editor.apply();
                    loadNotes();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void shareNote(NoteModel note) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, note.getContent());
        startActivity(Intent.createChooser(shareIntent, "Share Note using"));
    }

    private void togglePin(NoteModel note) {
        boolean newPinState = !note.isPinned();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(note.getId() + "_pinned", newPinState);
        editor.apply();
        loadNotes();
    }

    private boolean handleMenuClick(MenuItem item) {
        if (item.getItemId() == R.id.option_one) {
            Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.setting) {
            startActivity(new Intent(this, SettingActivity.class));
            return true;
        }
        return false;
    }

    private void showSortOptions() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_sort, null);
        bottomSheetDialog.setContentView(view);

        TextView normalOption = view.findViewById(R.id.option_normal);
        TextView timingOption = view.findViewById(R.id.option_timing);
        TextView titleOption = view.findViewById(R.id.option_title_shortest);

        //  Set color based on selected sort type
        normalOption.setTextColor(sortType == 0 ? Color.YELLOW : Color.WHITE);
        timingOption.setTextColor(sortType == 1 ? Color.YELLOW : Color.WHITE);
        titleOption.setTextColor(sortType == 2 ? Color.YELLOW : Color.WHITE);

        //  Handle clicks and update UI
        normalOption.setOnClickListener(v -> {
            sortType = 0;
            loadNotes();
            bottomSheetDialog.dismiss();
        });

        timingOption.setOnClickListener(v -> {
            sortType = 1;
            loadNotes();
            bottomSheetDialog.dismiss();
        });

        titleOption.setOnClickListener(v -> {
            sortType = 2;
            loadNotes();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }


    private void applyWhiteMenuText(PopupMenu popupMenu) {
        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            SpannableString spanString = new SpannableString(popupMenu.getMenu().getItem(i).getTitle());
            spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0);
            popupMenu.getMenu().getItem(i).setTitle(spanString);
        }
    }
}