package com.tw.artin.vp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.CountDownTimer
import android.os.ParcelUuid
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.tw.artin.R
import com.tw.artin.base.BaseInfoData
import com.tw.artin.bean.DevicesBean
import com.tw.artin.ble.BleMeshManager
import com.tw.artin.ui.adapter.DeviceListAdapter
import com.tw.artin.ui.fragment.DeviceFragment
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*

class DeviceFragmentPresenter (
    val mFragment: DeviceFragment
){

    val scanner by lazy {
        BluetoothLeScannerCompat.getScanner()
    }

    val settings by lazy {
        ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .setUseHardwareFilteringIfSupported(false)
            .build()
    }

    val scallBack = object : ScanCallback(){

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            onResult(result)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            ToastUtils.showShort(R.string.device_scan_error)
            stopBle()
        }
    }

    fun getServiceData(
        result: ScanResult,
        serviceUuid: UUID
    ): ByteArray? {
        val scanRecord =
            result.scanRecord
        return scanRecord?.getServiceData(ParcelUuid(serviceUuid))
    }

    fun searchBle() {

        XXPermissions.with(mFragment)
            .permission(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
            .permission(Permission.Group.LOCATION)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {

                    if (all){

                        if (!mFragment.getAttachActivity().isBluetooth){
                            //打开蓝牙
                            mFragment.getAttachActivity().startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        }else{
                            //开始扫描
                            starSearchBle()
                        }

                    }else{
                        ToastUtils.showShort(R.string.permission_error)
                    }
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {

                    if (never) {
                        ToastUtils.showShort(R.string.permission_error2)
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(mFragment, permissions)
                    } else {
                        ToastUtils.showShort(R.string.permission_error)
                    }

                }
            })

    }

    fun starSearchBle(){

        val info = mFragment.mAdapter.data[0]

        if (!info.isSearch!!){
            info.isSearch = true
            mFragment.mAdapter.notifyItemChanged(0)
        }

        scanner.startScan(
            mutableListOf(
                ScanFilter.Builder().setServiceUuid(
                    ParcelUuid(BleMeshManager.MESH_PROVISIONING_UUID)
                ).build()
            ),
            settings,
            scallBack
        )

    }

    fun stopBle() {

        scanner.stopScan(scallBack)

        mFragment.let {
            val info = it.mAdapter.getItem(0)
            info.isSearch = false
            it.mAdapter.notifyItemChanged(0)
        }

    }

    fun onResult(result: ScanResult) {

        mFragment.run {

            mAdapter.data.filter { it.type == 3
                    && it.note_name == result.device.name}.let {

                if (it.isEmpty()){
                    return@let
                }

                /*if (getAttachActivity()!= null){
                    getAttachActivity().shearControl.delNote(it[0].unicastAddress!!)
                }*/
            }

            mAdapter.data.filter { it.type == 1 }.let {d_it ->

                if (d_it.isEmpty()){

                    mAdapter.addData(1,
                        DevicesBean(type = 1, scan_datas = result, isSelect = false,is_scan = true)
                    )

                }else{

                    val hasany = d_it.any { it.scan_datas?.device?.address == result.device.address}

                    if (!hasany){

                        mAdapter.addData(1,
                            DevicesBean(type = 1, scan_datas = result, isSelect = false,is_scan = true)
                        )

                    }
                }
            }
        }
    }

    var isOpenTimer = false

    fun HeartChuLi(mAddress : Int){

        mFragment.mAdapter.data.forEachIndexed { index, devicesBean ->

            if (devicesBean.type == 3 &&
                mAddress == devicesBean.unicastAddress){

                if (!devicesBean.isonline!!){
                    mFragment.post(Runnable {
                        devicesBean.isonline = true
                        mFragment.mAdapter.notifyItemChanged(index)
                    })
                }
            }
        }

        if (!isOpenTimer){

            isOpenTimer = true

            Timer().schedule(object : TimerTask(){
                override fun run() {

                    val offLines = mutableListOf<Int>()

                    mFragment.mAdapter.data.forEachIndexed { index, devicesBean ->

                        //节点部分
                        if (devicesBean.type == 3){

                            BaseInfoData.onlines.forEach { onIt ->

                                if (onIt.address == devicesBean.unicastAddress) {

                                    val cz = (System.currentTimeMillis() - onIt.time) / 10000

                                    LogUtils.d("掉线 $cz")

                                    //掉线
                                    if (cz > 1) {
                                        offLines.add(onIt.address)
                                        devicesBean.isonline = false
                                        mFragment.post({
                                            mFragment.mAdapter.notifyItemChanged(index)
                                        })

                                    }

                                }

                            }

                        }
                    }

                    if (offLines.isNotEmpty()){

                        val iter = BaseInfoData.onlines.iterator()

                        while (iter.hasNext()){
                            val info = iter.next()
                            val hasd = offLines.any { it == info.address }
                            if (hasd){
                                iter.remove()
                            }
                        }
                    }


                }
            },0,22000)

        }
    }

}