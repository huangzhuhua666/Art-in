package com.tw.artin.ui.activity

import android.os.Build
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.*
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.hjq.http.listener.OnDownloadListener
import com.hjq.http.model.HttpMethod
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity
import com.tw.artin.dfu.DfuService
import com.tw.artin.http.api.DfuApi
import com.tw.artin.http.model.HttpData
import com.tw.artin.util.UtilsBigDecimal
import kotlinx.android.synthetic.main.updata_dfu_activity.*
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import java.io.File


class UpDataDfuActivity : TwActivity() {

    var deviceAddress : String = ""
    var model_name : String = ""

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.updata_dfu_activity
    }

    override fun initView() {
    }

    override fun initData() {

        model_name = intent.getStringExtra("model") ?: ""

        if (model_name != ""){
            val model = model_name.substring(0, model_name.indexOf("_"))
            getNetData(model)
        }

        deviceAddress = intent.getStringExtra("deviceAddress") ?: ""

    }


    fun getNetData(mModel: String){

        EasyHttp.post(this)
            .api(
                DfuApi(mModel)
            )
            .request(object : HttpCallback<HttpData<DfuApi.DfuBean>>(this) {

                override fun onSucceed(result: HttpData<DfuApi.DfuBean>?) {
                    super.onSucceed(result)
                    result?.objx?.let {
                        val path = it.attach
                        if (path != "") {
                            //downLoadFile(path)

                            val netmd5 = it.md5

                            val filelist = FileUtils.listFilesInDirWithFilter(PathUtils.getInternalAppFilesPath()
                            ) { it.name.endsWith(".zip") }

                            if (filelist.isEmpty()){
                                downLoadFile(path)
                            }else{

                                var hasFile = false
                                var fPath = ""

                                filelist.forEach {
                                    val bdmd5 = FileUtils.getFileMD5ToString(it)

                                    if (StringUtils.equalsIgnoreCase(netmd5,bdmd5)){
                                        hasFile = true
                                        fPath = it.path
                                        return@forEach
                                    }
                                }

                                //本地有文件
                                if (hasFile){

                                    postDelayed({
                                        DfuUpgrade(fPath)
                                    },800)

                                }else{
                                    //没有文件，下载
                                    downLoadFile(path)
                                }

                            }
                        }
                    }
                }

                override fun onFail(e: Exception?) {
                    super.onFail(e)
                    ToastUtils.showShort(R.string.dfu_text13)
                    finish()
                }
            })

    }

    fun downLoadFile(url: String){

        val file = File(PathUtils.getInternalAppFilesPath(), "${System.currentTimeMillis()}.zip")

        EasyHttp.download(this)
            .method(HttpMethod.GET)
            .file(file)
            .url(url)
            .listener(object : OnDownloadListener {

                override fun onStart(file: File?) {
                }

                override fun onProgress(file: File?, progress: Int) {

                }

                override fun onComplete(file: File?) {

                    file?.let {

                        if (it.exists()) {

                            postDelayed({
                                DfuUpgrade(it.path)
                            },800)

                        }
                    }
                    LogUtils.d(file?.path)
                }

                override fun onError(file: File?, e: Exception?) {
                    ToastUtils.showShort(R.string.dfu_text14)
                    finish()
                }

                override fun onEnd(file: File?) {
                }
            })
            .start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun DfuUpgrade(zipPath: String){

        val starter = DfuServiceInitiator(deviceAddress)
            .setDeviceName(model_name)
            .setZip(zipPath)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(this)
        }

        starter.start(this, DfuService::class.java)
    }

    private val mDfuProgressListener = object : DfuProgressListenerAdapter() {

        /*override fun onDeviceConnecting(deviceAddress: String) {
        }

        override fun onDeviceDisconnecting(deviceAddress: String) {
            ToastUtils.showShort(R.string.dfu_text15)
        }*/

        override fun onDfuCompleted(deviceAddress: String) {

            ToastUtils.showShort(R.string.dfu_text16)

            postDelayed({
                NotificationUtils.cancelAll()
                finish()
            },300)
        }

        override fun onDfuAborted(deviceAddress: String) {

            postDelayed({
                NotificationUtils.cancelAll()
            },300)
        }

        override fun onProgressChanged(
            deviceAddress: String,
            percent: Int,
            speed: Float,
            avgSpeed: Float,
            currentPart: Int,
            partsTotal: Int
        ) {
            pb_update.setProgress(percent)
//            upgrade_pbar.progress = percent
//            tv_jd.text = "($currentPart/$partsTotal)"

//            val sp = UtilsBigDecimal.div(avgSpeed.toDouble(),1.0)

//            tv_upgrade.text = "$sp KB/s"
        }

        override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String) {

            ToastUtils.showShort(message)

            postDelayed({
                NotificationUtils.cancelAll()
            },300)
        }
    }

    override fun onResume() {
        super.onResume()
        DfuServiceListenerHelper.registerProgressListener(this,mDfuProgressListener)
    }

    override fun onPause() {
        super.onPause()
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener)
    }
}