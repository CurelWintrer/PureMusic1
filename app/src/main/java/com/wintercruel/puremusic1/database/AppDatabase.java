package com.wintercruel.puremusic1.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.wintercruel.puremusic1.dao.MusicItemDao;
import com.wintercruel.puremusic1.entity.MusicItems;

@Database(entities = {MusicItems.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MusicItemDao musicItemDao();
}
