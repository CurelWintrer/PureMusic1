package com.wintercruel.puremusic1.entity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

// 创建一个 Singleton 类来保存数据
public class MusicHolder {

    private static SharedPreferences sharedPreferences;

    private static String musicId;
    private static Bitmap albumArt;
    private static String musicName;
    private static String artistName;
    private static String musicUrl;
    private static String playListId;
    private static String playListName;
    private static String albumArtUrl;
    private static String Lyrics;
    private static String SearchResult;
    private static int playingMode;//0,列表顺序循环  1，单曲循环  2，随机播放
    private static boolean isSearchMode;
    private static int position;
    private static String AlbumColor1;
    private static String AlbumColor2;

    private static boolean playerMode;  //播放器模式，true为酷狗反之网易云



    // 静态初始化方法，用于在应用启动时设置 SharedPreferences
    public static void initSharedP(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("MusicHolderPreferences", Context.MODE_PRIVATE);
        }
    }


    public static void setVisitorLogin(boolean isLogin){
        sharedPreferences.edit().putBoolean("isVisitLogin",isLogin).apply();
    }

    public static boolean isVisitLogin(){
        return sharedPreferences.getBoolean("isVisitLogin",false);
    }


    public static void setAlbumArt(Bitmap art) {
        albumArt = art;
    }

    public static Bitmap getAlbumArt() {
        return albumArt;
    }
//    public static void setMusicName(String MusicName){
//        musicName=MusicName;
//    }

    public static void setMusicName(String musicName) {
        sharedPreferences.edit().putString("musicName", musicName).apply();
    }

//    public static String getMusicName() {
//        return musicName;
//    }

    public static String getMusicName() {
        return sharedPreferences.getString("musicName", null);
    }

//    public static void setArtistName(String artistName) {
//        MusicHolder.artistName = artistName;
//    }
//
//    public static String getArtistName() {
//        return artistName;
//    }

    public static void setArtistName(String artistName) {
        sharedPreferences.edit().putString("artistName", artistName).apply();
    }

    public static String getArtistName() {
        return sharedPreferences.getString("artistName", null);
    }

//    public static String getMusicUrl() {
//        return musicUrl;
//    }
//
//    public static void setMusicUrl(String musicUrl) {
//        MusicHolder.musicUrl = musicUrl;
//    }

    public static void setMusicUrl(String musicUrl) {
        sharedPreferences.edit().putString("musicUrl", musicUrl).apply();
    }

    public static String getMusicUrl() {
        return sharedPreferences.getString("musicUrl", null);
    }

//    public static String getPlayListId() {
//        return playListId;
//    }
//
//    public static void setPlayListId(String playListId) {
//        MusicHolder.playListId = playListId;
//    }

    public static String getPlayListId() {
        return sharedPreferences.getString("playListId",null);
    }

    public static void setPlayListId(String playListId) {
        sharedPreferences.edit().putString("playListId",playListId).apply();
    }

//    public static String getAlbumArtUrl() {
//        return albumArtUrl;
//    }
//
//    public static void setAlbumArtUrl(String albumArtUrl) {
//        MusicHolder.albumArtUrl = albumArtUrl;
//    }

    public static String getAlbumArtUrl() {
        return sharedPreferences.getString("albumUrl",null);
    }

    public static void setAlbumArtUrl(String albumArtUrl) {
        sharedPreferences.edit().putString("albumUrl",albumArtUrl).apply();
    }

//    public static String getLyrics() {
//        return Lyrics;
//    }
//
//    public static void setLyrics(String lyrics) {
//        Lyrics = lyrics;
//    }

    public static String getLyrics() {
        return sharedPreferences.getString("lyrics",null);
    }

    public static void setLyrics(String lyrics) {
        sharedPreferences.edit().putString("lyrics",lyrics).apply();
    }

//    public static boolean isPlayingMode() {
//        return playingMode;
//    }
//
//    public static void setPlayingMode(boolean playingMode) {
//        MusicHolder.playingMode = playingMode;
//    }

    public static void setPlayingMode(int playingMode) {
        sharedPreferences.edit().putInt("playingMode", playingMode).apply();
    }

    public static int getPlayingMode() {
        return sharedPreferences.getInt("playingMode", 0);
    }


//    public static String getSearchResult() {
//        return SearchResult;
//    }
//
//    public static void setSearchResult(String searchResult) {
//        SearchResult = searchResult;
//    }

    public static String getSearchResult() {
       return sharedPreferences.getString("searchResult",null);
    }

    public static void setSearchResult(String searchResult) {
        sharedPreferences.edit().putString("searchResult",searchResult).apply();
    }

    public static boolean isIsSearchMode() {
        return isSearchMode;
    }

    public static void setIsSearchMode(boolean isSearchMode) {
        MusicHolder.isSearchMode = isSearchMode;
    }

//    public static int getPosition() {
//        return position;
//    }
//
//    public static void setPosition(int position) {
//        MusicHolder.position = position;
//    }

    public static void setPosition(int position) {
        sharedPreferences.edit().putInt("position", position).apply();
    }

    public static int getPosition() {
        return sharedPreferences.getInt("position", 0);
    }

//    public static String getMusicId() {
//        return musicId;
//    }
//
//    public static void setMusicId(String musicId) {
//        MusicHolder.musicId = musicId;
//    }

    public static String getMusicId() {
        return sharedPreferences.getString("musicId",null);
    }

    public static void setMusicId(String musicId) {
        sharedPreferences.edit().putString("musicId",musicId).apply();
    }

//    public static String getPlayListName() {
//        return playListName;
//    }
//
//    public static void setPlayListName(String playListName) {
//        MusicHolder.playListName = playListName;
//    }
//
//    public static boolean isPlayerMode() {
//        return playerMode;
//    }
//
//    public static void setPlayerMode(boolean playerMode) {
//        MusicHolder.playerMode = playerMode;
//    }
//
//    public static String getAlbumColor1() {
//        return AlbumColor1;
//    }
//
//    public static void setAlbumColor1(String albumColor1) {
//        AlbumColor1 = albumColor1;
//    }
//
//    public static String getAlbumColor2() {
//        return AlbumColor2;
//    }
//
//    public static void setAlbumColor2(String albumColor2) {
//        AlbumColor2 = albumColor2;
//    }

    public static String getPlayListName() {
        return sharedPreferences.getString("playListName", null);
    }

    public static void setPlayListName(String playListName) {
        sharedPreferences.edit().putString("playListName", playListName).apply();
    }

    public static boolean isPlayerMode() {
        return sharedPreferences.getBoolean("playerMode", false); // 默认为 false
    }

    public static void setPlayerMode(boolean playerMode) {
        sharedPreferences.edit().putBoolean("playerMode", playerMode).apply();
    }

    public static String getAlbumColor1() {
        return sharedPreferences.getString("albumColor1", "#FFFFFF"); // 默认为白色
    }

    public static void setAlbumColor1(String albumColor1) {
        sharedPreferences.edit().putString("albumColor1", albumColor1).apply();
    }

    public static String getAlbumColor2() {
        return sharedPreferences.getString("albumColor2", "#000000"); // 默认为黑色
    }

    public static void setAlbumColor2(String albumColor2) {
        sharedPreferences.edit().putString("albumColor2", albumColor2).apply();
    }

}

