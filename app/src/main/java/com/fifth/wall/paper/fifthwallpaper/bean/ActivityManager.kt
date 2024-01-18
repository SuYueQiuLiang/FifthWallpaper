package com.fifth.wall.paper.fifthwallpaper.bean

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.fifth.wall.paper.fifthwallpaper.ad.AdLoaderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.WeakReference
import java.util.LinkedList

object ActivityManager : ActivityLifecycleCallbacks {
    private val activities = LinkedList<WeakReference<Activity>>()
    private val onCreatedActivities = LinkedList<WeakReference<Activity>>()
    private val scope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    private val mutex by lazy { Mutex() }
    private var bgJob : Job? = null
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        scope.launch {
            mutex.withLock {
                onCreatedActivities.add(WeakReference(activity))
            }
        }
    }

    override fun onActivityStarted(activity: Activity) {
        scope.launch {
            mutex.withLock {
                bgJob?.cancel()
                activities.add(WeakReference(activity))
                if(activities.size == 1){
                    AdLoaderManager.appOpenAd.loadAd(null)
                    AdLoaderManager.interstitialAd.loadAd(null)
                }
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        
    }

    override fun onActivityPaused(activity: Activity) {
        
    }

    override fun onActivityStopped(activity: Activity) {
        scope.launch {
            mutex.withLock {
                activities.removeIf { it.get() == activity }
                if(activities.size == 0)
                    bgJob = launch {
                        delay(3000)
                        finishAll(activity)
                    }
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        
    }

    override fun onActivityDestroyed(activity: Activity) {
        scope.launch {
            mutex.withLock {
                onCreatedActivities.removeIf { it.get() == activity }
            }
        }
    }

    private fun finishAll(activity: Activity) {
        onCreatedActivities.forEach { it.get()?.finish() }
    }
}