package com.xwjr.xwjrstaple.activity

import android.content.Intent
import com.xwjr.staple.activity.StapleSplashActivity
import com.xwjr.staple.extension.logI
import com.xwjr.staple.model.StapleActivityBean

class SplashActivity : StapleSplashActivity() {
    override fun customDealActivityData(latestData: StapleActivityBean.DataBean?) {
        if (latestData == null) {
            logI("没有活动数据")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            logI("有活动数据")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
