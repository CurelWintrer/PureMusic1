package com.wintercruel.puremusic1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MusicDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME="CloudMusic.db";
    private static final int DATABASE_VERSION=1;


    public MusicDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(CREATE_TABLE_MUSIC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void InsertMusic(String PlayListId, String id, String title, String artist, String albumUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(PlayListId, null, "id=?", new String[]{id}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return; // 如果记录存在，则返回
        }

        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("title", title);
        values.put("artist", artist);
        values.put("albumUrl", albumUrl);

        // 使用 db.insert() 将数据插入到数据库
        db.insert(PlayListId, null, values);

        // 关闭游标和数据库连接
        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }


    public Cursor getMusicPaginated(String PlayListId, int offset, int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableName = "playlist_" + PlayListId;

        return db.rawQuery("SELECT * FROM " + tableName + " LIMIT ? OFFSET ?",
                new String[]{String.valueOf(limit), String.valueOf(offset)});
    }


    public Cursor getPlaylistItem(String playListId, int i) {
        SQLiteDatabase db=this.getReadableDatabase();
        // 表名：playlist_<playListId>
        String tableName = "playlist_" + playListId;
        // 查询第 i 项（LIMIT 1 OFFSET i-1，因为 OFFSET 从 0 开始）
        String query = "SELECT * FROM " + tableName + " LIMIT 1 OFFSET ?";
        // 执行查询并返回结果
        return db.rawQuery(query, new String[] {String.valueOf(i - 1)});
    }


    public Cursor getAllMusic(String PlayListId){
        SQLiteDatabase db=this.getReadableDatabase();
        return db.query("playlist_"+PlayListId,null,null,null,null,null,null);
    }

    // 2. 获取数据库的总项数
    public int getMusicTotalCount(String PlayListId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM playlist_" + PlayListId, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

}
