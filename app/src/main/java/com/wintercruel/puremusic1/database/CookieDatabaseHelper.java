package com.wintercruel.puremusic1.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CookieDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cookie.db"; // 数据库名称
    private static final int DATABASE_VERSION = 1; // 数据库版本

    private static final String TABLE_COOKIES = "cookies"; // Cookies 表的名称

    // Cookies 表的列
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_HOST = "host";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_DOMAIN = "domain";
    private static final String COLUMN_PATH = "path";
    private static final String COLUMN_EXPIRES_AT = "expires_at";
    private static final String COLUMN_SECURE = "secure";
    private static final String COLUMN_HTTP_ONLY = "http_only";

    public CookieDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建 cookies 表
        String createTableQuery = "CREATE TABLE " + TABLE_COOKIES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HOST + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_VALUE + " TEXT, " +
                COLUMN_DOMAIN + " TEXT, " +
                COLUMN_PATH + " TEXT, " +
                COLUMN_EXPIRES_AT + " INTEGER, " +
                COLUMN_SECURE + " INTEGER, " +
                COLUMN_HTTP_ONLY + " INTEGER);";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果数据库版本升级，删除旧的表并创建新表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COOKIES);
        onCreate(db);
    }
}
