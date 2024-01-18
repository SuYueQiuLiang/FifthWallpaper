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
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

class AdmobOpenAd(adUnitId: String, timeoutLimit: Int, adPositionId: String) :
    AdmobFullScreenAdLoader<AppOpenAd>(adUnitId, timeoutLimit, adPositionId) {
    override fun loadAd(context: Context, adLoaderCallbacks: AdLoaderCallbacks) {
        scope.launch {
            mutex.withLock {
                if (isLoading || isAdReady() || AdLoaderManager.isOvertimes())
                    return@launch
                log("$adUnitId start load ad")
                isLoading = true
                AppOpenAd.load(context, adUnitId, adRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    object : AppOpenAdLoadCallback() {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            this@AdmobOpenAd.ad = ad
                            isLoading = false
                            loadAdTime = System.currentTimeMillis()
                            adLoaderCallbacks.onAdLoaded()
                            log("$adUnitId loaded ad")


                            ad.onPaidEventListener = OnPaidEventListener { adValue ->
                                val loadedAdapterResponseInfo = ad.responseInfo.loadedAdapterResponseInfo
                                UploadUtil.ad(
                                    adValue.valueMicros, adValue.currencyCode, loadedAdapterResponseInfo?.adSourceName ?: "",
                                    "admob", adUnitId, adPositionId, "Op"
                                )
                            }
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
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

    override var ad: AppOpenAd? = null

}