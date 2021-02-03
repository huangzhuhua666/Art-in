package com.tw.artin.ui.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxj.xpopup.XPopup
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity
import com.tw.artin.ui.adapter.SearchBinder
import kotlinx.android.synthetic.main.dfu_search_activity.*
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*

class SearchDfuActivity : TwActivity() {

    //蓝牙是否开启
    var isBluetooth = false

    val bleListenerReceiver by lazy {
        BluetoothMonitorReceiver(this)
    }

    val mBluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private val mAdapter by lazy {

        BaseBinderAdapter().apply {

            addItemBinder(SearchBinder())

            setOnItemClickListener { adapter, view, position ->

                val info = adapter.getItem(position) as ScanResult

                XPopup.Builder(this@SearchDfuActivity)
                    .hasShadowBg(false)
                    .asConfirm(resources.getString(R.string.dfu_text02),
                        resources.getString(R.string.dfu_text03)
                    )
                    {

                        info.scanRecord?.let {

                            startActivity(
                                Intent(this@SearchDfuActivity,UpDataDfuActivity::class.java).apply {
                                    putExtra("model",it.deviceName)
                                    putExtra("deviceAddress",info.device.address)
                                }
                            )

                            finish()
                        }

                    }
                    .setConfirmText(getString(R.string.dfu_text04))
                    .bindLayout(R.layout.dialog_message)
                    .show()

            }
        }
    }

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.dfu_search_activity
    }

    override fun initView() {

        //监听蓝牙开启关闭
        registerReceiver(
            bleListenerReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
        )

        with(dfu_search_recycle){
            layoutManager = LinearLayoutManager(this@SearchDfuActivity)
            adapter = mAdapter
        }

    }

    override fun initData() {

        if (mBluetoothAdapter != null){
            isBluetooth = mBluetoothAdapter.isEnabled
        }

        searchLed()

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

                        if (has_data){
                            setDatas(result)
                        }
                    }
                }

            }

            override fun onBatchScanResults(results: List<ScanResult>) {
            }

            override fun onScanFailed(errorCode: Int) {}
        }

    override fun onRightClick(v: View?) {
        super.onRightClick(v)
        stopScan()
        searchLed()
    }

    fun stopScan() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.stopScan(scanCallback)
    }

    fun setDatas(datas : ScanResult){

        dfu_search_recycle.visibility = View.VISIBLE
        not_data_layout.visibility = View.GONE

        if (mAdapter.data.isEmpty()){
            mAdapter.addData(datas)
        }else{

            val has_data = mAdapter.data.any { it is ScanResult &&
                    it.scanRecord!!.deviceName == datas.scanRecord!!.deviceName }
            if (!has_data){
                mAdapter.addData(datas)
            }
        }

    }

    fun searchLed() {

        XXPermissions.with(this)
            .permission(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
            .permission(Permission.Group.LOCATION)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {

                    if (all){

                        if (!isBluetooth){
                            //打开蓝牙
                            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        }else{
                            //开始扫描
                            startScan()
                        }

                    }else{
                        ToastUtils.showShort(R.string.permission_error)
                    }
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {

                    if (never) {
                        ToastUtils.showShort(R.string.permission_error2)
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(this@SearchDfuActivity, permissions)
                    } else {
                        ToastUtils.showShort(R.string.permission_error)
                    }

                }
            })

    }

    class BluetoothMonitorReceiver(val mActivity : SearchDfuActivity) : BroadcastReceiver() {

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
        unregisterReceiver(bleListenerReceiver)
        stopScan()
        super.onDestroy()
    }

}