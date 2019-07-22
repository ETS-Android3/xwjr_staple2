package com.xwjr.xwjrstaple.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.xwjr.staple.extension.sendBroadcast
import com.xwjr.staple.extension.showToast
import com.xwjr.xwjrstaple.R
import kotlinx.android.synthetic.main.activity_broadcat.*

class BroadcastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcat)

        tv_content.setOnClickListener {
            showToast("发送广播")
            val intent = Intent()
            intent.putExtra("name","我是发送过来的广播")
            sendBroadcast("changeName", intent)
        }
    }
}
