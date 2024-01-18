package com.fifth.wall.paper.fifthwallpaper.ui.first

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fifth.wall.paper.fifthwallpaper.R
import com.fifth.wall.paper.fifthwallpaper.ad.AdLoaderManager
import com.fifth.wall.paper.fifthwallpaper.ad.base.FullScreenAdCallbacks
import com.fifth.wall.paper.fifthwallpaper.ui.home.MainActivity
import com.fifth.wall.paper.fifthwallpaper.utils.UploadUtil
import com.fifth.wall.paper.fifthwallpaper.utils.log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FirstActivity : AppCompatActivity() {
    private lateinit var processBar: ProgressBar
    private lateinit var consentInformation: ConsentInformation
    private var isShowing = false
    private var CMP_GET = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        init()
    }

    fun init() {
        processBar = findViewById(R.id.progressBar)
        requestConsentInfo(this@FirstActivity) {
            CMP_GET = true
        }
    }

    private var progressJob: Job? = null

    override fun onStart() {
        super.onStart()
        processBar()
    }

    override fun onStop() {
        super.onStop()
        progressJob?.cancel()
    }

    //进度条2秒
    private fun processBar() {
        if(isShowing)
            return
        progressJob = lifecycleScope.launch(Dispatchers.Main) {
            val startTime = System.currentTimeMillis()
            AdLoaderManager.appOpenAd.loadAd(this@FirstActivity.lifecycle)
            while (CMP_GET.not()) {
                val currentProgress = processBar.progress
                if(currentProgress < 50)
                    processBar.progress = currentProgress + 1
                delay(100)
            }
            val passed = System.currentTimeMillis() - startTime
            val delay = if(passed > 9000) 1000 else 10000 - passed
            val currentProgress = processBar.progress
            val per = delay / (100 - currentProgress)
            for(i in currentProgress .. 100){
                processBar.progress = i
                delay(per)
            }
            if (AdLoaderManager.appOpenAd.isAdReady()) {
                isShowing = true
                AdLoaderManager.appOpenAd.showAd(this@FirstActivity, object : FullScreenAdCallbacks {
                    override fun onAdDismissed() {
                        toMainActivity()
                    }
                })
            }
            else toMainActivity()
        }
    }

    //跳转到主页
    private fun toMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK
    }

    private fun requestConsentInfo(context: Activity, callBack: (Boolean) -> Unit) {
        val debugSettings = ConsentDebugSettings.Builder(this)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("EE84C96866C28AB46BF716CF8721CAC5")
            .addTestDeviceHashedId("535638A67725004580758F1DAB10E6B5")
            .build()
        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
        consentInformation = UserMessagingPlatform.getConsentInformation(context)
        consentInformation.requestConsentInfoUpdate(context,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    context,
                    ConsentForm.OnConsentFormDismissedListener { loadAndShowError ->
                        if (loadAndShowError != null || !consentInformation.canRequestAds()) {
                            callBack.invoke(false)
                            return@OnConsentFormDismissedListener
                        }
                        if (consentInformation.canRequestAds()) {
                            callBack.invoke(true)
                        }
                    }
                )
            },
            {
                callBack.invoke(false)
            })
    }
}
