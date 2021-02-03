package com.tw.artin.ui.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.BusUtils.ThreadMode
import com.blankj.utilcode.util.ToastUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity
import com.tw.artin.bean.DevicesBean
import com.tw.artin.ui.adapter.DfuAdapter
import com.tw.artin.view.UniversalItemDecoration
import kotlinx.android.synthetic.main.device_fragment.*
import kotlinx.android.synthetic.main.dfu_activity.*
import no.nordicsemi.android.support.v18.scanner.*
import java.util.ArrayList

class DfuActivity : TwActivity() {

    //蓝牙是否开启
    var isBluetooth = false

    val bleListenerReceiver by lazy {
        BluetoothMonitorReceiver(this)
    }

    val mBluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    //状态值用于进度条显示
    private var statueMessage = MutableLiveData<String>()

    //联网提示进度条
    val messageView by lazy {
        XPopup.Builder(this)
            .hasShadowBg(false)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false)
            .asCustom(
                LoadingPopupView(this).apply {
                    statueMessage.observeForever {
                        setTitle(it)
                    }
                }
            )
    }

    var isSuccess = false

    var ledName = ""

    val mAdapter by lazy {

        DfuAdapter().apply {

            setOnItemClickListener { adapter, view, position ->

                val info = adapter.getItem(position) as DevicesBean
                ledName = info.note_name ?:""

                if (info.type == 3){

                    XPopup.Builder(this@DfuActivity)
                        .hasShadowBg(false)
                        .asConfirm(resources.getString(R.string.dfu_text02),
                            resources.getString(R.string.dfu_text03)
                        )
                        {

                            isSuccess = false

                            statueMessage.value = resources.getString(R.string.dfu_text17)
                            messageView.show()
                            BusUtils.post("dfuModel",info.unicastAddress)

                            isError()
                        }
                        .setConfirmText(getString(R.string.dfu_text04))
                        .bindLayout(R.layout.dialog_message)
                        .show()

                }

            }
        }
    }

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.dfu_activity
    }

    override fun initView() {

        BusUtils.register(this)

        //监听蓝牙开启关闭
        registerReceiver(
            bleListenerReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
        )

        with(dfu_recycle){

            layoutManager = LinearLayoutManager(this@DfuActivity)

            addItemDecoration(object : UniversalItemDecoration(){
                override fun getItemOffsets(position: Int): Decoration {
                    return ColorDecoration().apply {
                        bottom = 1
                        decorationColor = Color.parseColor("#363636")
                    }
                }
            })
            adapter = mAdapter
        }

    }

    override fun initData() {

        if (mBluetoothAdapter != null){
            isBluetooth = mBluetoothAdapter.isEnabled
        }

        XXPermissions.with(this)
            .permission(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
            .permission(Permission.Group.LOCATION)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {

                    if (all){

                        BusUtils.postSticky("getDfuList")

                    }else{
                        ToastUtils.showShort(R.string.permission_error)
                    }
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {

                    if (never) {
                        ToastUtils.showShort(R.string.permission_error2)
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(this@DfuActivity, permissions)
                    } else {
                        ToastUtils.showShort(R.string.permission_error)
                    }

                }
            })


    }

    override fun onRightClick(v: View?) {
        super.onRightClick(v)
        startActivity(
            Intent(this,SearchDfuActivity::class.java)
        )
    }

    fun startScan() {

        val settings =
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .setUseHardwareFilteringIfSupported(false)
                .build()

        val filters: MutableList<ScanFilter> =
            ArrayList()

        val scanner =
            BluetoothLeScannerCompat.getScanner()

        scanner.startScan(filters, settings, scanCallback)
    }

    private val scanCallback: ScanCallback =
        object : ScanCallback() {
            override fun onScanResult(
                callbackType: Int,
                result: ScanResult
            ) {

                result.scanRecord?.let {datas ->
                    datas.serviceUuids?.let {

                        val has_data = it.any { it.uuid.toString().contains("fe59") }

                        if (has_data && datas.deviceName == ledName){

                            startActivity(
                                Intent(this@DfuActivity,UpDataDfuActivity::class.java).apply {
                                    putExtra("model",ledName)
                                    putExtra("deviceAddress",result.device.address)
                                }
                            )
                            stopScan()
                        }
                    }
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
            }

            override fun onScanFailed(errorCode: Int) {}
        }

    fun stopScan() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.stopScan(scanCallback)
    }

    fun isError(){

        postDelayed({
            if (!isSuccess){
                statueMessage.value = ""
                messageView.dismiss()
                ToastUtils.showLong(R.string.dfu_text18)
            }
        },10000)

    }

    @BusUtils.Bus(tag = "setDfuData",sticky = true,threadMode = ThreadMode.MAIN)
    fun onEvent(list : MutableList<DevicesBean>?){

        list?.let {
            mAdapter.setList(it)
        } ?: mAdapter.setList(mutableListOf())
    }

    @BusUtils.Bus(tag = "goDfu",threadMode = ThreadMode.MAIN)
    fun onEvent01(){

        isSuccess = true

        postDelayed({
            statueMessage.value = ""
            messageView.dismiss()

            if (isBluetooth){
                startScan()
            }

        },500)

    }


    class BluetoothMonitorReceiver(val mActivity : DfuActivity) : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            intent?.let {

                val mAction = it.action ?: ""

                if (mAction == BluetoothAdapter.ACTION_STATE_CHANGED){

                    val blueState = it.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)

                    when(blueState){

                        BluetoothAdapter.STATE_ON ->{
                            mActivity.isBluetooth = true
                        }

                        BluetoothAdapter.STATE_OFF ->{
                            mActivity.isBluetooth = false
                        }

                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (bleListenerReceiver != null){
            unregisterReceiver(bleListenerReceiver)
        }
        stopScan()
        BusUtils.unregister(this)
        super.onDestroy()
    }
}