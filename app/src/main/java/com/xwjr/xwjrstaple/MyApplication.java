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

        StapleConfig.INSTANCE.setAppSource(StapleConfig.APPHUB);
        StapleConfig.INSTANCE.setDebug(true);

        StapleUserTokenManager.INSTANCE.saveUserToken("831b3a594d3c4611240b704a288712d79b24229dcb547445bfda968cbfb71074");

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
