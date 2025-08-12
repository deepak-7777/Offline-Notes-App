package com.example.note.Model;

public class NoteModel {
    private String id;
    private String title;
    private String content;
    private String dateTime;
    private boolean isPinned;

    public NoteModel(String id, String title, String content, String dateTime, boolean isPinned) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.dateTime = dateTime;
        this.isPinned = isPinned;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDateTime() { return dateTime; }
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
}