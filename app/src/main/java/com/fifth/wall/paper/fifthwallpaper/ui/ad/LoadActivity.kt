package com.fifth.wall.paper.fifthwallpaper.ui.ad

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fifth.wall.paper.fifthwallpaper.R
import com.fifth.wall.paper.fifthwallpaper.ad.AdLoaderManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)
        MainScope().launch {
            delay(500)
            finish()
        }
    }
}