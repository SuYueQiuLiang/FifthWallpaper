package com.fifth.wall.paper.fifthwallpaper.bean

import android.os.Parcelable
import com.fifth.wall.paper.fifthwallpaper.utils.InstallReferrerUtil.referrer
import com.fifth.wall.paper.fifthwallpaper.utils.NetFun
import kotlinx.parcelize.Parcelize


@Parcelize
data class InstallReferrer(
    var referrer : String,
    val clickReferrerTime: Long? = null,
    val clickReferrerTimeServer: Long? = null,
    val appInstallTime: Long? = null,
    val appInstallTimeServer: Long? = null
) : Parcelable{
    companion object{
        lateinit var userConfig: UserConfig

        fun initUserConfig(config : String){
            try{
                userConfig = gson.fromJson(config,UserConfig::class.java)
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
    }


}
private val list = arrayListOf("fb4a","gclid","not%20set","youtubeads","%7B%22","adjust","bytedance")
fun InstallReferrer?.isBlacklist() : Boolean{
    if(InstallReferrer.userConfig.sw_ref_ask != 1)
        return false
    if(NetFun.hmd_data != "urchin")
        return true
    list.forEach { if(this?.referrer?.contains(it) == true) return false}
    return true
}