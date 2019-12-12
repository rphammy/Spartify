package com.example.spartify1;

public class Song {

    private String id;
    private String title;
    private String uri;
    private String artist;

    public Song(){}
    
    public Song(String id, String title, String uri, String artist) {
        this.title = title;
        this.id = id;
        this.uri = uri;
        this.artist = artist;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
    @Override
    public String toString() {
        return this.title + " by " + this.artist;
    }
}
