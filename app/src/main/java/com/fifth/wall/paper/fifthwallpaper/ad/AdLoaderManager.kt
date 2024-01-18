package com.fifth.wall.paper.fifthwallpaper.ad

import com.fifth.wall.paper.fifthwallpaper.ad.admob.AdmobFullScreenControlFlowAdLoader
import com.fifth.wall.paper.fifthwallpaper.ad.admob.AdmobFullScreenFlowAdLoader
import com.fifth.wall.paper.fifthwallpaper.bean.AdConfig
import com.fifth.wall.paper.fifthwallpaper.bean.gson
import com.fifth.wall.paper.fifthwallpaper.utils.UploadUtil
import com.fifth.wall.paper.fifthwallpaper.utils.log
import com.google.android.gms.ads.OnPaidEventListener
import com.tencent.mmkv.MMKV
import java.util.Calendar

object AdLoaderManager {
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private lateinit var adConfig: AdConfig
    val appOpenAd = AdmobFullScreenFlowAdLoader("sw_launch")
    val interstitialAd = AdmobFullScreenControlFlowAdLoader("sw_detail_int")
    fun getBannerUnitId() = adConfig.sw_main_ban[0].aubbp
    private var currentShowTimes
        get() = mmkv.decodeInt("currentShowTimes")
        set(value) {
            mmkv.encode("currentShowTimes", value)
        }
    private var currentClickTimes
        get() = mmkv.decodeInt("currentClickTimes")
        set(value) {
            mmkv.encode("currentClickTimes", value)
        }
    private var lastDay
        get() = Calendar.getInstance().apply {
            timeInMillis = mmkv.decodeLong("currentDay")
        }
        set(value) {
            mmkv.encode("currentDay", value.timeInMillis)
        }

    fun initAdConfig(config: String) {
        try {
            adConfig = gson.fromJson(config, AdConfig::class.java).apply {
                sw_launch.sortByDescending { it.yqbk }
                sw_detail_int.sortByDescending { it.yqbk }
                sw_main_ban.sortByDescending { it.yqbk }
                appOpenAd.initFlow(sw_launch)
                interstitialAd.initFlow(sw_detail_int)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onAdShowed(){
        val currentDay = Calendar.getInstance()
        if(saveDay(currentDay, lastDay)) currentShowTimes ++
        else {
            lastDay = currentDay
            currentShowTimes = 1
        }
        log("showed ad,current day showed times: $currentShowTimes")
    }
    fun onAdClicked(){
        val currentDay = Calendar.getInstance()
        if(saveDay(currentDay, lastDay)) currentClickTimes ++
        else {
            lastDay = currentDay
            currentClickTimes = 1
        }

        log("clicked ad,current day clicked times: $currentClickTimes")
    }

    fun isOvertimes() : Boolean{
        val currentDay = Calendar.getInstance()
        return saveDay(currentDay, lastDay) && (currentShowTimes >= adConfig.sjkl || currentClickTimes >= adConfig.suhb)
    }

    private fun saveDay(calendar1: Calendar,calendar2: Calendar) = (calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)) && (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR))
}