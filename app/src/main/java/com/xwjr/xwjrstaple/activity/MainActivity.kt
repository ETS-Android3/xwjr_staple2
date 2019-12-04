package com.xwjr.xwjrstaple.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xwjr.staple.camera.TakePhotoActivity
import com.xwjr.staple.extension.*
import com.xwjr.staple.fragment.ProgressDialogFragment
import com.xwjr.staple.jwt.JWTUtils
import com.xwjr.staple.manager.AuthManager
import com.xwjr.staple.helper.AuthManagerHelper
import com.xwjr.staple.helper.StapleHttpHelper
import com.xwjr.staple.model.StapleAuthIDCardBean
import com.xwjr.staple.model.StapleRiskShieldStepBean
import com.xwjr.xwjrstaple.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var stapleHelper: StapleHttpHelper? = null
    private var captchaToken = ""
    private val authManagerHelper = AuthManagerHelper(this)
    private var refreshBroadcast: BroadcastReceiver? = null
    private var filePath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logI("scheme:" + intent.scheme)

        stapleHelper = StapleHttpHelper(this)

//        AuthManager.getIDCardLicense(this)
        AuthManager.getLivingLicense(this)
        tv_queryRiskShieldData.setOnClickListener {
            authManagerHelper.queryRiskShieldStep(true)
        }
        tv_idCardScan.setOnClickListener {
            AuthManager.openScanIdActivity(this, side = 0)
        }
        tv_idCardScan2.setOnClickListener {
            AuthManager.openScanIdActivity(this, side = 1)
        }
        tv_liveScan.setOnClickListener {
            AuthManager.startLivingDetect(this)
        }
        tv_jwt.setOnClickListener {
            val dataMap = HashMap<String, String>()
            dataMap["name"] = "吕正志"
            logI(JWTUtils.getToken(JWTUtils.getKey(JWTUtils.FKZX), "{\"name\":\"吕正志\"}"))
        }
        tv_captcha.setOnClickListener {
            stapleHelper?.getCaptchaData(true)
        }
        tv_smsCaptcha.setOnClickListener {
            stapleHelper?.sendSMSCaptcha("18810409404", captchaToken, et_captcha.text.toString(), true)
        }
        tv_webView.setOnClickListener {
            startActivity(Intent(this@MainActivity, WebViewActivity::class.java))
        }

        tv_progress.setOnClickListener {
            val progress = ProgressDialogFragment.newInstance(hint = "哈哈哈///")
            progress.show(supportFragmentManager)
            laterDeal {
                progress.dismiss()
            }
        }

        tv_fileReader.setOnClickListener {
            startActivity(Intent(this@MainActivity, FileReaderActivity::class.java))
        }

        tv_broadcast.setOnClickListener {
            refreshBroadcast = registerBroadcast("changeName") { intent ->
                val name = intent.getStringExtra("name")
                tv_broadcast.text = name
            }
            startActivity(Intent(this@MainActivity, BroadcastActivity::class.java))
        }

        tv_baiduIdCardFront.setOnClickListener {
            val intent = Intent(this, TakePhotoActivity::class.java)
            intent.putExtra("side", "front")
            intent.putExtra("source", "wwxjk")
            startActivityForResult(intent, AuthManager.BAIDU_ID_FRONT)
//            AuthManager.startCamera(this, AuthManager.BAIDU_ID_FRONT) {
//                filePath = it
//                ""
//            }
        }

        tv_baiduIdCardBack.setOnClickListener {
            val intent = Intent(this, TakePhotoActivity::class.java)
            intent.putExtra("side", "back")
            intent.putExtra("source", "wwnt")
            startActivityForResult(intent, AuthManager.BAIDU_ID_BACK)
//            AuthManager.startCamera(this, AuthManager.BAIDU_ID_BACK) {
//                filePath = it
//                ""
//            }
        }

        stapleHelper?.addCaptchaListener(object : StapleHttpHelper.CaptchaListener {
            override fun backData(captchaToken: String, captchaBitmap: Bitmap) {
                showToast("token:$captchaToken")
                iv_captcha.setImageBitmap(captchaBitmap)
                this@MainActivity.captchaToken = captchaToken
            }
        })
        stapleHelper?.addSMSCaptchaListener(object : StapleHttpHelper.SMSCaptchaListener {
            override fun backData(smsCaptchaToken: String) {
                showToast("token:$smsCaptchaToken")
            }
        })

        authManagerHelper.setRiskShieldDataListener(object : AuthManagerHelper.RiskShieldData {
            override fun stepData(riskShieldStepBean: StapleRiskShieldStepBean.ResultBean) {
                showToast(riskShieldStepBean.toString())
            }

            override fun liveData(isApproved: Boolean) {
                showToast(isApproved.toString())
            }

            override fun idCardData(authIDCardBean: StapleAuthIDCardBean.ResultBean) {
                showToast(authIDCardBean.toString())
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK)
                when (requestCode) {
                    AuthManager.PAGE_INTO_IDCARDSCAN -> {
                        AuthManager.dealIDCardScan(data!!) { filePath ->
                            logI(filePath)
                            authManagerHelper.upLoadIDCardInfo(filePath, showProgress = true)
                        }
                    }

                    AuthManager.PAGE_INTO_LIVENESS -> {
                        AuthManager.dealLivingData(this, data!!) { imagesMap, _, delta ->
                            authManagerHelper.upLoadLiveData("朱小航", "412326199211116919", "18810409404", delta, imagesMap, true)
                        }
                    }
                    AuthManager.BAIDU_ID_FRONT -> {
                        if (data == null) {
                            return
                        }
                        AuthManager.dealBaiduIDCardScan(data.getStringExtra("filePath")) {
                            authManagerHelper.upLoadBaiduIDCardInfo(
                                    imagePath = it,
                                    side = data.getStringExtra("side"),
                                    showProgress = true
                            )
                        }
                    }
                    AuthManager.BAIDU_ID_BACK -> {
                        if (data == null) {
                            return
                        }
                        AuthManager.dealBaiduIDCardScan(filePath) {
                            authManagerHelper.upLoadBaiduIDCardInfo(
                                    imagePath = it,
                                    side = data.getStringExtra("side"),
                                    showProgress = true
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroy() {
        if (refreshBroadcast != null)
            unRegisterBroadcast(refreshBroadcast!!)
        super.onDestroy()
    }
}
