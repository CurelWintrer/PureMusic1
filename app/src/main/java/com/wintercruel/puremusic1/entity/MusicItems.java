package com.wintercruel.puremusic1.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "music")
public class MusicItems {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "MusicName")
    private String MusicName;

    @ColumnInfo(name = "ArtistName")
    private String ArtistName;

    @ColumnInfo(name = "AlbumArt")
    private String AlbumArt;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMusicName() {
        return MusicName;
    }

    public void setMusicName(String musicName) {
        MusicName = musicName;
    }

    public String getArtistName() {
        return ArtistName;
    }

    public void setArtistName(String artistName) {
        ArtistName = artistName;
    }

    public String getAlbumArt() {
        return AlbumArt;
    }

    public void setAlbumArt(String albumArt) {
        AlbumArt = albumArt;
    }
}
