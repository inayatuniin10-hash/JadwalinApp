package com.example.jadwalin.data.model;

public class Task {
    private String id;
    private String userId;
    private String kategori;
    private String title;
    private String dueDate;
    private boolean isCompleted;

    // Kontraktor Kosong (Wajib untuk Firebase)
    public Task() {
    }

    // Konstruktor Utama
    public Task(String id, String userId, String title, String kategori, String dueDate, boolean isCompleted) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.kategori = kategori;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
    }

    // GETTER DAN SETTER

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}