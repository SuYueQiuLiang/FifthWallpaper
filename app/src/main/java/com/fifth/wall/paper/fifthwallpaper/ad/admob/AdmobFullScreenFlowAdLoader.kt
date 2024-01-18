package com.fifth.wall.paper.fifthwallpaper.ad.admob

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Lifecycle
import com.fifth.wall.paper.fifthwallpaper.ad.AdLoaderManager
import com.fifth.wall.paper.fifthwallpaper.ad.base.AdLoaderCallbacks
import com.fifth.wall.paper.fifthwallpaper.ad.base.AdmobFullScreenAdLoader
import com.fifth.wall.paper.fifthwallpaper.ad.base.FullScreenAdCallbacks
import com.fifth.wall.paper.fifthwallpaper.bean.AdConfig
import com.fifth.wall.paper.fifthwallpaper.bean.App
import com.fifth.wall.paper.fifthwallpaper.ui.ad.LoadActivity
import com.fifth.wall.paper.fifthwallpaper.utils.UploadUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class AdmobFullScreenFlowAdLoader(private val adPositionId : String) {
    private val appOpenAdLoaders = arrayListOf<AdmobFullScreenAdLoader<*>>()
    private var lifecycle: Lifecycle? = null
    open fun loadAd(lifecycle: Lifecycle?) {
        if (isAdReady() || isLoading() || AdLoaderManager.isOvertimes())
            return
        this.lifecycle = lifecycle
        loadAd(0)
    }

    private fun loadAd(position: Int) {
        if (position >= appOpenAdLoaders.size)
            return
        appOpenAdLoaders[position].loadAd(App.getAppContext(), object : AdLoaderCallbacks {
            override fun onAdLoaded() = Unit
            override fun onAdFailedToLoad() {
                if (lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    var next = position + 1
                    if (next >= appOpenAdLoaders.size)
                        next = 0
                    loadAd(next)
                } else {
                    loadAd(position + 1)
                }
            }
        })
    }

    fun isAdReady(): Boolean {
        appOpenAdLoaders.forEach { if (it.isAdReady()) return true }
        return false
    }

    fun isLoading(): Boolean {
        appOpenAdLoaders.forEach { if (it.isLoading) return true }
        return false
    }

    fun initFlow(list: List<AdConfig.AdmobSource>) {
        appOpenAdLoaders.apply {
            clear()
            for (source in list) {
                val loader = when (source.ayikn) {
                    "op" -> AdmobOpenAd(source.aubbp, source.ayyb,adPositionId)
                    "int" -> AdmobInterstitialAd(source.aubbp, source.ayyb,adPositionId)
                    else -> continue
                }
                add(loader)
            }
        }
    }

    open fun showAd(activity: Activity, callbacks: FullScreenAdCallbacks) {
        UploadUtil.upload("sw_ad_chance", mapOf("ad_pos_id" to adPositionId))
        if(AdLoaderManager.isOvertimes()){
            callbacks.onAdDismissed()
            return
        }
        getReadyAd()?.apply {
            MainScope().launch {
                activity.startActivity(Intent(activity, LoadActivity::class.java))
                delay(500)
                this@apply.showAd(activity, callbacks)
                loadAd(null)
            }
        } ?: callbacks.onAdDismissed()
    }

    private fun getReadyAd() = appOpenAdLoaders.firstOrNull { it.isAdReady() }
}
