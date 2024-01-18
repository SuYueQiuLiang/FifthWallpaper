package com.fifth.wall.paper.fifthwallpaper.bean
import androidx.annotation.Keep

@Keep
data class AdConfig(
    val sjkl: Int,
    val suhb: Int,
    val sw_detail_int: MutableList<AdmobSource>,
    val sw_launch: MutableList<AdmobSource>,
    val sw_main_ban: MutableList<AdmobSource>
) {
    @Keep
    data class AdmobSource(
        val aubbp: String,
        val ayikn: String,
        val ayyb: Int,
        val iaih: String,
        val yqbk: Int
    )
}