package com.fifth.wall.paper.fifthwallpaper.utils

import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.fifth.wall.paper.fifthwallpaper.bean.App
import com.fifth.wall.paper.fifthwallpaper.bean.InstallReferrer
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object InstallReferrerUtil {
    private lateinit var referrerClient: InstallReferrerClient
    private val mmkv by lazy { MMKV.defaultMMKV() }
    var referrer = InstallReferrer("fb4a")
//        get() = mmkv.decodeParcelable("referrer", InstallReferrer::class.java)
        set(value) {
            mmkv.encode("referrer", value)
        }

    fun getReferrer() {
        if (referrer != null)
            return
        referrerClient = InstallReferrerClient.newBuilder(App.getAppContext()).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    val response = referrerClient.installReferrer
                    referrer = InstallReferrer(
                        response.installReferrer, response.referrerClickTimestampSeconds, response.installBeginTimestampSeconds,
                        response.referrerClickTimestampServerSeconds, response.installBeginTimestampServerSeconds
                    ).apply {
                        UploadUtil.install(this)
                    }
                }
                referrerClient.endConnection()
            }

            override fun onInstallReferrerServiceDisconnected() {
                if (referrer != null)
                    return
                MainScope().launch(Dispatchers.IO) {
                    delay(10000)
                    getReferrer()
                }
            }
        })
    }
}