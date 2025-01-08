package com.wintercruel.puremusic1.database;

import android.content.Context;

import androidx.media3.common.C;
import androidx.room.Room;

public class DatabaseClient {
    private Context mCtx;
    private static DatabaseClient mInstance;

    private AppDatabase appDatabase;
    private DatabaseClient(Context mCtx){
        this.mCtx=mCtx;
        appDatabase= Room.databaseBuilder(mCtx,AppDatabase.class,"MyMusic").build();

    }

    public static synchronized DatabaseClient getInstance(Context mCtx){
        if(mInstance==null){
            mInstance=new DatabaseClient(mCtx);
        }
        return mInstance;
    }
    public AppDatabase getAppDatabase(){
        return appDatabase;
    }

}
