package com.fifth.wall.paper.fifthwallpaper.ad.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex

abstract class AdLoader<T> {
    protected val scope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    protected val mutex by lazy { Mutex() }
    abstract val ad : T?
}

