package com.example.note.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.Model.NoteModel;
import com.example.note.R;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private Context context;
    private List<NoteModel> noteList;
    private SharedPreferences sharedPreferences;

    public interface OnNoteClickListener {
        void onNoteClick(NoteModel note);
        void onNoteLongClick(NoteModel note, View anchorView);
    }

    private final OnNoteClickListener listener;

    public NoteAdapter(Context context, List<NoteModel> noteList, OnNoteClickListener listener) {
        this.context = context;
        this.noteList = noteList;
        this.listener = listener;
        this.sharedPreferences = context.getSharedPreferences("Notes", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteModel note = noteList.get(position);

        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.dateTime.setText(note.getDateTime());

        // Show pin icon if pinned
        holder.pinIcon.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onNoteLongClick(note, v);
            return true;
        });
    }


    private void showPopupMenu(View anchorView, NoteModel note, int position) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(context, R.style.CustomPopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, anchorView, Gravity.END, 0, R.style.CustomPopupMenu);
        popupMenu.getMenu().add(note.isPinned() ? "Unpin" : "Pin");
        popupMenu.getMenu().add("Delete");
        popupMenu.getMenu().add("Share");

        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            SpannableString span = new SpannableString(popupMenu.getMenu().getItem(i).getTitle());
            span.setSpan(new ForegroundColorSpan(Color.WHITE), 0, span.length(), 0);
            popupMenu.getMenu().getItem(i).setTitle(span);
        }

        popupMenu.setOnMenuItemClickListener(item -> handleMenuClick(item, note, position));
        popupMenu.show();
    }


    private boolean handleMenuClick(MenuItem item, NoteModel note, int position) {
        String action = item.getTitle().toString();
        SharedPreferences sp = context.getSharedPreferences("Notes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        switch (action) {
            case "Delete":
                new androidx.appcompat.app.AlertDialog.Builder(new ContextThemeWrapper(context, R.style.BlackAlertDialog))
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            editor.remove(note.getId() + "_title");
                            editor.remove(note.getId() + "_content");
                            editor.remove(note.getId() + "_datetime");
                            editor.remove(note.getId() + "_pinned");
                            editor.apply();
                            noteList.remove(position);
                            notifyItemRemoved(position);
//                            Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
                break;

            case "Pin":
                note.setPinned(true);
                editor.putBoolean(note.getId() + "_pinned", true);
                editor.apply();
                notifyItemChanged(position);
//                Toast.makeText(context, "Note pinned", Toast.LENGTH_SHORT).show();
                break;

            case "Unpin":
                note.setPinned(false);
                editor.putBoolean(note.getId() + "_pinned", false);
                editor.apply();
                notifyItemChanged(position);
                Toast.makeText(context, "Note unpinned", Toast.LENGTH_SHORT).show();
                break;

            case "Share":
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareText = note.getTitle() + "\n\n" + note.getContent();
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                context.startActivity(Intent.createChooser(shareIntent, "Share Note via"));
                break;
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, dateTime;
        ImageView pinIcon;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            content = itemView.findViewById(R.id.noteContent);
            dateTime = itemView.findViewById(R.id.noteDateTime);
            pinIcon = itemView.findViewById(R.id.pinIcon);
        }
    }
}
