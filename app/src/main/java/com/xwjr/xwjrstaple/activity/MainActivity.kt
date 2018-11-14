package com.xwjr.xwjrstaple.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xwjr.staple.extension.laterDeal
import com.xwjr.staple.extension.logI
import com.xwjr.staple.extension.showToast
import com.xwjr.staple.fragment.ProgressDialogFragment
import com.xwjr.staple.jwt.JWTUtils
import com.xwjr.staple.manager.AuthManager
import com.xwjr.staple.helper.AuthManagerHelper
import com.xwjr.staple.helper.StapleHelper
import com.xwjr.staple.model.StapleAuthIDCardBean
import com.xwjr.xwjrstaple.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var stapleHelper: StapleHelper? = null
    private var captchaToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stapleHelper = StapleHelper(this)

        AuthManager.getIDCardLicense(this)
        AuthManager.getLivingLicense(this)
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
            showToast(JWTUtils.getJWT(JWTUtils.SMS))
        }
        tv_captcha.setOnClickListener {
            stapleHelper?.getCaptchaData()
        }
        tv_smsCaptcha.setOnClickListener {
            stapleHelper?.sendSMSCaptcha("18810409404", captchaToken, et_captcha.text.toString())
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

        stapleHelper?.addCaptchaListener(object : StapleHelper.CaptchaListener {
            override fun backData(captchaToken: String, captchaBitmap: Bitmap) {
                showToast("token:$captchaToken")
                iv_captcha.setImageBitmap(captchaBitmap)
                this@MainActivity.captchaToken = captchaToken
            }
        })
        stapleHelper?.addSMSCaptchaListener(object : StapleHelper.SMSCaptchaListener {
            override fun backData(smsCaptchaToken: String) {
                showToast("token:$smsCaptchaToken")
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
                            val authManagerHelper = AuthManagerHelper(this)
                            authManagerHelper.upLoadIDCardInfo(filePath)
                            authManagerHelper.setRiskShieldDataListener(object : AuthManagerHelper.RiskShieldData {
                                override fun liveData(isApproved: Boolean) {

                                }

                                override fun idCardData(authIDCardBean: StapleAuthIDCardBean.ResultBean) {
                                    tv_idCardScan2.text = authIDCardBean.name
                                }
                            })
                        }
                    }

                    AuthManager.PAGE_INTO_LIVENESS -> {
                        AuthManager.dealLivingData(this, data!!) { imagesMap, _, delta ->
                            val authManagerHelper = AuthManagerHelper(this)
                            authManagerHelper.upLoadLiveData("朱小航", "412326199211116919", delta, imagesMap)
                            authManagerHelper.setRiskShieldDataListener(object : AuthManagerHelper.RiskShieldData {
                                override fun liveData(isApproved: Boolean) {

                                }

                                override fun idCardData(authIDCardBean: StapleAuthIDCardBean.ResultBean) {

                                }
                            })
                        }
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
