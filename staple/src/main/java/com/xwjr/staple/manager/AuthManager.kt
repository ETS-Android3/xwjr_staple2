package com.xwjr.staple.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Base64
import com.megvii.idcardlib.IDCardScanActivity
import com.megvii.idcardlib.util.Util
import com.megvii.idcardquality.IDCardQualityLicenseManager
import com.megvii.licensemanager.Manager
import com.megvii.livenessdetection.LivenessLicenseManager
import com.megvii.livenesslib.LivenessActivity
import com.megvii.livenesslib.util.ConUtil
import com.xwjr.staple.BuildConfig
import com.xwjr.staple.constant.StapleConfig
import com.xwjr.staple.extension.laterDeal
import com.xwjr.staple.extension.logE
import com.xwjr.staple.extension.logI
import com.xwjr.staple.extension.showToast
import com.xwjr.staple.permission.PermissionRequest
import com.xwjr.staple.permission.PermissionUtils
import com.xwjr.staple.util.BitmapUtil
import com.xwjr.staple.util.FileUtil
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.ArrayList

/**
 * 身份证识别，活体检测功能
 */
object AuthManager {

    const val PAGE_INTO_IDCARDSCAN = 100
    const val PAGE_INTO_LIVENESS = 101
    const val BAIDU_ID_FRONT = 1688
    const val BAIDU_ID_BACK = 1689

    private var idCardLicense = false //身份证扫描联网授权
    private var livingLicense = false //活体检测联网授权
    private const val retryTime = 10000L //联网授权重试时间


    /** face++ 身份证扫描联网授权*/
    fun getIDCardLicense(context: Context, deal: () -> Any = {}) {
        try {
            Thread(Runnable {
                val manager = Manager(context)
                val idCardLicenseManager = IDCardQualityLicenseManager(context)
                val uuid = Util.getUUIDString(context)
                manager.registerLicenseManager(idCardLicenseManager)
                manager.takeLicenseFromNetwork(uuid)
                val code = idCardLicenseManager.checkCachedLicense()
                if (code > 0) {
                    deal()
                    idCardLicense = true
                    if (!livingLicense)
                        getLivingLicense(context)
                    logI("身份证识别联网授权 OK")
                } else {
                    logI("身份证识别联网授权 False --  code:$code")
                    laterDeal(retryTime) {
                        //60s后重试
                        getIDCardLicense(context)
                    }
                    idCardLicense = false
                }
            }).start()
        } catch (e: Exception) {
            logE("身份证扫描联网授权认证异常")
            e.printStackTrace()
        }
    }

    /** 开启扫描身份证
     * activity: 指定的Activity
     * side：扫描的正反面  0：正面  1：反面
     * fragment：如果是在fragment中启动，则需要传fragment的值，否则传null或不传
     */
    fun openScanIdActivity(activity: AppCompatActivity, fragment: Fragment? = null, side: Int) {
        try {
            if (idCardLicense) {
                if (PermissionUtils.checkPermission(activity,
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                "相机或读写权限")) {
                    if (fragment != null)
                        startScan(activity, side, fragment)
                    else
                        startScan(activity, side)
                }
            } else {
                showToast("身份证扫描授权失败")
            }
        } catch (e: Exception) {
            showToast("身份证扫描打开异常")
            e.printStackTrace()
        }
    }

    /**启动扫描身份证
     * activity: 指定的Activity
     * side：扫描的正反面  0：正面  1：反面
     * fragment：如果是在fragment中启动，则需要传fragment的值，否则传null或不传
     */
    private fun startScan(activity: AppCompatActivity, side: Int, fragment: Fragment? = null) {
        try {
            val intent = Intent()
            intent.setClass(activity, IDCardScanActivity::class.java)
            intent.putExtra("side", side)
            intent.putExtra("isVertical", false)
            if (fragment != null)
                fragment.startActivityForResult(intent, PAGE_INTO_IDCARDSCAN)
            else
                activity.startActivityForResult(intent, PAGE_INTO_IDCARDSCAN)
        } catch (e: Exception) {
            showToast("启动扫描身份证异常")
            e.printStackTrace()
        }

    }

    /**
     * 扫描身份证获取数据后处理
     */
    fun dealIDCardScan(data: Intent, deal: (filePath: String) -> Any) {
        try {
            val idCardImg = data.getByteArrayExtra("idcardImg")
            if (idCardImg != null) {
                val bitmap = BitmapFactory.decodeByteArray(idCardImg, 0, idCardImg.count())
                val filePath = StapleConfig.getImgFilePath() + "/idcardImg" + System.currentTimeMillis() + ".png"
                val success = save(bitmap, FileUtil.getFileByPath(filePath), Bitmap.CompressFormat.PNG, true)
                if (success) {
                    deal(filePath)
                }
            } else {
                showToast("身份证识别失败, 请重新识别")
            }
        } catch (e: Exception) {
            showToast(" 身份证数据处理异常")
            e.printStackTrace()
        }
    }

    /**储存身份识别照片*/
    private fun save(src: Bitmap, file: File, format: Bitmap.CompressFormat, recycle: Boolean): Boolean {
        try {
            if (isEmptyBitmap(src) || !FileUtil.createOrExistsFile(file)) return false
            var os: OutputStream? = null
            var ret = false
            try {
                os = BufferedOutputStream(FileOutputStream(file))
                ret = src.compress(format, 100, os)
                if (recycle && !src.isRecycled) src.recycle()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                os?.close()
            }
            return ret
        } catch (e: Exception) {
            showToast("储存身份识别照片异常")
            return false
        }
    }

    /**判断bitmap是否为空*/
    private fun isEmptyBitmap(src: Bitmap?): Boolean {
        return src == null || src.width == 0 || src.height == 0
    }


    /** face++ 联网授权*/
    fun getLivingLicense(context: Context, deal: () -> Any = {}) {
        try {
            Thread(Runnable {
                val manager = Manager(context)
                val licenseManager = LivenessLicenseManager(context)
                val uuid = Util.getUUIDString(context)
                manager.registerLicenseManager(licenseManager)
                manager.takeLicenseFromNetwork(uuid)
                val code = licenseManager.checkCachedLicense()
                livingLicense = if (code > 0) {
                    deal()
                    if (!idCardLicense)
                        getIDCardLicense(context)
                    logI("活体检测联网授权 OK")
                    true
                } else {
                    logI("活体检测联网授权 False --  code:$code")
                    laterDeal(retryTime) {
                        //60s后重试
                        getLivingLicense(context)
                    }
                    false
                }
            }).start()
        } catch (e: Exception) {
            logE("活体识别联网授权异常")
            e.printStackTrace()
        }

    }

    /**
     * 开始活体检测
     */
    fun startLivingDetect(activity: AppCompatActivity, fragment: Fragment? = null) {
        try {
            if (PermissionUtils.checkPermission(activity,
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            "相机或读写权限")) {
                if (livingLicense) {
                    if (fragment != null)
                        goLiving(activity, fragment)
                    else
                        goLiving(activity)
                }
            }
        } catch (e: Exception) {
            showToast("开始活体检测异常")
            e.printStackTrace()
        }

    }

    /**跳转活体检测认证页面*/
    private fun goLiving(activity: AppCompatActivity, fragment: Fragment? = null) {
        try {
            val intent = Intent()
            intent.setClass(activity, LivenessActivity::class.java)
            if (fragment != null)
                fragment.startActivityForResult(intent, PAGE_INTO_LIVENESS)
            else
                activity.startActivityForResult(intent, PAGE_INTO_LIVENESS)
        } catch (e: Exception) {
            showToast("跳转活体检测异常")
            e.printStackTrace()
        }

    }

    /**
     * 处理活体检测数据
     */
    @Suppress("UNCHECKED_CAST")
    fun dealLivingData(context: Context, intent: Intent, deal: (imagesMap: MutableMap<String, String>, bestImg: Bitmap?, delta: String) -> Any) {
        try {
            val bundle = intent.extras!!
            val resultOBJ = bundle.getString("result")
            val result = JSONObject(resultOBJ)
            val resultStr = result.getString("result")
//            val resID = result.getInt("resultcode")
            val isSuccess = resultStr == "验证成功"
            if (isSuccess) {
                val delta = bundle.getString("delta")
                val images: Map<String, ByteArray> = bundle.getSerializable("images") as Map<String, ByteArray>
                val image_best = images["image_best"]
//                val image_env = images["image_env"]
                var bestBitmap: Bitmap? = null
                if (image_best != null)
                    bestBitmap = BitmapFactory.decodeByteArray(image_best, 0, image_best.count())
                val imagesMap: MutableMap<String, String> = mutableMapOf()
                for ((key, value) in images) {
                    imagesMap[key] = ConUtil.saveJPGFile(context, value, key)
                }
                deal(imagesMap, bestBitmap, delta!!)
            } else {
                logI("活体检测验证失败")
            }
        } catch (e: JSONException) {
            showToast("活体检测数据处理失败")
            e.printStackTrace()
        } catch (e: Exception) {
            showToast("活体检测验证数据处理失败")
            e.printStackTrace()
        }
    }


    /**
     * 处理活体检测数据
     */
    @Suppress("UNCHECKED_CAST")
    fun dealBaiduLivingData(imagesMap: HashMap<String, String>, deal: (imagesMap: MutableMap<String, String>) -> Any) {
        try {
            val imageBestFile = imagesMap["bestImage0"]?:""
            val imagesBackMap: MutableMap<String, String> = mutableMapOf()
            imagesBackMap["image_best"] =  base64ToFile(imageBestFile)
            deal(imagesBackMap)
        } catch (e: JSONException) {
            showToast("活体检测数据处理失败")
            e.printStackTrace()
        } catch (e: Exception) {
            showToast("活体检测验证数据处理失败")
            e.printStackTrace()
        }
    }


    /**
     * 跳转到照相机
     */
    fun Context.gotoCamera(
            activity: FragmentActivity,
            requestCode: Int,
            deal: (tempFile: File) -> Unit
    ) {
        //logI("*****************打开相机********************")
        //创建拍照存储的图片文件
        val tempFile = File(checkDirPath(StapleConfig.getImgFilePath()), System.currentTimeMillis().toString() + ".png")
        if (tempFile == null) {
            activity.showToast("没有获取到文件！")
            return
        }
        //跳转到调用系统相机
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //设置7.0中共享文件，分享路径定义在xml/file_paths.xml
            intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            val contentUri = FileProvider.getUriForFile(
                    activity,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    tempFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)

        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile))
        }
        //logI( "**********startActivityForResult*******")

        deal(tempFile)
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * 开启相机
     */
    fun startCamera(activity: FragmentActivity,
                    requestCode: Int,
                    deal: (filePath: String) -> Any) {
        PermissionRequest.getInstance(activity).requestPermission(object : PermissionRequest.PermissionListener {
            override fun permissionGranted() {
                val fileName = System.currentTimeMillis().toString() + ".jpg"
                val filePath = StapleConfig.getImgFilePath() + fileName
                val file = File(filePath)
                deal(filePath)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val uri = MyFileProvider.getUriForFile(activity, file)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                activity.startActivityForResult(intent, requestCode)
            }

            override fun permissionDenied(permissions: ArrayList<String>?) {
                activity.showToast("权限不足，请授权后使用")
            }

            override fun permissionNeverAsk(permissions: ArrayList<String>?) {
                activity.showToast("权限不足，请授权后使用")
            }
        }, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    /**
     * 检查文件是否存在
     */
    private fun checkDirPath(dirPath: String): String {
        if (TextUtils.isEmpty(dirPath)) {
            return ""
        }
        val dir = File(dirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dirPath
    }

    /**
     * 扫描身份证获取数据后处理
     */
    fun dealBaiduIDCardScan(path: String, deal: (filePath: String) -> Any) {
        try {
            BitmapUtil.getCompressFile(path).apply {
                deal(this)
            }
        } catch (e: Exception) {
            showToast(" 身份证数据处理异常")
            e.printStackTrace()
        }
    }

    fun base64ToFile(base64: String):String {
        val fileName = System.currentTimeMillis().toString() + ".png"
        val file: File?
        //创建文件目录
        val dir = File(StapleConfig.getImgFilePath())
        if (!dir.exists() && !dir.isDirectory) {
            dir.mkdirs()
        }
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            file = File("${StapleConfig.getImgFilePath()}/$fileName")
            fos = FileOutputStream(file)
            bos = BufferedOutputStream(fos)
            bos.write(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (bos != null) {
                try {
                    bos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return "${StapleConfig.getImgFilePath()}/$fileName"
    }
}
