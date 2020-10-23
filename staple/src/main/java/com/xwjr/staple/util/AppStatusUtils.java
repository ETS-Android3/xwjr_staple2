package com.xwjr.staple.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AppStatusUtils {

    //存取activity是否在后台运行， 0  是
    public static void saveAppInBackgroundStatus(Context context, String status) {
        try {
            SharedPreferences sp = context.getSharedPreferences("StapleTable", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("appInBackgroundStatus", status);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //获取activity是否在后台运行，
    public static String getAppInBackgroundStatus(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences("StapleTable", Context.MODE_PRIVATE);
            return sp.getString("appInBackgroundStatus", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    //获取activity是否在后台运行，
    public static boolean isAppInBackground(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences("StapleTable", Context.MODE_PRIVATE);
            String data = sp.getString("appInBackgroundStatus", "");
            if (data != null) {
                return data.equals("0");
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

    }

    //存储activity是否在后台被杀死的状态，
    public static void saveFirstOpenStatus(Context context, String status) {
        try {
            SharedPreferences sp = context.getSharedPreferences("StapleTable", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("firstOpen", status);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //存储activity是否在后台被杀死的状态，
    public static String getFirstOpenStatus(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences("StapleTable", Context.MODE_PRIVATE);
            return sp.getString("firstOpen", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    //存储activity是否在后台被杀死的状态，
    public static void saveHaveActivityKilledStatus(Context context, String status) {
        try {
            SharedPreferences sp = context.getSharedPreferences("StapleTable", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("haveActivityKilledStatus", status);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取activity是否在后台被杀死的状态
    public static String getHaveActivityKilledStatus(Context context, String status) {
        try {
            SharedPreferences sp = context.getSharedPreferences("StapleTable", Context.MODE_PRIVATE);
            return sp.getString("haveActivityKilledStatus", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    //是否需要重启
    public static boolean isNeedRestart(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences("StapleTable", Context.MODE_PRIVATE);
            String data = sp.getString("haveActivityKilledStatus", "");
            if (data != null) {
                return data.equals("true");
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

    }

    //重启app
    public static void reStartApp(Context context, Activity activity) {
        Intent intent = new Intent(context, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
