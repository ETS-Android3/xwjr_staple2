package com.xwjr.staple.extension

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import java.lang.reflect.InvocationTargetException

/**
 * 获取App版本号
 */
fun Context.getVersionName(): String {
    return try {
        val pm = this.packageManager
        val pi = pm.getPackageInfo(packageName, 0)
        pi.versionName
    } catch (e: Exception) {
        logI("发生异常：获取app版本号失败")
        e.printStackTrace()
        ""
    }
}

/**
 * 查看通知权限是否开启
 */
fun Context.isNotificationEnabled(): Boolean {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mAppOps = this.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

            val appInfo = this.applicationInfo
            val pkg = this.applicationContext.packageName
            val uid = appInfo.uid

            val appOpsClass = Class.forName(AppOpsManager::class.java.name)
            val checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE,
                    String::class.java)
            val opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION")

            val value = opPostNotificationValue.get(Int::class.java) as Int
            return checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) as Int == AppOpsManager.MODE_ALLOWED

        } else {
            return true
        }
    } catch (e: ClassNotFoundException) {
        logE("获取通知权限 ClassNotFoundException")
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        logE("获取通知权限 NoSuchMethodException")
        e.printStackTrace()
    } catch (e: NoSuchFieldException) {
        logE("获取通知权限 NoSuchFieldException")
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        logE("获取通知权限 InvocationTargetException")
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        logE("获取通知权限 IllegalAccessException")
        e.printStackTrace()
    } catch (e: Exception) {
        logE("获取通知权限 Exception")
        e.printStackTrace()
    }
    return false
}

/**
 * 将应用进程置顶
 */
fun Context.setTopApp() {
    try {
        //    if (!isRunningForeground()) {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE)
        activityManager as ActivityManager
        val taskInfoList = activityManager.getRunningTasks(100)
        for (item in taskInfoList) {
            /**找到本应用的 task，并将它切换到前台*/
            if (item.topActivity.packageName == packageName) {
                activityManager.moveTaskToFront(item.id, 0)
                break
            }
        }
//    }
    } catch (e: Exception) {
        logE("将应用进程置顶时发生异常")
        e.printStackTrace()
    }
}

/**
 * 当前app是否在前台运行
 */
fun Context.isRunningForeground(): Boolean {
    try {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE)
        activityManager as ActivityManager
        val appProcessList = activityManager.runningAppProcesses
        /**枚举进程*/
        for (item in appProcessList) {
            if (item.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (item.processName == applicationInfo.processName) {
                    return true
                }
            }
        }
    } catch (e: Exception) {
        logE("获取app是否在前台运行时发生异常")
        e.printStackTrace()
    }
    return false
}

/**
 * 注册广播
 */
fun Context.registerBroadcast(action: String, deal: ((intent: Intent) -> Unit)? = null): BroadcastReceiver {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            try {
                if (deal != null) deal(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    try {
        val intentFilter = IntentFilter()
        intentFilter.addAction(action)
        registerReceiver(receiver, intentFilter)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return receiver

}

/**
 * 取消注册广播
 */
fun Context.unRegisterBroadcast(receiver: BroadcastReceiver) {
    try {
        unregisterReceiver(receiver)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 发送广播
 */
fun Context.sendBroadcast(action: String, intent: Intent) {
    try {
        intent.action = action
        sendBroadcast(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


