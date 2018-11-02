package com.xwjr.staple.helper

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.xwjr.staple.constant.StapleHttpUrl
import com.xwjr.staple.extension.base64ToBitmap
import com.xwjr.staple.extension.logI
import com.xwjr.staple.extension.showToast
import com.xwjr.staple.model.StapleCaptchaBean
import com.xwjr.staple.model.StapleSMSCaptchaBean
import okhttp3.*
import java.io.IOException
import java.lang.Exception


/**
 * 身份证识别，活体检测功能
 */
class StapleHelper {


    /**
     * 统一的handler数据发送
     */
    private fun sendData(data: String, code: Int) {
        Handler(Looper.getMainLooper()).post {
            when (code) {
                -1 -> {
                    showToast(data)
                }
                0 -> {
                    //处理图形验证码数据
                    val captchaData = Gson().fromJson(data, StapleCaptchaBean::class.java)
                    if (captchaData?.token != null && captchaData.captcha != null) {
                        val bitmap = base64ToBitmap(captchaData.captcha!!)
                        if (bitmap != null) {
                            captchaListener?.backData(captchaData.token!!, bitmap)
                        } else {
                            showToast("无法显示图形验证码图片...")
                        }
                    } else {
                        showToast("获取图形验证码失败...")
                    }
                }
                1 -> {
                    //处理短信验证码数据
                    val smsCaptchaData = Gson().fromJson(data, StapleSMSCaptchaBean::class.java)
                    if (smsCaptchaData.checkCodeErrorShow()) {
                        if (smsCaptchaData.result != null && smsCaptchaData?.result?.smsCaptchaToken != null) {
                            smsCaptchaListener?.backData(smsCaptchaData.result?.smsCaptchaToken!!)
                        } else {
                            showToast("短信验证码数据解析异常...")
                        }
                    }
                }
            }
        }
    }

    /**
     * 查询图形验证码数据
     */
    fun getCaptchaData() {
        logI("开始获取图形验证码数据...")
        val url = StapleHttpUrl.captchaUrl()
        logI("请求URL:$url")
        OkHttpClient().newCall(Request.Builder()
                .url(url)
                .get()
                .build()
        ).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                logI("网络异常：获取图形验证码数据失败 $url")
                e.printStackTrace()
                sendData("网络异常：获取图形验证码数据失败", -1)
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body()?.string().toString()
                logI("返回数据 $url ：\n$data")
                sendData(data, 0)
            }
        })
    }

    private var captchaListener: CaptchaListener? = null

    interface CaptchaListener {
        fun backData(captchaToken: String, captchaBitmap: Bitmap)
    }

    fun setCaptchaListener(captchaListener: CaptchaListener) {
        this.captchaListener = captchaListener
    }

    /**
     * 发送短信验证码
     */
    fun sendSMSCaptcha(mobile: String, captchaToken: String, captchaAnswer: String) {
        logI("开始获取短信验证码数据...")
        val url = StapleHttpUrl.smsCaptchaUrl(mobile, captchaToken, captchaAnswer)
        logI("请求URL:$url")
        OkHttpClient().newCall(Request.Builder()
                .url(url)
                .get()
                .build()
        ).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                logI("网络异常：获取短信验证码数据失败 $url")
                e.printStackTrace()
                sendData("网络异常：获取短信验证码数据失败", -1)
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body()?.string().toString()
                logI("返回数据 $url ：\n$data")
                sendData(data, 1)
            }
        })
    }

    private var smsCaptchaListener: SMSCaptchaListener? = null

    interface SMSCaptchaListener {
        fun backData(smsCaptchaToken: String)
    }

    fun setSMSCaptchaListener(smsCaptchaListener: SMSCaptchaListener) {
        this.smsCaptchaListener = smsCaptchaListener
    }

}
