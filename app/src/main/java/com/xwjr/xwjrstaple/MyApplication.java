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

        StapleUserTokenManager.INSTANCE.saveUserToken("b2d33e49dc8ad2f3055ae38b0769d2f94bb7d2701a1963687dffa9dad224acfb");

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
