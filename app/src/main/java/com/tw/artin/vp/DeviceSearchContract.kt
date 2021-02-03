package com.tw.artin.vp

import android.bluetooth.BluetoothDevice
import com.tw.artin.base.BaseInfVp
import no.nordicsemi.android.support.v18.scanner.ScanResult

class DeviceSearchContract {

    interface Presenter : BaseInfVp.BasePresenter {

        //搜索ble
        fun searchBle()

        //停止搜索
        fun stopBle()

    }

    interface View : BaseInfVp.BaseView {

        fun stopBle()

        //扫描结果
        fun onScanResult(result: ScanResult)

    }

}