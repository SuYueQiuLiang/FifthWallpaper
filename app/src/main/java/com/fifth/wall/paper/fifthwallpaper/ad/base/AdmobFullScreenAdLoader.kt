package com.fifth.wall.paper.fifthwallpaper.ad.base

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest


abstract class AdmobFullScreenAdLoader<T>(val adUnitId: String, private val timeoutLimit: Int,val adPositionId : String) : AdLoader<T>() {
    var isLoading = false
    protected var isShowing = false
    protected var loadAdTime: Long = 0

    protected val adRequest get() = AdRequest.Builder().build()

    abstract fun loadAd(context: Context, adLoaderCallbacks: AdLoaderCallbacks)

    abstract fun showAd(activity: Activity,callbacks : FullScreenAdCallbacks)

    fun isAdReady(): Boolean {
        val ad = ad
        return (ad != null) && (timeoutLimit * 1000 + loadAdTime > System.currentTimeMillis())
    }
}

interface AdLoaderCallbacks {
    fun onAdLoaded()

    fun onAdFailedToLoad()
}

interface FullScreenAdCallbacks{
    fun onAdShowed(){}
    fun onAdDismissed(){}
    fun onAdClicked(){}
}