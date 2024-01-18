package com.fifth.wall.paper.fifthwallpaper.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.webkit.WebSettings
import com.fifth.wall.paper.fifthwallpaper.bean.App
import com.fifth.wall.paper.fifthwallpaper.bean.InstallReferrer
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale
import java.util.UUID


object UploadUtil {
    val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }
    val okhttpClient by lazy { OkHttpClient.Builder().build() }
    var JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    private val telephonyManager by lazy { App.getAppContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager }
    private val scope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    private val packageInfo by lazy { App.getAppContext().packageManager.getPackageInfo(App.getAppContext().packageName, 0) }

    fun install(installReferrer: InstallReferrer) {
        scope.launch {
            val jsonObject = initBaseJson()
            val drexel = JsonObject().apply {
                addProperty("cafe", Build.ID)
                addProperty("lyon", installReferrer.referrer)
                addProperty("division", WebSettings.getDefaultUserAgent(App.getAppContext()))
                addProperty("judy", if (getAdTrackable()) 0 else 1)
                addProperty("lange", installReferrer.clickReferrerTime)
                addProperty("trail", installReferrer.appInstallTime)
                addProperty("shamble", installReferrer.clickReferrerTimeServer)
                addProperty("ball", installReferrer.appInstallTimeServer)
                addProperty("nadir", packageInfo.firstInstallTime)
                addProperty("prohibit", packageInfo.lastUpdateTime)
            }
            jsonObject.add("drexel", drexel)
            post(jsonObject)
        }
    }

    fun session() {
        scope.launch {
            val jsonObject = initBaseJson()
            jsonObject.addProperty("yarmulke", "bangui")
            post(jsonObject)
        }
    }

    fun ad(oenology : Long,agile : String,saginaw : String,glimmer : String,wolf : String,anent : String,conjugal : String){
        scope.launch {
            val jsonObject = initBaseJson()
            val genus = JsonObject().apply {
                addProperty("oenology",oenology)
                addProperty("agile",agile)
                addProperty("saginaw",saginaw)
                addProperty("glimmer",glimmer)
                addProperty("wolf",wolf)
                addProperty("anent",anent)
                addProperty("conjugal",conjugal)
            }
            jsonObject.add("genus",genus)
            post(jsonObject)
        }
    }

    fun upload(name: String, values: Map<String, String>? = null) {
        scope.launch {
            val jsonObject = initBaseJson()
            jsonObject.addProperty("yarmulke", name)
            values?.let {
                val json = JsonObject().apply {
                    it.forEach {
                        addProperty(it.key, it.value)
                    }
                }
                jsonObject.add(name, json)
            }
//            firebaseAnalytics.logEvent(name, values?.let {
//                Bundle().apply {
//                    it.forEach {
//                        putString(it.key, it.value)
//                    }
//                }
//            })
            post(jsonObject)
        }
    }

    private suspend fun post(jsonObject: JsonObject) {
        try {
            for (i in 0..3) {
                val httpUrl = HttpUrl.Builder()
                    .scheme("https")
                    .host("test-cedilla.stunningwallpapers.net")
                    .addPathSegments("quetzal/why/reverend")
                    .addQueryParameter("furry", Locale.getDefault().country)
                    .addQueryParameter("squad", Build.MANUFACTURER)
                    .addQueryParameter("priory", getAdId())
                    .build()
                log("http post url : $httpUrl")
                val request = Request.Builder()
                    .post(jsonObject.toString().toRequestBody(JSON))
                    .url(httpUrl)
                    .header("squad", Build.MANUFACTURER)
                    .header("furry", Locale.getDefault().country)
                    .build()
                val call = okhttpClient.newCall(request).execute()
                val code = call.code
                log("http post return code : $code,body ${call.body?.string()}")
                if (code == 200)
                    break
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("HardwareIds")
    private suspend fun initBaseJson(): JsonObject {
        val maltose = JsonObject().apply {
            addProperty("dial", Build.VERSION.RELEASE)
            addProperty("flounce", "${Locale.getDefault().language}_${Locale.getDefault().country}")
            addProperty("bin", NetFun.uu_stunning)
            addProperty("protista", Settings.Secure.getString(App.getAppContext().contentResolver, Settings.Secure.ANDROID_ID))
            addProperty("vet", UUID.randomUUID().toString())
        }
        val frolic = JsonObject().apply {
            addProperty("depict", "com.stunning.wallpapers.artistic.photo.beauty")
            addProperty("priory", getAdId())
            addProperty("strum", System.currentTimeMillis())
            addProperty("fleck", packageInfo.versionName)
            addProperty("squad", Build.MANUFACTURER)
            addProperty("palazzo", telephonyManager.networkOperator)
            addProperty("ammeter", "clang")
            addProperty("conceal", Build.MODEL)
        }
        return JsonObject().apply {
            add("maltose", maltose)
            add("frolic", frolic)
        }
    }

    private fun getAdId() =
        try {
            AdvertisingIdClient.getAdvertisingIdInfo(App.getAppContext()).id
        } catch (e: Exception) {
            null
        }

    private fun getAdTrackable() = try {
        AdvertisingIdClient.getAdvertisingIdInfo(App.getAppContext()).isLimitAdTrackingEnabled.not()
    } catch (e: Exception) {
        false
    }
}