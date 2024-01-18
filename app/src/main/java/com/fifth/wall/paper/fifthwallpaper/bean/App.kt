package com.fifth.wall.paper.fifthwallpaper.bean

import android.app.Application
import android.content.Context
import android.util.Base64
import android.util.Log
import com.fifth.wall.paper.fifthwallpaper.ad.AdLoaderManager
import com.fifth.wall.paper.fifthwallpaper.utils.InstallReferrerUtil
import com.fifth.wall.paper.fifthwallpaper.utils.NetClassFun
import com.fifth.wall.paper.fifthwallpaper.utils.NetFun
import com.google.android.gms.ads.MobileAds
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

val gson = Gson()
class App : Application() {
    var successState = false
    companion object{
        private lateinit var instance: App
        fun getAppContext() = instance
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        if (NetFun.uu_stunning.isEmpty()) {
            NetFun.uu_stunning = UUID.randomUUID().toString()
        }
        getBlackList(this@App)
        registerActivityLifecycleCallbacks(ActivityManager)
        MMKV.initialize(this)
        InstallReferrerUtil.getReferrer()

//        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this)
        val localString = String(Base64.decode(localAdConfig,Base64.DEFAULT))
        AdLoaderManager.initAdConfig(localString)
        InstallReferrer.initUserConfig(userConfig)
        AdLoaderManager.appOpenAd.loadAd(null)
        AdLoaderManager.interstitialAd.loadAd(null)
    }

    fun getBlackList(context: Context) {
        if (NetFun.hmd_data.isNotEmpty()) {
            return
        }
        GlobalScope.launch(Dispatchers.IO) {
            getBlackList(context, onSuccess = {
                successState = true
                Log.e("TAG", "The blacklist request is successful：$it")
                NetFun.hmd_data = it
            }, onFail = {
                retry(context)
            })
        }
    }
    //发起黑名单请求
    fun getBlackList(context: Context,onSuccess:(it:String)->Unit,onFail:(it:String)->Unit) {
        val map = NetFun.getHmdData(context)
        Log.e("TAG", "Blacklist request data= $map", )
        NetClassFun().getMapData("https://swigging.stunningwallpapers.net/basin/goody", map, {
            onSuccess(it)
        }, {
            //请求失败
            onFail(it)
        })
    }
    //请求重试
    fun retry(context: Context) {
        successState = false
        GlobalScope.launch(Dispatchers.IO) {
            delay(10000)
            Log.e("TAG", "The blacklist request failed")
            getBlackList(context)
        }
    }

    val remoteConfig: FirebaseRemoteConfig by lazy { Firebase.remoteConfig }

    private fun loadConfig(){
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    AdLoaderManager.initAdConfig(remoteConfig.getString("sw_ad_config"))
                    InstallReferrer.initUserConfig(remoteConfig.getString("sw_config_user"))
                }
            }
    }

    private var userConfig = """
        {
        "sw_ref_ask":0
        }
    """.trimIndent()

    private val localAdConfig = """ewogICJzamtsIjogMTAsCiAgInN1aGIiOiAzLAogICJzd19sYXVuY2giOiBbCiAgICB7CiAgICAgICJhdWJicCI6ICJjYS1hcHAtcHViLTM5NDAyNTYwOTk5NDI1NDQvOTI1NzM5NTkyMSIsCiAgICAgICJpYWloIjogImFkbW9iIiwKICAgICAgImF5aWtuIjogIm9wIiwKICAgICAgImF5eWIiOiAxMzgwMCwKICAgICAgInlxYmsiOiAyCiAgICB9LAogICAgewogICAgICAiYXViYnAiOiAiY2EtYXBwLXB1Yi0zOTQwMjU2MDk5OTQyNTQ0LzkyNTczOTU5MjEiLAogICAgICAiaWFpaCI6ICJhZG1vYiIsCiAgICAgICJheWlrbiI6ICJvcCIsCiAgICAgICJheXliIjogMTM4MDAsCiAgICAgICJ5cWJrIjogMwogICAgfQogIF0sCiAgICAic3dfZGV0YWlsX2ludCI6IFsKICAgIHsKICAgICAgImF1YmJwIjogImNhLWFwcC1wdWItMzk0MDI1NjA5OTk0MjU0NC8xMDMzMTczNzEyIiwKICAgICAgImlhaWgiOiAiYWRtb2IiLAogICAgICAiYXlpa24iOiAiaW50IiwKICAgICAgImF5eWIiOiAzMDAwLAogICAgICAieXFiayI6IDIKICAgICB9LAogICAgewogICAgICAiYXViYnAiOiAiY2EtYXBwLXB1Yi0zOTQwMjU2MDk5OTQyNTQ0LzEwMzMxNzM3MTIiLAogICAgICAiaWFpaCI6ICJhZG1vYiIsCiAgICAgICJheWlrbiI6ICJpbnQiLAogICAgICAiYXl5YiI6IDMwMDAsCiAgICAgICJ5cWJrIjogMQogICAgIH0sCiAgICB7CiAgICAgICJhdWJicCI6ICJjYS1hcHAtcHViLTM5NDAyNTYwOTk5NDI1NDQvMTAzMzE3MzcxMkFBIiwKICAgICAgImlhaWgiOiAiYWRtb2IiLAogICAgICAiYXlpa24iOiAiaW50IiwKICAgICAgImF5eWIiOiAzMDAwLAogICAgICAieXFiayI6IDMKICAgICB9CiAgXSwKICAic3dfbWFpbl9iYW4iOiBbCiAgICB7CiAgICAgICJhdWJicCI6ICJjYS1hcHAtcHViLTM5NDAyNTYwOTk5NDI1NDQvMjAxNDIxMzYxNyIsCiAgICAgICJpYWloIjogImFkbW9iIiwKICAgICAgImF5aWtuIjogImJhbiIsCiAgICAgICJheXliIjogMzAwMCwKICAgICAgInlxYmsiOiAzCiAgICB9CiAgXQp9"""
}