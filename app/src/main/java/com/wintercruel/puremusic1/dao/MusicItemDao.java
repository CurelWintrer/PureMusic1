package com.wintercruel.puremusic1.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wintercruel.puremusic1.entity.MusicItem;
import com.wintercruel.puremusic1.entity.MusicItems;
import com.wintercruel.puremusic1.entity.User;

import java.util.List;

@Dao
public interface MusicItemDao {
    @Insert
    void insert(MusicItems musicItems);

    @Query("SELECT * FROM music")
    List<MusicItems>getAllMusicItems();

    @Delete
    void delete(MusicItems musicItems);

    @Update
    void update(MusicItems musicItems);

}
