package com.wintercruel.puremusic1.cloud;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wintercruel.puremusic1.database.CookieDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;


public class CookieManager {
    private static CookieDatabaseHelper dbHelper;
    private static SQLiteDatabase db;

    // 初始化 SQLite 数据库
    public static void initialize(Context context) {
        dbHelper = new CookieDatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    // 保存 Cookie 到 SQLite 数据库
    public static void saveCookiesToDatabase(String host, List<Cookie> cookies) {
        db.beginTransaction();
        try {
            for (Cookie cookie : cookies) {
                ContentValues values = new ContentValues();
                values.put("host", host);
                values.put("name", cookie.name());
                values.put("value", cookie.value());
                values.put("domain", cookie.domain());
                values.put("path", cookie.path());
                values.put("expires_at", cookie.expiresAt());
                values.put("secure", cookie.secure() ? 1 : 0);
                values.put("http_only", cookie.httpOnly() ? 1 : 0);

                db.insert("cookies", null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // 从 SQLite 数据库加载 Cookie
    public static List<Cookie> loadCookiesFromDatabase(String host) {
        List<Cookie> cookies = new ArrayList<>();
        Cursor cursor = db.query(
                "cookies",
                null,
                "host = ?",
                new String[]{host},
                null, null, null
        );
        try {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range") String value = cursor.getString(cursor.getColumnIndex("value"));
                @SuppressLint("Range") String domain = cursor.getString(cursor.getColumnIndex("domain"));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex("path"));
                @SuppressLint("Range") long expiresAt = cursor.getLong(cursor.getColumnIndex("expires_at"));
                @SuppressLint("Range") boolean secure = cursor.getInt(cursor.getColumnIndex("secure")) == 1;
                @SuppressLint("Range") boolean httpOnly = cursor.getInt(cursor.getColumnIndex("http_only")) == 1;

                Cookie cookie = new Cookie.Builder()
                        .name(name)
                        .value(value)
                        .domain(domain)
                        .path(path)
                        .expiresAt(expiresAt)
                        .secure()
                        .httpOnly()
                        .build();
                cookies.add(cookie);
            }
        } finally {
            cursor.close();
        }
        return cookies;
    }
}
