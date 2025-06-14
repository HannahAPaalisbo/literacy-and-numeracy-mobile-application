@file:Suppress("DEPRECATION")
package com.example.tagakaulolearningapp

import android.app.Application
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache

class TagakauloLearningApp : Application() {
    lateinit var simpleCache: SimpleCache

    override fun onCreate() {
        super.onCreate()
        val exoPlayerCacheSize = 90 * 1024 * 1024
        simpleCache = SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize.toLong()), ExoDatabaseProvider(this))
    }
}
