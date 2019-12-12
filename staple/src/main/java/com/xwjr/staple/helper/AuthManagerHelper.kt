package com.xwjr.staple.helper

import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson
import com.xwjr.staple.constant.StapleConfig
import com.xwjr.staple.constant.StapleHttpUrl
import com.xwjr.staple.extension.isNotNullOrEmpty
import com.xwjr.staple.extension.logE
import com.xwjr.staple.extension.logI
import com.xwjr.staple.extension.showToast
import com.xwjr.staple.fragment.ProgressDialogFragment
import com.xwjr.staple.manager.StapleUserTokenManager
import com.xwjr.staple.model.StapleAuthIDCardBean
import com.xwjr.staple.model.StapleAuthLiveBean
import com.xwjr.staple.model.StapleRiskShieldStepBean
import com.xwjr.staple.util.FileUtil
import okhttp3.*
import java.io.IOException
import okhttp3.MultipartBody
import okhttp3.RequestBody


/**
 * 身份证识别，活体检测功能
 */
class AuthManagerHelper(private val activity: AppCompatActivity) {
    private var dialog: ProgressDialogFragment? = null

    /**
     * 统一的handler数据发送
     */
    private fun sendData(url: String, data: String, code: Int) {
        Handler(Looper.getMainLooper()).post {
            when (code) {
                -1 -> {
                    showToast(data)
                }
                0 -> {
                    dealIDCardResponse(url, data)
                }
                1 -> {
                    dealLiveResponse(url, data)
                }
                2 -> {
                    dealRiskShieldStep(url, data)
                }

            }
        }
    }


    /**
     * 获取风控中心数据
     */
    fun queryRiskShieldStep(showProgress: Boolean = false) {
        try {
            if (showProgress) {
                dialog = ProgressDialogFragment.newInstance(hint = "数据获取中...")
                dialog?.show(activity.supportFragmentManager)
            }

            logI("开始查询风控中心数据...")
            val url = StapleHttpUrl.queryRiskShieldStep()
            logI("请求URL:$url")
            OkHttpClient().newCall(Request.Builder()
                    .addHeader("Cookie", "ccat=" + StapleUserTokenManager.getUserToken())
                    .url(url)
                    .get()
                    .build()
            ).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    if (showProgress) dialog?.dismiss()
                    logI("网络异常：查询风控中心数据失败 $url")
                    e.printStackTrace()
                    sendData(url, "网络异常：查询风控中心数据失败", -1)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (showProgress) dialog?.dismiss()
                    val data = response.body()?.string().toString()
                    logI("返回数据 $url ：\n$data")
                    sendData(url, data, 2)
                }
            })
        } catch (e: Exception) {
            if (showProgress) dialog?.dismiss()
            logI("数据异常：查询风控中心数据失败")
            sendData("", "数据异常：查询风控中心数据失败", -1)
            e.printStackTrace()
        }
    }

    /**
     * 处理风控中心数据
     */
    private fun dealRiskShieldStep(url: String, data: String) {
        try {
            logI("返回数据 $url ：\n$data")
            val riskShieldStepBean = Gson().fromJson(data, StapleRiskShieldStepBean::class.java)
            if (riskShieldStepBean.checkCodeErrorShow()) {
                if (riskShieldStepBean.result != null)
                    riskShieldData?.stepData(riskShieldStepBean.result!!)
                else
                    showToast("风控平台相关数据为空")
            }
        } catch (e: Exception) {
            showToast("风控平台数据解析异常")
            e.printStackTrace()
        }

    }

    /**
     * 上传身份证数据
     * owner : 0:本人 1:非本人
     */
    fun upLoadIDCardInfo(imagePath: String, owner: String = "0", showProgress: Boolean = false) {
        try {
            if (showProgress) {
                dialog = ProgressDialogFragment.newInstance(hint = "身份证数据识别中...")
                dialog?.show(activity.supportFragmentManager)
            }
            logI("开始上传身份证识别数据...")
            val url = StapleHttpUrl.upLoadIDCardInfo()

            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            val file = FileUtil.getFileByPath(imagePath)
            val body = RequestBody.create(MediaType.parse("image/*"), file)
            requestBody.addFormDataPart("image", file.name, body)
            requestBody.addFormDataPart("source", StapleConfig.getRiskShieldSource())
            requestBody.addFormDataPart("owner", owner)

            logI("请求URL:$url  请求参数：{'image':$imagePath 'source':${StapleConfig.getRiskShieldSource()} 'owner':$owner}")

            OkHttpClient().newCall(Request.Builder()
                    .addHeader("Cookie", "ccat=" + StapleUserTokenManager.getUserToken())
                    .url(url)
                    .post(requestBody.build())
                    .build()
            ).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    if (showProgress) dialog?.dismiss()
                    logE("网络异常：上传身份证识别数据失败 $url")
                    sendData("", "网络异常，上传身份证识别数据失败", -1)
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (showProgress) dialog?.dismiss()
                    val data = response.body()?.string().toString()
                    sendData(url, data, 0)
                }
            })
        } catch (e: Exception) {
            if (showProgress) dialog?.dismiss()
            logE("发生异常，身份证数据上传异常")
            sendData("", "发生异常，身份证数据上传异常", -1)
            e.printStackTrace()
        }
    }

    /**
     * 处理身份证返回数据
     */
    private fun dealIDCardResponse(url: String, data: String) {
        try {
            logI("返回数据 $url ：\n$data")
            val authIDCardBean = Gson().fromJson(data, StapleAuthIDCardBean::class.java)
            if (authIDCardBean.checkCodeErrorShow()) {
                if (authIDCardBean.result != null)
                    riskShieldData?.idCardData(authIDCardBean.result!!)
                else
                    showToast("身份证识别关键数据为空")
            }
        } catch (e: Exception) {
            showToast("身份证识别数据解析异常")
            e.printStackTrace()
        }

    }

    /**
     * 上传活体识别数据
     */
    fun upLoadLiveData(name: String, idNumber: String, mobile: String, delta: String?, imgMap: MutableMap<String, String>, showProgress: Boolean = false, channel: String?) {
        try {
            if (showProgress) {
                dialog = ProgressDialogFragment.newInstance(hint = "正在识别...")
                dialog?.show(activity.supportFragmentManager)
            }
            logI("开始上传活体识别数据...")
            val url: String = if (channel.isNotNullOrEmpty()) {
                StapleHttpUrl.upLoadLiveInfo()+"?channel="+channel
            } else {
                StapleHttpUrl.upLoadLiveInfo()
            }
            logI("请求URL:$url")
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            for (key in imgMap.keys) {
                when (key) {
                    "image_best" -> {
                        val imgBest = FileUtil.getFileByPath(imgMap["image_best"])
                        val imgBestBody = RequestBody.create(MediaType.parse("image/*"), imgBest!!)
                        requestBody.addFormDataPart("imgBest", imgBest.name, imgBestBody)
                        logI("请求参数:'imgBest':${imgMap["image_best"]}")
                    }
                    "image_action1" -> {
                        val imgAction1 = FileUtil.getFileByPath(imgMap["image_action1"])
                        val imgAction1Body = RequestBody.create(MediaType.parse("image/*"), imgAction1!!)
                        requestBody.addFormDataPart("imgAction1", imgAction1.name, imgAction1Body)
                        logI("请求参数:'imgAction1':${imgMap["image_action1"]}")
                    }
                    "image_action2" -> {
                        val imgAction2 = FileUtil.getFileByPath(imgMap["image_action2"])
                        val imgAction2Body = RequestBody.create(MediaType.parse("image/*"), imgAction2!!)
                        requestBody.addFormDataPart("imgAction2", imgAction2.name, imgAction2Body)
                        logI("请求参数:'imgAction2':${imgMap["image_action2"]}")
                    }
                    "image_action3" -> {
                        val imgAction3 = FileUtil.getFileByPath(imgMap["image_action3"])
                        val imgAction3Body = RequestBody.create(MediaType.parse("image/*"), imgAction3!!)
                        requestBody.addFormDataPart("imgAction3", imgAction3.name, imgAction3Body)
                        logI("请求参数:'imgAction3':${imgMap["image_action3"]}")
                    }
                    "image_action4" -> {
                        val imgAction4 = FileUtil.getFileByPath(imgMap["image_action4"])
                        val imgAction4Body = RequestBody.create(MediaType.parse("image/*"), imgAction4!!)
                        requestBody.addFormDataPart("imgAction4", imgAction4.name, imgAction4Body)
                        logI("请求参数:'image_action4':${imgMap["imgAction4"]}")
                    }
                    "image_env" -> {
                        val imageEnv = FileUtil.getFileByPath(imgMap["image_env"])
                        val imageEnvBody = RequestBody.create(MediaType.parse("image/*"), imageEnv!!)
                        requestBody.addFormDataPart("imageEnv", imageEnv.name, imageEnvBody)
                        logI("请求参数:'imageEnv':${imgMap["image_env"]}")
                    }
                }
            }

            requestBody.addFormDataPart("source", StapleConfig.getRiskShieldSource())
            requestBody.addFormDataPart("name", name)
            requestBody.addFormDataPart("idNumber", idNumber)
            if (delta != null)
                requestBody.addFormDataPart("delta", delta)
            requestBody.addFormDataPart("mobile", mobile)
            logI("请求参数:'source':${StapleConfig.getRiskShieldSource()} 'name':$name  'idNumber':$idNumber")


            OkHttpClient().newCall(Request.Builder()
                    .addHeader("Cookie", "ccat=" + StapleUserTokenManager.getUserToken())
                    .url(url)
                    .post(requestBody.build())
                    .build()
            ).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    if (showProgress) dialog?.dismiss()
                    logE("网络异常：上传活体识别数据失败 $url")
                    sendData("", "网络异常，上传活体识别数据失败", -1)
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (showProgress) dialog?.dismiss()
                    val data = response.body()?.string().toString()
                    sendData(url, data, 1)
                }
            })
        } catch (e: Exception) {
            if (showProgress) dialog?.dismiss()
            logE("发生异常，活体识别数据上传异常")
            sendData("", "发生异常，活体识别数据上传异常", -1)
            e.printStackTrace()
        }
    }

    /**
     * 处理活体识别数据
     */
    private fun dealLiveResponse(url: String, data: String) {
        try {
            logI("返回数据 $url ：\n$data")
            val authLiveBean = Gson().fromJson(data, StapleAuthLiveBean::class.java)
            if (authLiveBean.checkCodeErrorShow()) {
                if (authLiveBean.result != null && authLiveBean?.result?.approved != null) {
                    riskShieldData?.liveData(authLiveBean.result?.approved!!)
                } else {
                    showToast("活体识别关键数据为空")
                }
            }
        } catch (e: Exception) {
            showToast("活体识别数据解析异常")
            e.printStackTrace()
        }

    }


    /**
     * 上传百度身份证数据
     * owner : 0:本人 1:非本人
     */
    fun upLoadBaiduIDCardInfo(imagePath: String, owner: String = "0", side: String, showProgress: Boolean = false) {
        try {
            if (showProgress) {
                dialog = ProgressDialogFragment.newInstance(hint = "身份证数据识别中...")
                dialog?.show(activity.supportFragmentManager)
            }
            logI("开始上传身份证识别数据...")
            val url = StapleHttpUrl.upLoadIDCardInfo() + "?channel=baidu"

            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            val file = FileUtil.getFileByPath(imagePath)
            val body = RequestBody.create(MediaType.parse("image/*"), file)
            requestBody.addFormDataPart("image", file.name, body)
            requestBody.addFormDataPart("source", StapleConfig.getRiskShieldSource())
            requestBody.addFormDataPart("owner", owner)
            requestBody.addFormDataPart("side", side)

            logI("请求URL:$url  请求参数：{'image':$imagePath 'source':${StapleConfig.getRiskShieldSource()} 'owner':$owner}")

            OkHttpClient().newCall(Request.Builder()
                    .addHeader("Cookie", "ccat=" + StapleUserTokenManager.getUserToken())
                    .url(url)
                    .post(requestBody.build())
                    .build()
            ).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    if (showProgress) dialog?.dismiss()
                    logE("网络异常：上传身份证识别数据失败 $url")
                    sendData("", "网络异常，上传身份证识别数据失败", -1)
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (showProgress) dialog?.dismiss()
                    val data = response.body()?.string().toString()
                    sendData(url, data, 0)
                }
            })
        } catch (e: Exception) {
            if (showProgress) dialog?.dismiss()
            logE("发生异常，身份证数据上传异常")
            sendData("", "发生异常，身份证数据上传异常", -1)
            e.printStackTrace()
        }
    }

    private var riskShieldData: RiskShieldData? = null

    interface RiskShieldData {
        fun idCardData(authIDCardBean: StapleAuthIDCardBean.ResultBean)
        fun liveData(isApproved: Boolean)
        fun stepData(riskShieldStepBean: StapleRiskShieldStepBean.ResultBean)
    }

    fun setRiskShieldDataListener(riskShieldData: RiskShieldData) {
        this.riskShieldData = riskShieldData
    }
}
