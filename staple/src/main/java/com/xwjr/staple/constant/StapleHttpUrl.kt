package com.xwjr.staple.constant

import android.content.Context
import com.xwjr.staple.extension.getVersionName


object StapleHttpUrl {

    /**
     * 域名配置
     */
    private fun getBaseUrl(): String {
        return if (StapleConfig.isDebug) {
            "http://p2psp.kfxfd.cn:9080"
        } else {
            "https://www.xwjr.com"
        }
    }

    private fun getRiskShieldBaseUrl(): String {
        return if (StapleConfig.isDebug) {
            "http://p2psp.kfxfd.cn:9080"
        } else {
            //todo 调整域名
            "http://xiaodai.xwjr.com"
        }
    }


    /**
     * 获取webview需要配置域名list
     */
    fun getDomainUrl():List<String> {
        return if (StapleConfig.isDebug){
            arrayListOf("http://p2psp.kfxfd.cn:9080","http://p2p.slowlytime.com:9084")
        }else{
            arrayListOf("https://www.xwjr.com","http://xiaodai.xwjr.com")
        }
    }

    /**
     * 版本升级
     */
    fun updateInfoUrl(context: Context): String {
        return getBaseUrl() + "/apphub/app/checkUpdate?" +
                "appkey=" + StapleConfig.getAppKey() +
                "&bundleVersion=" + context.getVersionName() +
                "&nativeVersion=" + context.getVersionName() +
                "&token=" + StapleConfig.getAppToken() +
                "&platform=android"
    }

    /**
     * app活动
     */
    fun activityInfoUrl(): String {
        return getBaseUrl() + "/apphub/activity/latest/" + StapleConfig.getAppKey()
    }

    /**
     * app开屏页
     */
    fun splashImgInfoUrl(): String {
        return getBaseUrl() + "/apphub/splash/latest/" + StapleConfig.getAppKey()
    }

    /**
     * 风控中心身份识别状态
     */
    fun queryRiskShieldStep():String{
        return getRiskShieldBaseUrl() + "/rsapi/verify/steps/MYSELF/" +StapleConfig.getRiskShieldSource()
    }

    /**
     * 上传身份证识别数据
     */
    fun upLoadIDCardInfo(): String {
        return getRiskShieldBaseUrl() + "/rsapi/verify/idCard/ocr/MYSELF"
    }

    /**
     * 上传活体识别数据
     */
    fun upLoadLiveInfo(): String {
        return getRiskShieldBaseUrl() + "/rsapi/verify/faceId/ocr/MYSELF"
    }

    /**
     * 图形验证码
     */
    fun getCaptchaUrl(): String {
        return getBaseUrl() + "/api/v2/captcha"
    }

    /**
     * 短信验证码
     * type:0  图形验证码鉴权
     * type:1  jwt鉴权
     */
    fun getSmsCaptchaUrl(mobile: String, captchaToken: String, captchaAnswer: String): String {
        return getBaseUrl() + "/api/v2/smsCaptcha?" +
                "to=" + mobile +
                "&captchaToken=" + captchaToken +
                "&captchaAnswer=" + captchaAnswer +
                "&auth=captcha"
    }
}
