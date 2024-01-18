package com.fifth.wall.paper.fifthwallpaper.ad.admob

import android.app.Activity
import androidx.lifecycle.Lifecycle
import com.fifth.wall.paper.fifthwallpaper.ad.base.FullScreenAdCallbacks
import com.fifth.wall.paper.fifthwallpaper.bean.isBlacklist
import com.fifth.wall.paper.fifthwallpaper.utils.InstallReferrerUtil.referrer
import com.fifth.wall.paper.fifthwallpaper.utils.UploadUtil

class AdmobFullScreenControlFlowAdLoader(adPositionId : String) : AdmobFullScreenFlowAdLoader(adPositionId){
    override fun loadAd(lifecycle: Lifecycle?) {
        if(referrer.isBlacklist())
            return
        super.loadAd(lifecycle)
    }

    override fun showAd(activity: Activity, callbacks: FullScreenAdCallbacks) {
        if(referrer.isBlacklist()) {
            callbacks.onAdDismissed()
            return
        }
        super.showAd(activity, callbacks)
    }
}