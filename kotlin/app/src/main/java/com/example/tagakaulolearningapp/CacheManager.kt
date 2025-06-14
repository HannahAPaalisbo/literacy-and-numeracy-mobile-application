package com.example.tagakaulolearningapp

import android.content.Context
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

object CacheManager {
    private var simpleCache: SimpleCache? = null

    fun getSimpleCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "Videos")
            simpleCache = SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024))
        }
        return simpleCache!!
    }
}
