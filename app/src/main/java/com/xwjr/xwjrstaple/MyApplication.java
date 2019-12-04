package com.xwjr.xwjrstaple;

import android.app.Application;

import com.xwjr.staple.constant.StapleConfig;
import com.xwjr.staple.manager.CrashHandlerManager;
import com.xwjr.staple.manager.StapleUserTokenManager;
import com.xwjr.staple.util.StapleUtils;

import java.util.HashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StapleUtils.init(this);

        StapleConfig.INSTANCE.setAppSource(StapleConfig.XIAODAI);
        StapleConfig.INSTANCE.setDebug(true);

        StapleUserTokenManager.INSTANCE.saveUserToken("ec1a0119d1f62cbe0311b16803c6322b383da8be242b2ba85c498fbd9977cc00");

        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        JPushInterface.setAlias(this, 5233, "a0000000");

        Set<String> tags = new HashSet<>();
        tags.add("A");
        tags.add("B");
        JPushInterface.setTags(this, 5233, tags);

        //崩溃处理
        CrashHandlerManager.getInstance().init(getApplicationContext());

    }
}
