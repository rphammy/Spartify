package com.example.spartify1;

public class Song {

    private String id;
    private String name;
    private String uri;
//    private String artist;

    public Song(String id, String name, String uri) {
        this.name = name;
        this.id = id;
        this.uri = uri;
//        this.artist = artist;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return this.name;
    }
//

//    public String getArtist() {
//        return artist;
//    }
//
//    public void setArtist(String artist) {
//        this.artist = artist;
//    }

}
