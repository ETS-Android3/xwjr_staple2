package com.xwjr.staple.activity

import android.Manifest
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson
import com.tencent.smtt.sdk.QbSdk
import com.xwjr.staple.R

import com.xwjr.staple.constant.StapleConfig
import com.xwjr.staple.constant.StapleHttpUrl
import com.xwjr.staple.extension.*
import com.xwjr.staple.fragment.UpdateDialogFragment
import com.xwjr.staple.fragment.UpdateDialogFragmentWWXHB
import com.xwjr.staple.fragment.UpdateDialogFragmentWWXJK
import com.xwjr.staple.manager.StapleActivityBeanManager
import com.xwjr.staple.manager.StapleSplashBeanManager
import com.xwjr.staple.model.StapleActivityBean
import com.xwjr.staple.model.StapleSplashBean
import com.xwjr.staple.model.StapleUpdateBean
import com.xwjr.staple.permission.PermissionRequest
import com.xwjr.staple.presenter.StapleHttpContract
import com.xwjr.staple.presenter.StapleHttpPresenter
import java.io.File
import com.xwjr.staple.permission.PermissionRequest.PermissionListener
import com.xwjr.staple.util.AppStatusUtils
import java.util.ArrayList


abstract class StapleSplashActivity : AppCompatActivity(), StapleHttpContract {

    private lateinit var httpPresenter: StapleHttpPresenter

    //升级数据
    var updateData: StapleUpdateBean.DataBean.NativeVersionBean? = null

    //活动数据
    private var activityData: StapleActivityBean = StapleActivityBean()

    //开屏页面延迟时间
    private var splashTime = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.staple_activity_splash)
        httpPresenter = StapleHttpPresenter(this, this)
        init()
    }

    /**
     * 初始化数据
     */
    @Suppress("DEPRECATION")
    private fun init() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setStatusBar()
        initX5Core()
        logI("是否开启了通知权限：" + isNotificationEnabled())
        dealBeforeInit {
            dealPermission()
        }
        setWindowBackground()
        AppStatusUtils.saveHaveActivityKilledStatus(this, "")
    }

    /**
     * 初始化X5内核
     */
    private fun initX5Core() {
        QbSdk.setDownloadWithoutWifi(true)
        QbSdk.initX5Environment(this, object : QbSdk.PreInitCallback {
            override fun onCoreInitFinished() {
            }

            override fun onViewInitFinished(b: Boolean) {
                logI("onViewInitFinished,X5内核初始化完成：$b")
            }
        })
    }

    /**
     * 处理权限相关
     */
    fun dealPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            arrayOf(Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        PermissionRequest.getInstance(this)?.requestPermission(object : PermissionListener {
            override fun permissionGranted() {
                logI("permissionGranted")
                queryUpdateInfo()
                querySplashInfo()
            }

            override fun permissionDenied(permissions: ArrayList<String>?) {
                logI("permissionDenied")
                queryUpdateInfo()
                querySplashInfo()
            }

            override fun permissionNeverAsk(permissions: ArrayList<String>?) {
                logI("permissionNeverAsk")
                queryUpdateInfo()
                querySplashInfo()
            }

        }, permissions)
    }

    /**
     * 设置app开屏页面背景
     */
    private fun setWindowBackground() {
        try {
            val filePath = StapleConfig.getImgFilePath() + "/" + StapleConfig.getSplashFileName()
            if (File(filePath).exists()) {
                setWindowBG(filePath = filePath)
            } else {
                logI("未获取到本地开屏页图片，使用默认图片")
                setDefaultWindowBackground()
            }
        } catch (e: Exception) {
            setDefaultWindowBackground()
            logI("设置apphub配置的开屏页失败")
        }

    }

    @Suppress("DEPRECATION")
    private fun setDefaultWindowBackground() {
        try {
            StapleSplashBeanManager.clearSplashInfo(this)
            StapleConfig.dealAppSource(
                    wwxhb = {
                        setWindowBG(resId = R.mipmap.staple_wwxhb_window_bg)
                    },
                    wwxhc = {
                        setWindowBG(resId = R.mipmap.staple_wwxhc_null_window_bg)
                    },
                    xwjr = {
                        setWindowBG(resId = R.mipmap.staple_xwjr_window_bg)
                    },
                    xwb = {
                        setWindowBG(res = resources.getDrawable(R.mipmap.staple_xwb_window_bg))
                    },
                    wwxjk = {
                        setWindowBG(res = resources.getDrawable(R.mipmap.staple_wwxjk_window_bg))
                    },
                    apphub = {
                        setWindowBG(resId = R.mipmap.staple_apphub_window_bg)
                    },
                    xiaodai = {
                        setWindowBG(resId = R.mipmap.staple_xiaodai_window_bg)
                    }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            logI("设置默认开屏页失败")
        }
    }

    /**
     * 查询升级数据
     */
    private fun queryUpdateInfo() {
        httpPresenter.queryUpdateInfo()
    }

    /**
     * 查询活动数据
     */
    private fun queryActivityInfo() {
        httpPresenter.queryActivityInfo()
    }

    /**
     * 查询开屏页数据
     */
    private fun querySplashInfo() {
        httpPresenter.querySplashInfo()
    }

    /**
     * 下载开屏图
     */
    private fun downloadSplashImg(url: String?) {
        if (url != null)
            httpPresenter.downLoadSplashImg(url)
    }

    override fun statusBack(i: String, data: Any) {
        when (i) {
            StapleHttpUrl.updateInfoUrl(this) -> {
                try {
                    logI("获取升级数据成功，开始解析")
                    data as String
                    val updateBean = (Gson().fromJson(data, StapleUpdateBean::class.java))
                    if (updateBean.success) {
                        updateData = updateBean.data?.nativeVersion!!
                        if (updateData!!.hasNew && updateData!!.downloadUrl.isNotNullOrEmpty()) {
                            dealUpdateData()
                        } else {
                            //无升级
                            logI("无升级：")
                            queryActivityInfo()
                        }
                    } else {
                        logI("未查询到升级版本：")
                        queryActivityInfo()
                    }
                } catch (e: Exception) {
                    logI("发生异常：" + "升级数据解析失败")
                    //无升级
                    logI("无升级：")
                    queryActivityInfo()
                    e.printStackTrace()
                }
            }
            StapleHttpUrl.updateInfoUrl(this).err() -> {
                logI("发生异常：获取升级数据失败")
                queryActivityInfo()
            }
            StapleHttpUrl.activityInfoUrl() -> {
                try {
                    logI("获取活动数据成功，开始解析")
                    data as String
                    activityData = (Gson().fromJson(data, StapleActivityBean::class.java))
                    if (activityData.success && activityData.data != null) {
                        if (StapleActivityBeanManager.isNeedShowActivity(this, activityData.data!!)) {
                            dealActivityData(activityData.data)
                        } else {
                            dealActivityData()
                        }
                    } else {
                        dealActivityData()
                    }
                } catch (e: Exception) {
                    logI("发生异常：" + "活动数据解析失败")
                    e.printStackTrace()
                    dealActivityData()
                }
            }
            StapleHttpUrl.activityInfoUrl().err() -> {
                logI("发生异常：获取活动数据失败")
                dealActivityData()
            }
            StapleHttpUrl.splashImgInfoUrl() -> {
                try {
                    data as String
                    val splashBean = (Gson().fromJson(data, StapleSplashBean::class.java))
                    if (splashBean.success) {
                        if (splashBean.data?.imgUrl != null) {
                            if (StapleSplashBeanManager.isNeedDownloadSplashImg(this, splashBean.data?.imgUrl!!)) {
                                downloadSplashImg(splashBean.data?.imgUrl)
                            }
                        } else {
                            deleteSplashImg()
                        }
                    } else {
                        deleteSplashImg()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            StapleHttpUrl.splashImgInfoUrl().err() -> {
                logI("发生异常：获取开屏图失败")
                deleteSplashImg()
            }
        }
    }

    /**
     * 移除本地开屏图图片
     */
    private fun deleteSplashImg() {
        try {
            val file = File(StapleConfig.getImgFilePath() + "/" + StapleConfig.getSplashFileName())
            if (file.exists()) {
                file.delete()
                logI("已删除本地开屏图")
            } else {
                logI("本地不存在开屏图")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 处理升级相关数据
     */
    private fun dealUpdateData() {
        logI("是否强制升级：${updateData!!.forceUpdate}")
        when (StapleConfig.appSource) {
            StapleConfig.WWXHB -> {
                UpdateDialogFragmentWWXHB
                        .newInstance(updateData!!.forceUpdate, updateData!!.downloadUrl!!, "V" + updateData!!.version!!, updateData!!.changeLog!!).apply {
                            setCancelUpdateListener(object : UpdateDialogFragmentWWXHB.CancelUpdate {
                                override fun cancel() {
                                    logI("稍后升级")
                                    queryActivityInfo()
                                }
                            })
                            show(supportFragmentManager)
                        }
            }
            StapleConfig.WWXJK -> {
                UpdateDialogFragmentWWXJK
                        .newInstance(updateData!!.forceUpdate, updateData!!.downloadUrl!!, "V" + updateData!!.version!!, updateData!!.changeLog!!).apply {
                            setCancelUpdateListener(object : UpdateDialogFragmentWWXJK.CancelUpdate {
                                override fun cancel() {
                                    logI("稍后升级")
                                    queryActivityInfo()
                                }
                            })
                            show(supportFragmentManager)
                        }
            }
            else -> {
                UpdateDialogFragment
                        .newInstance(updateData!!.forceUpdate, updateData!!.downloadUrl!!, "V" + updateData!!.version!!, updateData!!.changeLog!!).apply {
                            setCancelUpdateListener(object : UpdateDialogFragment.CancelUpdate {
                                override fun cancel() {
                                    logI("稍后升级")
                                    queryActivityInfo()
                                }
                            })
                            show(supportFragmentManager)
                        }
            }

        }
    }


    //自定义处理数据
    abstract fun customDealActivityData(latestData: StapleActivityBean.DataBean? = null)

    private fun dealActivityData(latestData: StapleActivityBean.DataBean? = null) {
        laterDeal(splashTime) {
            customDealActivityData(latestData)
        }
    }

    //自定义处理数据
    abstract fun beforeInit(deal: (() -> Unit))

    private fun dealBeforeInit(deal: (() -> Unit)) {
        beforeInit(deal)
    }
}
