package com.tw.artin.base.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.ToastUtils
import com.gyf.immersionbar.ImmersionBar
import com.hjq.bar.TitleBar
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.lxj.xpopup.XPopup
import com.tw.artin.BuildConfig
import com.tw.artin.base.BaseActivity
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.action.TitleBarAction
import okhttp3.Call
import java.io.File
import java.util.*

abstract class TwActivity : BaseActivity(), TitleBarAction, OnHttpListener<Any> {

    /** 标题栏对象  */
    private var mTitleBar: TitleBar? = null

    /** 状态栏沉浸  */
    private lateinit var mImmersionBar: ImmersionBar

    /** p层(控制层)  */
    abstract fun <T : BaseInfVp.BasePresenter>getPresenter() : T?

    /** 加载对话框 */
    val LoadingView by lazy {
        XPopup.Builder(this)
            .hasShadowBg(false)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false)
            .asLoading()
    }


    fun showDialog(){
        LoadingView.show()
    }

    fun hideDialog(){
        LoadingView.dismiss()
    }

    override fun initLayout() {
        super.initLayout()

        getTitleBar()?.run {
            this.setOnTitleBarListener(this@TwActivity)
        }

        initImmersion()
    }

    /**
     * 初始化沉浸式
     */
    protected open fun initImmersion() { // 初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            createStatusBarConfig().init()
            // 设置标题栏沉浸
            mTitleBar?.run {
                ImmersionBar.setTitleBar(this@TwActivity,this)
            }
        }
    }

    /**
     * 是否使用沉浸式状态栏
     */
    protected open fun isStatusBarEnabled(): Boolean {
        return true
    }

    /**
     * 状态栏字体白色模式
     */
    protected open fun isStatusBarDarkFont(): Boolean {
        return false
    }

    /**
     * 初始化沉浸式状态栏
     */
    protected open fun createStatusBarConfig(): ImmersionBar { // 在BaseActivity里初始化
        mImmersionBar = ImmersionBar.with(this) // 默认状态栏字体颜色为黑色
            .statusBarDarkFont(isStatusBarDarkFont())
        return mImmersionBar
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    open fun getStatusBarConfig(): ImmersionBar? {
        return mImmersionBar
    }


    override fun setTitle(title: CharSequence?) {
        mTitleBar?.run {
            setTitle(title)
        }
    }

    override fun getTitleBar(): TitleBar? {
        if (mTitleBar == null){
            mTitleBar = findTitleBar(getContentView())
        }
        return mTitleBar
    }


    override fun setTitle(titleId: Int) {
        setTitle(resources.getString(titleId))
    }

    override fun onLeftClick(v: View?) {
        super.onLeftClick(v)
        finish()
    }

    override fun onStart(call: Call?) {
        showDialog()
    }

    override fun onFail(e: Exception?) {
        ToastUtils.showShort(e?.message)
    }

    override fun onEnd(call: Call?) {
        hideDialog()
    }

    override fun onDestroy() {
        getPresenter<BaseInfVp.BasePresenter>()?.onDestroy()
        EasyHttp.cancel(this)
        hideDialog()
        super.onDestroy()
    }

    override fun onSucceed(result: Any?) {

    }

    fun is5GHz(ssid: String, context: Context): Boolean {
        val wifiManger =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManger.connectionInfo
        return if (wifiInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val freq = wifiInfo.frequency
            freq in 4901..5899
        } else ssid.toUpperCase(Locale.ROOT).endsWith("5G")
    }


    fun openWifi() {
        var wifiSettingsIntent = Intent("android.settings.WIFI_SETTINGS")
        if (null != wifiSettingsIntent.resolveActivity(packageManager)) {
            startActivity(wifiSettingsIntent)
        } else {
            wifiSettingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
            if (null != wifiSettingsIntent.resolveActivity(packageManager)) {
                startActivity(wifiSettingsIntent)
            }
        }
    }


    fun openfile(file: File){

        val intent = Intent()

        intent.action = Intent.ACTION_VIEW

        val type: String = getMIMEType(file)
        if (type == "*/*") {
            ToastUtils.showShort("不支持此类型")
            return
        } else {

            try {
                val data : Uri

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    data = FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    )
                    // 给目标应用一个临时授权
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    data = Uri.fromFile(file)
                }

                intent.setDataAndType(data, type)
                startActivity(intent)
            } catch (e: Exception) {
                ToastUtils.showShort("打开文件失败")
            }
        }

    }


    private fun getMIMEType(file: File): String{
        var type = "*/*"
        val fName = file.name
        //获取后缀名前的分隔符"."在fName中的位置。
        val dotIndex = fName.lastIndexOf(".")
        if (dotIndex < 0) return type
        /* 获取文件的后缀名 */
        val fileType = fName.substring(dotIndex, fName.length).toLowerCase()
        //val fileType = fName.substring(dotIndex, fName.length).toLowerCase()
        if (fileType == null || "" == fileType) return type
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (i in MIME_MapTable.indices) {
            if (fileType == MIME_MapTable[i][0]) type = MIME_MapTable[i][1]
        }
        return type
    }

    private val MIME_MapTable =
        arrayOf(
            arrayOf(".3gp", "video/3gpp"),
            arrayOf(".apk", "application/vnd.android.package-archive"),
            arrayOf(".asf", "video/x-ms-asf"),
            arrayOf(".avi", "video/x-msvideo"),
            arrayOf(".bin", "application/octet-stream"),
            arrayOf(".bmp", "image/bmp"),
            arrayOf(".c", "text/plain"),
            arrayOf(".class", "application/octet-stream"),
            arrayOf(".conf", "text/plain"),
            arrayOf(".cpp", "text/plain"),
            arrayOf(".doc", "application/msword"),
            arrayOf(".docx", "application/msword"),
            arrayOf(".xls", "application/msword"),
            arrayOf(".xlsx", "application/msword"),
            arrayOf(".exe", "application/octet-stream"),
            arrayOf(".gif", "image/gif"),
            arrayOf(".gtar", "application/x-gtar"),
            arrayOf(".gz", "application/x-gzip"),
            arrayOf(".h", "text/plain"),
            arrayOf(".htm", "text/html"),
            arrayOf(".html", "text/html"),
            arrayOf(".jar", "application/java-archive"),
            arrayOf(".java", "text/plain"),
            arrayOf(".jpeg", "image/jpeg"),
            arrayOf(".JPEG", "image/jpeg"),
            arrayOf(".jpg", "image/jpeg"),
            arrayOf(".js", "application/x-javascript"),
            arrayOf(".log", "text/plain"),
            arrayOf(".m3u", "audio/x-mpegurl"),
            arrayOf(".m4a", "audio/mp4a-latm"),
            arrayOf(".m4b", "audio/mp4a-latm"),
            arrayOf(".m4p", "audio/mp4a-latm"),
            arrayOf(".m4u", "video/vnd.mpegurl"),
            arrayOf(".m4v", "video/x-m4v"),
            arrayOf(".mov", "video/quicktime"),
            arrayOf(".mp2", "audio/x-mpeg"),
            arrayOf(".mp3", "audio/x-mpeg"),
            arrayOf(".mp4", "video/mp4"),
            arrayOf(".mpc", "application/vnd.mpohun.certificate"),
            arrayOf(".mpe", "video/mpeg"),
            arrayOf(".mpeg", "video/mpeg"),
            arrayOf(".mpg", "video/mpeg"),
            arrayOf(".mpg4", "video/mp4"),
            arrayOf(".mpga", "audio/mpeg"),
            arrayOf(".msg", "application/vnd.ms-outlook"),
            arrayOf(".ogg", "audio/ogg"),
            arrayOf(".pdf", "application/pdf"),
            arrayOf(".png", "image/png"),
            arrayOf(".pps", "application/vnd.ms-powerpoint"),
            arrayOf(".ppt", "application/vnd.ms-powerpoint"),
            arrayOf(".pptx", "application/vnd.ms-powerpoint"),
            arrayOf(".prop", "text/plain"),
            arrayOf(".rar", "application/x-rar-compressed"),
            arrayOf(".rc", "text/plain"),
            arrayOf(".rmvb", "audio/x-pn-realaudio"),
            arrayOf(".rtf", "application/rtf"),
            arrayOf(".sh", "text/plain"),
            arrayOf(".tar", "application/x-tar"),
            arrayOf(".tgz", "application/x-compressed"),
            arrayOf(".txt", "text/plain"),
            arrayOf(".wav", "audio/x-wav"),
            arrayOf(".wma", "audio/x-ms-wma"),
            arrayOf(".wmv", "audio/x-ms-wmv"),
            arrayOf(".wps", "application/vnd.ms-works"),
            arrayOf(".xml", "text/plain"),
            arrayOf(".z", "application/x-compress"),
            arrayOf(".zip", "application/zip"),
            arrayOf("", "*/*")
        )

}