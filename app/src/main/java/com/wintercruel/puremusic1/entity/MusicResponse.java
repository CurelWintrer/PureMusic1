package com.wintercruel.puremusic1.entity;

import java.util.List;

public class MusicResponse {
    private List<MusicItems> songs;

    public List<MusicItems> getSongs() {
        return songs;
    }

    public void setSongs(List<MusicItems> songs) {
        this.songs = songs;
    }
}