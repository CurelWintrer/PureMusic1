package com.wintercruel.puremusic1.musicCache;

import android.content.Context;

import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import java.io.File;

@UnstableApi public class MusicCache {
    // 设置缓存大小，比如 100MB
    private static final long CACHE_SIZE = 2000 * 1024 * 1024; // 2Gb
    private static SimpleCache simpleCache;

    public static SimpleCache getCache(Context context) {
        if (simpleCache == null) {
            // 创建一个最少最近使用（LRU）缓存驱逐策略
            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(CACHE_SIZE);
            simpleCache = new SimpleCache(new File(context.getCacheDir(), "media"), evictor);
        }
        return simpleCache;
    }

    public DataSource.Factory buildDataSourceFactory(Context context, SimpleCache cache) {
        // 一个默认的数据源工厂，负责从网络获取数据
        DefaultDataSource.Factory defaultDataSourceFactory = new DefaultDataSource.Factory(context);

        // 创建缓存数据源工厂
        return new CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(defaultDataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR); // 忽略缓存错误
    }

    public ExoPlayer preparePlayer(Context context, String url) {
        // 获取缓存实例
        SimpleCache cache = getCache(context);

        // 创建数据源工厂
        DataSource.Factory dataSourceFactory = buildDataSourceFactory(context, cache);

        // 创建 ExoPlayer 实例
        ExoPlayer player = new ExoPlayer.Builder(context).build();

        // 创建 MediaSource
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(url));

        // 设置媒体资源并准备播放
        player.setMediaSource(mediaSource);
        player.prepare();

        return player;
    }

    public static void releaseCache() {
        if (simpleCache != null) {
            simpleCache.release();
            simpleCache = null;
        }
    }
}
