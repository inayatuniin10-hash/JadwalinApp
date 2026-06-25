package com.example.jadwalin.data.model;

public class Catatan {
    private String id, title, content, imageUrl, date;

    public Catatan() {} //  untuk Firebase

    public Catatan(String id, String title, String content, String imageUrl, String date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    // Getter dan Setter
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public String getDate() { return date; }
}