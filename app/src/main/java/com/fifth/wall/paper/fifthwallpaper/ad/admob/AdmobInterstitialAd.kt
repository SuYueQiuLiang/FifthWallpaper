package com.fifth.wall.paper.fifthwallpaper.ad.admob

import android.app.Activity
import android.content.Context
import com.fifth.wall.paper.fifthwallpaper.ad.AdLoaderManager
import com.fifth.wall.paper.fifthwallpaper.ad.base.AdLoaderCallbacks
import com.fifth.wall.paper.fifthwallpaper.ad.base.AdmobFullScreenAdLoader
import com.fifth.wall.paper.fifthwallpaper.ad.base.FullScreenAdCallbacks
import com.fifth.wall.paper.fifthwallpaper.utils.UploadUtil
import com.fifth.wall.paper.fifthwallpaper.utils.log
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock


class AdmobInterstitialAd(adUnitId: String, timeoutLimit: Int,adPositionId : String) : AdmobFullScreenAdLoader<InterstitialAd>(adUnitId, timeoutLimit,adPositionId) {
    override fun loadAd(context: Context, adLoaderCallbacks: AdLoaderCallbacks) {
        scope.launch {
            mutex.withLock {
                if (isLoading || isAdReady() || AdLoaderManager.isOvertimes())
                    return@launch
                log("$adUnitId start load ad")
                isLoading = true
                InterstitialAd.load(context, adUnitId, adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            ad = interstitialAd
                            isLoading = false
                            loadAdTime = System.currentTimeMillis()
                            adLoaderCallbacks.onAdLoaded()
                            log("$adUnitId loaded ad")

                            interstitialAd.onPaidEventListener = OnPaidEventListener{ adValue ->
                                val loadedAdapterResponseInfo = interstitialAd.responseInfo.loadedAdapterResponseInfo
                                UploadUtil.ad(adValue.valueMicros,adValue.currencyCode,loadedAdapterResponseInfo?.adSourceName ?:"",
                                    "admob",adUnitId,adPositionId,"Inter")
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            isLoading = false
                            adLoaderCallbacks.onAdFailedToLoad()
                            log("$adUnitId load ad failed")
                        }
                    })
            }
        }
    }

    override fun showAd(activity: Activity, callbacks: FullScreenAdCallbacks) {
        if (isAdReady()) {
            log("$adUnitId start show ad")
            ad!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    callbacks.onAdDismissed()
                }

                override fun onAdClicked() {
                    callbacks.onAdClicked()
                    AdLoaderManager.onAdClicked()
                }

                override fun onAdShowedFullScreenContent() {
                    callbacks.onAdShowed()
                    AdLoaderManager.onAdShowed()
                    UploadUtil.upload("sw_ad_impression", mapOf("ad_pos_id" to adPositionId))
                }
            }
            ad!!.show(activity)
            ad = null
        } else callbacks.onAdDismissed()
    }

    override var ad: InterstitialAd? = null

}