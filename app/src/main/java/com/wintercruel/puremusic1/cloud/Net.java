package com.wintercruel.puremusic1.cloud;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wintercruel.puremusic1.Index;
import com.wintercruel.puremusic1.MainActivity;
import com.wintercruel.puremusic1.database.MusicDatabase;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.entity.User;
import com.wintercruel.puremusic1.event_bus.LoginSuccessUpdateUI;
import com.wintercruel.puremusic1.items.MusicItem;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Net {

    public static OkHttpClient client;
    private static final String USER_PREFS = "User"; // 用户信息的 SharedPreferences
    private static final String COOKIE_PREFS = "Cookies_Prefs";
    private static final String COOKIE_KEY = "cookie";
    private static SharedPreferences cookiePrefs;
    //private static final String REAL_IP="&realIP=116.25.146.177";
    public static final String REAL_IP="";
    private static MusicDatabase musicDatabase;

    // 创建一个CookieJar来管理和持久化Cookies
//    private static final CookieJar cookieJar = new CookieJar() {
//        private final Map<String, List<Cookie>> cookieStore = new HashMap<>();
//
//        @Override
//        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//            String host = url.host();
//            cookieStore.put(host, new ArrayList<>(cookies));
//            saveCookiesToSharedPreferences(host, cookies);
//        }
//
//        @Override
//        public List<Cookie> loadForRequest(HttpUrl url) {
//            String host = url.host();
//            List<Cookie> cookies = cookieStore.get(host);
//            if (cookies == null || cookies.isEmpty()) {
//                cookies = loadCookiesFromSharedPreferences(host);
//                cookieStore.put(host, cookies);
//            }
//            return cookies != null ? cookies : new ArrayList<>();
//        }
//    };
    private static final CookieJar cookieJar = new CookieJar() {
        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            String host = url.host();
            // 使用 CookieManager 存储 Cookies
            CookieManager.saveCookiesToDatabase(host, cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            String host = url.host();
            // 使用 CookieManager 加载 Cookies
            return CookieManager.loadCookiesFromDatabase(host);
        }
    };


    // 初始化方法，传入Context来获取SharedPreferences
//    public static void initialize(Context context) {
//        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE);
//        client = new OkHttpClient.Builder()
//                .cookieJar(cookieJar)
//                .build();
//    }
    public static void initialize(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE);
        CookieManager.initialize(context);  // 初始化 SQLite 数据库
        client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
    }


    // 将Cookie保存到SharedPreferences
    private static synchronized void saveCookiesToSharedPreferences(String host, List<Cookie> cookies) {
        SharedPreferences.Editor editor = cookiePrefs.edit();
        Set<String> cookieSet = new HashSet<>();
        for (Cookie cookie : cookies) {
            cookieSet.add(serializeCookie(cookie));
        }
        editor.putStringSet(COOKIE_KEY + "_" + host, cookieSet);
        editor.commit();
    }

    // 从SharedPreferences加载Cookies
    private static synchronized List<Cookie> loadCookiesFromSharedPreferences(String host) {
        Set<String> cookieSet = cookiePrefs.getStringSet(COOKIE_KEY + "_" + host, new HashSet<>());
        List<Cookie> cookies = new ArrayList<>();
        for (String cookieString : cookieSet) {
            Cookie cookie = deserializeCookie(cookieString);
            if (cookie != null) {
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    // 序列化Cookie对象为String
    private static String serializeCookie(Cookie cookie) {
        return cookie.name() + "=" + cookie.value() + ";" +
                "domain=" + cookie.domain() + ";" +
                "path=" + cookie.path() + ";" +
                (cookie.expiresAt() != Long.MAX_VALUE ? "expiresAt=" + cookie.expiresAt() + ";" : "") +
                (cookie.secure() ? "secure;" : "") +
                (cookie.httpOnly() ? "httponly;" : "");
    }

    // 反序列化字符串为Cookie对象
    private static Cookie deserializeCookie(String cookieString) {
        return Cookie.parse(HttpUrl.get(server.ADDRESS), cookieString);
    }

    public static void visitorLogin(Context context){
        String loginUrl=String.format(server.ADDRESS+"/register/anonimous?realIP="+REAL_IP);
        Request loginRequest=new Request.Builder()
                .url(loginUrl)
                .build();
        try(Response response=client.newCall(loginRequest).execute()){
            if(response.isSuccessful()){
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MusicHolder.setVisitorLogin(true);
                        Toast.makeText(context.getApplicationContext(), "登录成功", Toast.LENGTH_LONG).show();
                        ((Activity) context).finish();
                    }
                });

                String responseBody=response.body().string();
                System.out.println("登录结果："+responseBody);
            }else {
                System.out.println(response.body().string());
                System.out.println("Login failed: " + response.code());

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MusicHolder.setVisitorLogin(false);
                        Toast.makeText(context.getApplicationContext(), "登录失败", Toast.LENGTH_LONG).show();
                    }
                });
            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    // 登录方法，使用GET请求
    public static void login(String phoneNumber, String captcha,Context context) {
        // 构建登录请求
        String loginUrl = String.format(server.ADDRESS+"/login/cellphone?phone="+phoneNumber+"&captcha="+captcha+"&realIP="+REAL_IP);
        Request loginRequest = new Request.Builder()
                .url(loginUrl) // 这里使用GET请求
                .build();

        try (Response response = client.newCall(loginRequest).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Login successful: " + response.code());

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context.getApplicationContext(), "登录成功", Toast.LENGTH_LONG).show();
                        ((Activity) context).finish();
                    }
                });

                String responseBody=response.body().string();
                System.out.println("登录结果："+responseBody);
                User user= GetUser(responseBody);
                SaveUser(user,context);
                GetPlayList(context);
//                Index.isLogin =true;
                EventBus.getDefault().postSticky(new LoginSuccessUpdateUI());

            } else {
                System.out.println(response.body().string());
                System.out.println("Login failed: " + response.code());

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context.getApplicationContext(), "登录失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送验证码
    public static void sendCaptcha(String phoneNumber,Context context) {
        OkHttpClient client = new OkHttpClient();

        String url = server.ADDRESS+"/captcha/sent?phone=" + phoneNumber+"&realIP="+REAL_IP;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // Handle failure here
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ((Activity)context).runOnUiThread(()->{
                        Toast.makeText(context,"验证码已发送",Toast.LENGTH_LONG).show();
                    });

                    // Handle success here (e.g., notify the user that the SMS has been sent)
                } else {
                    ((Activity)context).runOnUiThread(()->{
                        Toast.makeText(context,"验证码发送失败",Toast.LENGTH_LONG).show();
                    });

                    // Handle error here
                }
            }
        });
    }

    //解析登录时返回的信息
    public static User GetUser(String userDetail) throws JSONException {
        User user = new User();
        JSONObject jsonObject = new JSONObject(userDetail);

        // 获取 account 对象，并从中提取 id 和其他信息
        JSONObject accountObject = jsonObject.getJSONObject("account");
        user.setUserId(accountObject.getString("id"));
        user.setUserName(accountObject.getString("userName"));
        user.setVipType(accountObject.getInt("vipType"));
        // 获取 token
//        user.setToken(jsonObject.getString("token"));
//        user.setCookie(jsonObject.getString("cookie"));
        // 获取 profile 对象，并从中提取 nickname, avatarUrl, backgroundUrl
        JSONObject profileObject = jsonObject.getJSONObject("profile");
        user.setNickname(profileObject.getString("nickname"));
        user.setAvatarUrl(profileObject.getString("avatarUrl"));
        user.setBackgroundUrl(profileObject.getString("backgroundUrl"));
        return user;
    }

    //将用户信息保存到文件
    public static void SaveUser(User user,Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("User", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", user.getUserId());
        editor.putString("token", user.getToken());
        editor.putString("nickname", user.getNickname());
        editor.putString("userName", user.getUserName());
        editor.putInt("vipType", user.getVipType());
        editor.putString("avatarUrl", user.getAvatarUrl());
        editor.putString("backgroundUrl", user.getBackgroundUrl());
        editor.putString("cookie",user.getCookie());
        editor.apply();
    }

    //获取用户歌单，保存到文件
    // 获取用户歌单，保存到文件
    public static void GetPlayList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString("userId", null);

        Request PlayListRequest = new Request.Builder()
                .url(server.ADDRESS + "/user/playlist?uid=" + uid+"&realIP="+REAL_IP)
                .build();

        try (Response response = client.newCall(PlayListRequest).execute()) {
            if (response.isSuccessful()) {
                String responseDate = response.body().string();
                System.out.println(responseDate);

                JSONObject jsonObject = new JSONObject(responseDate);
                JSONArray playlistArray = jsonObject.getJSONArray("playlist");
                String playListId, coverImgUrl, name, trackCount;

                for (int i = 0; i < playlistArray.length(); i++) {
                    JSONObject playlistObject = playlistArray.getJSONObject(i);
                    playListId = playlistObject.getString("id");
                    coverImgUrl = playlistObject.getString("coverImgUrl");
                    name = playlistObject.getString("name");


                    System.out.println("ID: " + playListId);
                    System.out.println("Cover Image URL: " + coverImgUrl);
                    System.out.println("Name: " + name);
                    System.out.println("-------------------------");

                    GetPlayListMusic(playListId, context);
                }

                SharedPreferences sharedPreferences_edit = context.getSharedPreferences("playList", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences_edit.edit();
                editor.putString("playList", responseDate);
                editor.apply();
            } else {
                Log.d("歌单扫描：", "失败");
                System.out.println(response.body().string());
                System.out.println("获取歌单失败: " + response.code());
            }
        } catch (IOException e) {
            // 使用 Handler 在主线程显示 Toast
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "服务器连接失败", Toast.LENGTH_LONG).show()
            );
        } catch (JSONException e) {
            Log.d("出错：", String.valueOf(e));
        }
    }

//    public static void GetPlayListMusic(String playListId, Context context) {
//        Request dataRequest = new Request.Builder()
//                .url(server.ADDRESS + "/playlist/track/all?id=" + playListId+"&realIP="+REAL_IP)
//                .build();
//
//        client.newCall(dataRequest).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.d("歌单音乐获取：", "请求失败");
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String PlayListDate = response.body().string();
//                    Log.d("歌单音乐获取：", "成功");
//                    System.out.println(PlayListDate);
//
//                    // 解析音乐信息，保存到数据库
//                    MusicDatabase musicDatabase = new MusicDatabase(context.getApplicationContext());
//                    String tableName = "playlist_" + playListId;
//
//                    // 检查表是否存在，表不存在时才创建
//                    SQLiteDatabase db = musicDatabase.getWritableDatabase();
//                    String checkTableExistsQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
//                    try (Cursor cursor = db.rawQuery(checkTableExistsQuery, null)) {
//                        if (cursor.getCount() == 0) {
//                            String CREATE_TABLE_MUSIC = "CREATE TABLE " + tableName + " (" +
//                                    "id TEXT PRIMARY KEY," +
//                                    "title TEXT," +
//                                    "artist TEXT," +
//                                    "albumUrl TEXT)";
//                            db.execSQL(CREATE_TABLE_MUSIC);
//                        }
//                    }
//
//                    try {
//                        JSONObject jsonObject = new JSONObject(PlayListDate);
//                        JSONArray musicArray = jsonObject.getJSONArray("songs");
//
//                        for (int i = 0; i < musicArray.length(); i++) {
//                            JSONObject musicData = musicArray.getJSONObject(i);
//                            String name = musicData.getString("name");
//                            String id = musicData.getString("id");
//
//                            // 获取专辑信息
//                            JSONObject albumObject = musicData.getJSONObject("al");
//                            String albumPicUrl = albumObject.getString("picUrl");
//
//                            JSONArray arArray = musicData.getJSONArray("ar");
//                            StringBuilder artistNames = new StringBuilder();
//
//                            for (int j = 0; j < arArray.length(); j++) {
//                                JSONObject artistObject = arArray.getJSONObject(j);
//                                if (j > 0) artistNames.append("/");  // 用斜杠隔开
//                                artistNames.append(artistObject.getString("name"));
//                            }
//
//                            // 插入音乐数据
//                            musicDatabase.InsertMusic(tableName, id, name, artistNames.toString(), albumPicUrl);
//                        }
//
//                    } catch (JSONException e) {
//                        Log.e("解析错误", "JSON解析出错", e);
//                    }
//                } else {
//                    Log.d("歌单音乐获取：", "请求失败, 状态码：" + response.code());
//                }
//            }
//        });
//    }

    public static void GetPlayListMusic(String playListId, Context context) {

        Request dataRequest = new Request.Builder()
                .url(server.ADDRESS + "/playlist/track/all?id=" + playListId + "&realIP=" + REAL_IP)
                .build();

        client.newCall(dataRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("歌单音乐获取：", "请求失败");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String PlayListDate = response.body().string();
                    Log.d("歌单音乐获取：", "成功");
                    System.out.println(PlayListDate);

                    // 更新音乐数据
                    updateMusicTable(playListId, PlayListDate, context);

                } else {
                    Log.d("歌单音乐获取：", "请求失败, 状态码：" + response.code());
                }
            }
        });
    }

    public static void updateMusicTable(String playListId, String playlistData, Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取数据库实例
                MusicDatabase musicDatabase = new MusicDatabase(context.getApplicationContext());
                String tableName = "playlist_" + playListId;
                SQLiteDatabase db = musicDatabase.getWritableDatabase();

                // 检查表是否存在
                if (!isTableExist(db, tableName)) {
                    // 如果表不存在，创建表
                    createTable(db, tableName);
                } else {
                    // 清理旧数据
                    db.delete(tableName, null, null);  // 删除该歌单表中的所有数据
                }

                // 解析新数据并插入
                try {
                    JSONObject jsonObject = new JSONObject(playlistData);
                    JSONArray musicArray = jsonObject.getJSONArray("songs");

                    for (int i = 0; i < musicArray.length(); i++) {
                        JSONObject musicData = musicArray.getJSONObject(i);
                        String name = musicData.getString("name");
                        String id = musicData.getString("id");

                        // 获取专辑信息
                        JSONObject albumObject = musicData.getJSONObject("al");
                        String albumPicUrl = albumObject.getString("picUrl");

                        JSONArray arArray = musicData.getJSONArray("ar");
                        StringBuilder artistNames = new StringBuilder();

                        for (int j = 0; j < arArray.length(); j++) {
                            JSONObject artistObject = arArray.getJSONObject(j);
                            if (j > 0) artistNames.append("/");  // 用斜杠隔开
                            artistNames.append(artistObject.getString("name"));
                        }

                        // 插入音乐数据
                        ContentValues values = new ContentValues();
                        values.put("id", id);
                        values.put("title", name);
                        values.put("artist", artistNames.toString());
                        values.put("albumUrl", albumPicUrl);

                        // 使用insertWithOnConflict来避免重复数据，REPLACE表示重复时替换
                        db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }

                } catch (JSONException e) {
                    Log.e("解析错误", "JSON解析出错", e);
                }
            }
        }).start();
    }

    // 检查表是否存在
    public static boolean isTableExist(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?;", new String[]{tableName});
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // 创建表
    public static void createTable(SQLiteDatabase db, String tableName) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT, " +
                "artist TEXT, " +
                "albumUrl TEXT);";
        db.execSQL(createTableQuery);
    }



    private static void SaveRecommendPlayListMusic(Context context, String responseDate) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MusicDatabase musicDatabase = new MusicDatabase(context.getApplicationContext());
                String tableName = "playlist_recommend";
                SQLiteDatabase db = musicDatabase.getWritableDatabase();

                // 检查表是否存在，表不存在时才创建
                if (!isTableExist(db, tableName)) {
                    // 如果表不存在，创建表
                    createTable(db, tableName);
                } else {
                    // 清理旧数据
                    db.delete(tableName, null, null);  // 删除该歌单表中的所有数据
                }

                try {
                    JSONObject jsonObject = new JSONObject(responseDate);
                    JSONArray musicArray = jsonObject.getJSONArray("recommend");

                    for (int i = 0; i < musicArray.length(); i++) {
                        JSONObject musicData = musicArray.getJSONObject(i);
                        String name = musicData.getString("name");
                        String id = musicData.getString("id");

                        // 获取专辑信息
                        JSONObject albumObject = musicData.getJSONObject("al");
                        String albumPicUrl = albumObject.getString("picUrl");

                        JSONArray arArray = musicData.getJSONArray("ar");
                        StringBuilder artistNames = new StringBuilder();

                        for (int j = 0; j < arArray.length(); j++) {
                            JSONObject artistObject = arArray.getJSONObject(j);
                            if (j > 0) artistNames.append("/");  // 用斜杠隔开
                            artistNames.append(artistObject.getString("name"));
                        }

                        // 插入音乐数据
                        ContentValues values = new ContentValues();
                        values.put("id", id);
                        values.put("title", name);
                        values.put("artist", artistNames.toString());
                        values.put("albumUrl", albumPicUrl);

                        // 使用insertWithOnConflict来避免重复数据，REPLACE表示重复时替换
                        long result = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);

                        if (result == -1) {
                            Log.e("数据库操作", "插入失败: " + id + ", " + name);
                        } else {
                            Log.d("数据库操作", "插入成功: " + id + ", " + name);
                        }
                    }

                } catch (JSONException e) {
                    Log.e("JSON解析错误", "解析数据失败", e);
                } finally {
                    db.close();
                }


            }
        }).start();
    }

    public interface LyricsCallback {
        void onSuccess(String lyrics);
        void onFailure(Exception e);
    }

    public static void GetLyrics(Context context, String id, LyricsCallback callback) {
        // 创建一个不带 CookieJar 的 OkHttpClient 实例
        OkHttpClient clientWithoutCookies = new OkHttpClient.Builder().build();

        Request dataRequest = new Request.Builder()
                .url(server.ADDRESS + "/lyric?id=" + id+"&realIP="+REAL_IP)
                .build();

        clientWithoutCookies.newCall(dataRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 网络请求失败，返回错误信息
                System.out.println("歌词获取失败"+e.getMessage());
//                Toast.makeText(context, "歌词获取失败", Toast.LENGTH_LONG).show();
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("lrc");
                        String lyrics = jsonObject1.getString("lyric");

                        Log.d("Net", "获取到歌词: " + lyrics);

                        // 成功获取歌词，通过回调返回
                        callback.onSuccess(lyrics);
                    } catch (JSONException e) {
                        // JSON 解析失败

                        callback.onFailure(e);
                    }
                } else {
                    // 请求失败
//                    Toast.makeText(context, "歌词获取失败", Toast.LENGTH_LONG).show();
                    callback.onFailure(new IOException("请求失败，响应码：" + response.code()));
                }
            }
        });
    }

    public static String SearchMusic(Context context,String SearchText){
        String responseDate=null;

        // 创建一个不带CookieJar的OkHttpClient实例
        OkHttpClient clientWithoutCookies = new OkHttpClient.Builder()
                .build();

        Request dataRequest=new Request.Builder()
                .url(server.ADDRESS+"/cloudsearch?keywords="+SearchText+"&type=1")
                .build();

        try(Response response=clientWithoutCookies.newCall(dataRequest).execute()){
            if(response.isSuccessful()){
                responseDate=response.body().string();
                System.out.println("音乐搜索结果"+responseDate);


            }

        } catch (IOException e) {

            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context.getApplicationContext(),"搜索出错请检查网络连接", Toast.LENGTH_LONG).show();
                }
            });

        }

        return responseDate;
    }

    // 退出登录方法
    public static void logout(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String logoutUrl = server.ADDRESS+"/logout"; // 退出登录的URL
                Request logoutRequest = new Request.Builder()
                        .url(logoutUrl)
                        .build();

                try (Response response = client.newCall(logoutRequest).execute()) {
                    if (response.isSuccessful()) {
                        System.out.println("Logout successful");

                        // 在主线程中更新UI
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                clearCookies();
                                clearUserInfo(context);
                                clearPlayList(context);
                                Toast.makeText(context, "成功退出登录", Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().postSticky(new LoginSuccessUpdateUI());
                            }
                        });
                    } else {
                        System.out.println("Logout failed");

                        // 在主线程中更新UI
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "退出登录失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    // 在主线程中更新UI
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "网络错误，退出登录失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    // 清除Cookies
    private static void clearCookies() {
        SharedPreferences.Editor editor = cookiePrefs.edit();
        editor.remove(COOKIE_KEY);
        editor.apply();
    }

    // 清除用户信息
    private static void clearUserInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // 清除所有用户信息
        editor.apply();
    }

    private static void clearPlayList(Context context){
        SharedPreferences sharedPreferences=context.getSharedPreferences("playList",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }



}
