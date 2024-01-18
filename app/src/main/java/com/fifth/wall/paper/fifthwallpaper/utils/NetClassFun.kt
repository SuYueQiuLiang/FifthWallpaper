package com.fifth.wall.paper.fifthwallpaper.utils

import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class NetClassFun {
    private val client: OkHttpClient = OkHttpClient()

    fun getMapData(url: String, map: Map<String, Any>,onSucess:(response: String)->Unit,onError:(error: String)->Unit) {
        val urlBuilder = url.toHttpUrl().newBuilder()
        map.forEach { entry ->
            urlBuilder.addEncodedQueryParameter(
                entry.key,
                URLEncoder.encode(entry.value.toString(), StandardCharsets.UTF_8.toString())
            )
        }
        val request = Request.Builder()
            .get()
            .tag(map)
            .url(urlBuilder.build())
            .cacheControl(CacheControl.FORCE_NETWORK)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    onSucess(responseBody)
                } else {
                    onError(responseBody.toString())
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                onError("Network error")
            }
        })
    }

}