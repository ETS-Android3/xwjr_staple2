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
        StapleConfig.INSTANCE.setDebug(false);

        StapleUserTokenManager.INSTANCE.saveUserToken("7ffe10c011c391502394732b0c35cef677f9510623807fafa614ed5ebed9178f");

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
