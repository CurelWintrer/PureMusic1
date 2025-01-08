package com.wintercruel.puremusic1.entity;

public class MusicItem {
    private String id;
    private String MusicName;
    private String ArtistName;
    private String MusicImage;

    public MusicItem(String name,String artist,String album){
        this.MusicName=name;
        this.ArtistName=artist;
        this.MusicImage=album;
    }


    public String getMusicImage() {
        return MusicImage;
    }

    public String getArtistName() {
        return ArtistName;
    }

    public String getMusicName() {
        return MusicName;
    }

    public void setArtistName(String artistName) {
        ArtistName = artistName;
    }

    public void setMusicImage(String musicImage) {
        MusicImage = musicImage;
    }

    public void setMusicName(String musicName) {
        MusicName = musicName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
