package com.tw.artin.vp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import com.tw.artin.MainTabActivity2
import com.tw.artin.R
import com.tw.artin.base.BaseInfoData
import com.tw.artin.bean.*
import com.tw.artin.ble.BleMeshManager
import com.tw.artin.ble.BleMeshManagerCallbacks
import com.tw.artin.http.api.*
import com.tw.artin.http.model.HttpData
import com.tw.artin.util.UtilsBigDecimal
import com.tw.artin.view.LinkPopView
import kotlinx.android.synthetic.main.scenes_fragment.*
import no.nordicsemi.android.mesh.*
import no.nordicsemi.android.mesh.provisionerstates.ProvisioningState
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode
import no.nordicsemi.android.mesh.transport.*
import no.nordicsemi.android.support.v18.scanner.*
import okhttp3.Call
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class TotalControl(val mActivity: MainTabActivity2) {

    lateinit var bleMeshManager : BleMeshManager

    lateinit var mMeshManagerApi : MeshManagerApi

    private var mSetupProvisionedNode = false
    //连接对象
    var mSresult : ScanResult? = null

    //设备名称 = 节点名称
    var node_name : String? = null

    private var mProvisionedMeshNode: ProvisionedMeshNode? = null

    private val ATTENTION_TIMER = 5

    // 1新增组 2修改组名 3删除分组  4配网  5试灯  6节点订阅分组
    var operating_mesh = 0
    var operating_group_address = 0

    //是否在配网
    var isContinue = false

    //第一次且没有分组
    var isFirstNotGroup = false

    //选中需要连接数组
    var select_link = mutableListOf<LinkSelect>()

    //状态值用于进度条显示
    private var linkProgress = MutableLiveData<Int>()

    //开启设备时候，是否连接
    var isMainLink = false

    val linkPop by lazy {

        XPopup.Builder(mActivity)
            .hasShadowBg(false)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false)
            .asCustom(
                LinkPopView(mActivity, mActivity, linkProgress).apply {

                    setOnStopListener(object : LinkPopView.StopListener {

                        override fun onResult() {
                            isContinue = false
                        }
                    })

                }
            )
    }

    //初始化
    fun init(){

        initBleMeshManager()

        initMeshManagerApi()

        getNetMeshConfig()

        /*setNetMeshConfig(
            UpLoadMeshConfigApi(
                ""
            ),false
        )*/
    }

    private fun initMeshManagerApi(){

        mMeshManagerApi = MeshManagerApi(mActivity)

        mMeshManagerApi.setMeshManagerCallbacks(object : MeshManagerCallbacks {

            override fun onNetworkLoaded(meshNetwork: MeshNetwork?) {

                //无网络数据，加载本地数据库  上传数据
                meshNetwork?.let {

                    mMeshManagerApi.meshNetwork?.let {

                        if (it.groups.isEmpty()) {
                            //检查是否存在 未分组分组  不存在则创建
                            isFirstNotGroup = true
                            val wfz = mActivity.getString(R.string.device_text05)
                            creatMeshGroup(wfz)
                        }
                    }

                    mActivity.postDelayed({
                        //上传数据，创建默认场景
                        notDataFirst()
                    }, 200)

                }

            }

            override fun onNetworkUpdated(meshNetwork: MeshNetwork?) {

                if (operating_mesh == 1 && isFirstNotGroup) {
                    isFirstNotGroup = false
                    return
                }

                //数据更新 只更新非消息数据
                when (operating_mesh) {

                    //新增 修改组名 删除
                    1, 2, 3 -> upLoadMeshData(true)

                    //试灯
                    5 -> upLoadMeshData(false)


                }
                /*when (operating_mesh) {

                    //组数据  上传
                    1 -> {
                        upLoadDatas()
                    }

                    //试灯  上传
                    2 -> {
                        mActivity.postDelayed({
                            upLoadDatas()
                            dealWithDatas(meshNetwork)
                        }, 1000)
                    }

                    6 -> {
                        //删除节点
                        upLoadDatas()
                    }
                }*/

            }

            override fun onNetworkLoadFailed(error: String?) {
                ToastUtils.showShort(R.string.pop_t_str06)
            }

            override fun onNetworkImported(meshNetwork: MeshNetwork?) {

                if (mMeshManagerApi.meshNetwork?.meshUUID != meshNetwork?.meshUUID) {
                    mMeshManagerApi.deleteMeshNetworkFromDb(mMeshManagerApi.meshNetwork)
                }

                BusUtils.postSticky("init_mesh_success")

                //获取数据，检查场景
                hasDataFirst()
            }

            override fun onNetworkImportFailed(error: String?) {
                ToastUtils.showShort(R.string.pop_t_str05)
            }

            override fun sendProvisioningPdu(meshNode: UnprovisionedMeshNode?, pdu: ByteArray?) {
                bleMeshManager.sendPdu(pdu)
            }

            override fun onMeshPduCreated(pdu: ByteArray?) {
                bleMeshManager.sendPdu(pdu)
            }

            override fun getMtu(): Int {
                return bleMeshManager.maximumPacketSize
            }
        })

        mMeshManagerApi.setProvisioningStatusCallbacks(object : MeshProvisioningStatusCallbacks {

            override fun onProvisioningStateChanged(
                meshNode: UnprovisionedMeshNode?,
                state: ProvisioningState.States?,
                data: ByteArray?
            ) {

                //进度+1
                linkProgress.value = linkProgress.value!! + 1

                meshNode?.let {

                    gxUnicastAddress(it)

                    if (state == ProvisioningState.States.PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING) {

                        if (isCarryOn()) {
                            mMeshManagerApi.setProvisioningAuthentication(
                                "847B5943C0A05F41370A4171E5319ED2"
                            )
                        }

                    } else if (state == ProvisioningState.States.PROVISIONING_CAPABILITIES) {

                        if (mSetupProvisionedNode) {

                            if (isCarryOn()) {

                                //设置节点名称
                                node_name?.let { name ->
                                    it.nodeName = name
                                }

                                mActivity.postDelayed({
                                    mMeshManagerApi.startProvisioningWithStaticOOB(it)
                                }, 500)

                            }
                        }
                    }
                }

            }

            override fun onProvisioningFailed(
                meshNode: UnprovisionedMeshNode?,
                state: ProvisioningState.States?,
                data: ByteArray?
            ) {

                linkPop.dismiss()

                meshNode?.let {

                    gxUnicastAddress(it)
                }

            }

            override fun onProvisioningCompleted(
                meshNode: ProvisionedMeshNode?,
                state: ProvisioningState.States?,
                data: ByteArray?
            ) {

                if (state == ProvisioningState.States.PROVISIONING_COMPLETE) {

                    //进度+1
                    linkProgress.value = linkProgress.value!! + 1

                    if (isCarryOn()) {

                        meshNode?.let {
                            mProvisionedMeshNode = it
                        }

                        bleMeshManager.disconnect().enqueue()

                        //移除新设备
                        BusUtils.postSticky("remove_scan", mSresult!!.device.name)

                        //dealWithDatas(mMeshManagerApi.meshNetwork)

                        stopScan()

                        mActivity.postDelayed(
                            {
                                startScan()
                            }, 800
                        )

                    }

                } else {
                    linkPop.dismiss()
                }

            }

        })

        mMeshManagerApi.setMeshStatusCallbacks(object : MeshStatusCallbacks {

            override fun onTransactionFailed(dst: Int, hasIncompleteTimerExpired: Boolean) {
                mProvisionedMeshNode = mMeshManagerApi.meshNetwork?.getNode(dst)
            }

            override fun onUnknownPduReceived(src: Int, accessPayload: ByteArray?) {
            }

            override fun onBlockAcknowledgementProcessed(dst: Int, message: ControlMessage) {
                mMeshManagerApi.meshNetwork?.getNode(dst)?.let {
                    mProvisionedMeshNode = it
                }
            }

            override fun onBlockAcknowledgementReceived(src: Int, message: ControlMessage) {
                mMeshManagerApi.meshNetwork?.getNode(src)?.let {
                    mProvisionedMeshNode = it
                }
            }

            override fun onMeshMessageProcessed(dst: Int, meshMessage: MeshMessage) {

                mMeshManagerApi.meshNetwork?.getNode(dst)?.let {

                    mProvisionedMeshNode = it

                    if (meshMessage is ConfigCompositionDataGet) {

                        /*statueMessage.postValue(
                            statueToString(ProvisioningState.States.COMPOSITION_DATA_GET_SENT)
                        )*/

                    } else if (meshMessage is ConfigDefaultTtlGet) {

                        /*statueMessage.postValue(
                            statueToString(ProvisioningState.States.SENDING_DEFAULT_TTL_GET)
                        )*/

                    } else if (meshMessage is ConfigAppKeyAdd) {

                        /*statueMessage.postValue(
                            statueToString(ProvisioningState.States.SENDING_APP_KEY_ADD)
                        )*/

                    } else if (meshMessage is ConfigNetworkTransmitSet) {

                        /* statueMessage.postValue(
                             statueToString(ProvisioningState.States.SENDING_NETWORK_TRANSMIT_SET)
                         )*/

                    } else if (meshMessage is ConfigModelSubscriptionDelete) {

                        /*statueMessage.postValue(
                            mActivity.getString(R.string.pop_t_str35)
                        )*/

                    } else if (meshMessage is ConfigModelSubscriptionAdd) {

                        /*statueMessage.postValue(
                            mActivity.getString(R.string.pop_t_str36)
                        )*/

                    } else if (meshMessage is ConfigModelAppBind) {

                        /*statueMessage.postValue(
                            mActivity.getString(R.string.pop_t_str32)
                        )*/


                    } else if (meshMessage is ConfigNodeReset) {

                        /*statueMessage.postValue(
                            mActivity.getString(R.string.pop_t_str37)
                        )*/
                    }

                }

            }

            override fun onMeshMessageReceived(src: Int, meshMessage: MeshMessage) {

                mMeshManagerApi.meshNetwork?.getNode(src)?.let {

                    mProvisionedMeshNode = it

                    when (meshMessage) {

                        is ConfigCompositionDataStatus -> {

                            //进度+1
                            linkProgress.value = linkProgress.value!! + 1

                            if (mSetupProvisionedNode) {

                                mActivity.postDelayed(
                                    {

                                        if (isCarryOn()) {

                                            val configDefaultTtlGet = ConfigDefaultTtlGet()
                                            mMeshManagerApi.createMeshPdu(
                                                it.unicastAddress,
                                                configDefaultTtlGet
                                            )

                                        }


                                    }, 500
                                )
                            } else {

                            }

                        }

                        is ConfigDefaultTtlStatus -> {

                            //进度+1
                            linkProgress.value = linkProgress.value!! + 1

                            if (mSetupProvisionedNode) {

                                mActivity.postDelayed(

                                    {

                                        BaseInfoData.scenes_cur?.let { scenes ->

                                            val scen_key = scenes.applicationKey

                                            mMeshManagerApi.meshNetwork?.let { work ->

                                                work.appKeys.filter { byteArrayToHexStr(it.key) == scen_key }
                                                    .run {

                                                        if (isNotEmpty()) {

                                                            if (isCarryOn()) {

                                                                val configAppKeyAdd =
                                                                    ConfigAppKeyAdd(
                                                                        work.getNetKey(0), get(
                                                                            0
                                                                        )
                                                                    )

                                                                mMeshManagerApi.createMeshPdu(
                                                                    it.unicastAddress,
                                                                    configAppKeyAdd
                                                                )

                                                            }
                                                        }

                                                    }
                                            }

                                        }

                                    }, 500
                                )

                            } else {

                            }

                        }

                        is ConfigAppKeyStatus -> {

                            if (mSetupProvisionedNode) {

                                if (meshMessage.isSuccessful) {

                                    //进度+1
                                    linkProgress.value = linkProgress.value!! + 1


                                    mActivity.postDelayed(

                                        {

                                            if (isCarryOn()) {

                                                val networkTransmitSet =
                                                    ConfigNetworkTransmitSet(2, 1)

                                                mMeshManagerApi.createMeshPdu(
                                                    it.unicastAddress,
                                                    networkTransmitSet
                                                )

                                            }

                                        }, 500
                                    )
                                } else {

                                }
                            } else {

                            }

                        }

                        is ConfigNetworkTransmitStatus -> {

                            //配网完成，然后配onoff
                            if (mSetupProvisionedNode) {
                                mSetupProvisionedNode = false

                                if (bleMeshManager.isProvisioningComplete) {

                                    //进度+1
                                    linkProgress.value = linkProgress.value!! + 1
                                    LogUtils.d("finish =${linkProgress.value}")

                                    if (isCarryOn()) {

                                        //查看是否还有别的需要连接的设备
                                        val lsIt = getNextLinkData()

                                        if (lsIt == null) {

                                            linkPop.dismiss()
                                            //完成配网

                                            select_link.clear()

                                            upLoadMeshData(true)

                                            operating_mesh = 0

                                            //获取心跳包
                                            mActivity.postDelayed(Runnable {
                                                getHeartbeat()
                                            },100)

                                        } else {

                                            stopScan()

                                            BusUtils.postSticky("deal_with")

                                            connetBle(lsIt.scan_datas, true)

                                        }

                                    } else {

                                    }

                                } else {

                                }
                            } else {

                            }
                        }

                        is LightCtlStatus -> {

                            val ctlData = mutableMapOf<String, Float>()

                            val m1 = UtilsBigDecimal.div(
                                meshMessage.presentLightness.toDouble(),
                                655.35
                            )
                            val m2 = UtilsBigDecimal.GetFInt2(m1)
                            ctlData.put("lightness", m2)

                            ctlData.put("temperature", meshMessage.presentTemperature.toFloat())

                            val bd01 = UtilsBigDecimal.div(
                                meshMessage.presentDeltaUv.toDouble(),
                                32768.0
                            )
                            val bd02 = UtilsBigDecimal.mul(bd01, 50.0)
                            val bd03 = UtilsBigDecimal.GetFInt(bd02)
                            ctlData.put("deltauv", bd03)

                            BusUtils.postSticky("getCctData", ctlData)

                        }

                        is LightHslStatus -> {

                            val hsiData = mutableMapOf<String, Float>()

                            val m1 = UtilsBigDecimal.div(
                                meshMessage.presentLightness.toDouble(),
                                655.35
                            )
                            val m2 = UtilsBigDecimal.GetFInt2(m1)
                            hsiData.put("lightness", m2)

                            val bd01 = UtilsBigDecimal.div(
                                meshMessage.presentHue.toDouble(),
                                182.0416
                            )
                            hsiData.put("hue", bd01.toFloat())

                            val bd02 = UtilsBigDecimal.div(
                                meshMessage.presentSaturation.toDouble(),
                                65535.toDouble()
                            )
                            hsiData.put("sat", bd02.toFloat())

                            BusUtils.postSticky("getHsiData", hsiData)

                        }

                        is ConfigNodeResetStatus -> {

                            /*bleMeshManager.setClearCacheRequired()

                            if (operating_mesh == 9) {

                                //删除场景中节点
                                mMeshManagerApi.exportMeshNetwork()?.let { json ->

                                    setNetMeshConfig2(
                                        UpLoadMeshConfigApi(json)
                                    )

                                }

                            } else {

                            }*/

                        }

                        is GenericOnOffStatus -> {

                            //cct 操作
                            /*if (operating_index == 0) {

                                BusUtils.postSticky(
                                    "cctOnOff",
                                    mutableMapOf(
                                        "datas" to operatingDatas,
                                        "onoff" to meshMessage.presentState.toString()
                                    )
                                )

                            } else if (operating_index == 1) {
                                //hsi
                                BusUtils.postSticky(
                                    "hsiOnOff",
                                    mutableMapOf(
                                        "datas" to operatingDatas,
                                        "onoff" to meshMessage.presentState.toString()
                                    )
                                )

                            } else {

                                //effect
                                BusUtils.postSticky(
                                    "EffectOnOff",
                                    mutableMapOf(
                                        "datas" to operatingDatas,
                                        "onoff" to meshMessage.presentState.toString()
                                    )
                                )
                            }*/

                        }

                        is HeartBeatStatus ->{

                            setHeart(src, meshMessage)


                        }

                        is VendorModelMessageStatus -> {

                            LogUtils.d("VendorModelMessageStatus =${it.unicastAddress}")

                            //获取特效状态
                        }

                        is ConfigModelSubscriptionStatus -> {
                            //订阅分组状态

                            //订阅组
                            if (operating_mesh == 6){

                                upLoadMeshData(true)
                                operating_mesh = 0
                            }else{

                            }

                        }

                        is ConfigModelAppStatus -> {

                            //绑定钥匙成功
                        }

                        else -> {

                        }
                    }


                }

            }

            override fun onMessageDecryptionFailed(meshLayer: String?, errorMessage: String?) {
            }

        })

    }

    private fun setHeart(
        src: Int,
        meshMessage: HeartBeatStatus
    ): Any {

        //心跳
        if (BaseInfoData.onlines.isEmpty()) {
            BaseInfoData.onlines.add(
                CheckOnLine(
                    src,
                    System.currentTimeMillis()
                )
            )
        } else {

            val hasData = BaseInfoData.onlines.any { it.address == src }

            if (hasData) {

                BaseInfoData.onlines.forEach {
                    if (it.address == src) {
                        it.time = System.currentTimeMillis()
                    }
                }

            } else {
                BaseInfoData.onlines.add(
                    CheckOnLine(
                        src,
                        System.currentTimeMillis()
                    )
                )
            }
        }

        BusUtils.postSticky("HeartEvent",src)

        //给设备详情显示
        return if (BaseInfoData.device_info.isEmpty()) {
            BaseInfoData.device_info.add(
                HeartBean(
                    src,
                    meshMessage.getmElectricQuantity(),
                    meshMessage.getmRemainingTime(),
                    meshMessage.getmTem()
                )
            )
        } else {
            val hasData = BaseInfoData.device_info.any { it.address == src }

            if (hasData) {

                BaseInfoData.device_info.forEach {
                    if (it.address == src) {
                        it.electricQuantity = meshMessage.getmElectricQuantity()
                        it.remainingTime = meshMessage.getmRemainingTime()
                        it.mTem = meshMessage.getmTem()
                    }
                }

            } else {
                BaseInfoData.device_info.add(
                    HeartBean(
                        src,
                        meshMessage.getmElectricQuantity(),
                        meshMessage.getmRemainingTime(),
                        meshMessage.getmTem()
                    )
                )
            }
        }

    }

    fun initBleMeshManager(){

        bleMeshManager = BleMeshManager(mActivity)

        bleMeshManager.setGattCallbacks(object : BleMeshManagerCallbacks {
            override fun onDeviceConnecting(device: BluetoothDevice) {
                /*linkMessage.postValue(0)
                linkPop.show()*/
            }

            override fun onDeviceConnected(device: BluetoothDevice) {
            }

            override fun onDeviceDisconnecting(device: BluetoothDevice) {
            }

            override fun onDeviceDisconnected(device: BluetoothDevice) {
                mMeshManagerApi.meshNetwork?.proxyFilter = null
                isMainLink = false
                if (!mSetupProvisionedNode){
                    ToastUtils.showShort(R.string.link_dk)
                    startScan()
                }

            }

            override fun onLinkLossOccurred(device: BluetoothDevice) {
            }

            override fun onServicesDiscovered(
                device: BluetoothDevice,
                optionalServicesFound: Boolean
            ) {
            }

            override fun onDeviceReady(device: BluetoothDevice) {

                if (bleMeshManager.isProvisioningComplete) {

                    //进度+1
                    if (linkPop.isShow) {
                        linkProgress.postValue(linkProgress.value!! + 1)
                    }


                    if (mSetupProvisionedNode) {

                        mMeshManagerApi.meshNetwork?.let {

                            if (it.selectedProvisioner.provisionerAddress != null) {

                                //配置节点预设信息
                                mProvisionedMeshNode?.let {

                                    if (isCarryOn()) {

                                        mMeshManagerApi
                                            .createMeshPdu(
                                                it.unicastAddress,
                                                ConfigCompositionDataGet()
                                            )
                                    }
                                }

                            } else {
                                mSetupProvisionedNode = false
                            }

                        }


                    } else {

                        //连接已配网设备
                        isMainLink = true
                        LinkView.dismiss()
                        //获取心跳包
                        //mMeshManagerApi.createMeshPdu(2,LightCtlGet(mMeshManagerApi.meshNetwork?.getAppKey(3)!!))
                        mActivity.postDelayed(Runnable {
                            getHeartbeat()
                        },100)


                    }

                } else {

                    //进度+1
                    if (linkPop.isShow) {
                        linkProgress.postValue(linkProgress.value!! + 1)
                    }

                    mSetupProvisionedNode = true
                    //亮灯验证节点
                    mSresult?.let {

                        getServiceData(it, BleMeshManager.MESH_PROVISIONING_UUID)?.let { ba ->

                            node_name = device.name

                            val uuid = mMeshManagerApi.getDeviceUuid(ba)

                            if (isCarryOn()) {
                                mMeshManagerApi.identifyNode(uuid, ATTENTION_TIMER)
                            }
                        }
                    }
                }

            }

            override fun onBondingRequired(device: BluetoothDevice) {
            }

            override fun onBonded(device: BluetoothDevice) {
            }

            override fun onBondingFailed(device: BluetoothDevice) {
            }

            override fun onError(device: BluetoothDevice, message: String, errorCode: Int) {
            }

            override fun onDeviceNotSupported(device: BluetoothDevice) {
            }

            override fun onDataReceived(
                bluetoothDevice: BluetoothDevice?,
                mtu: Int,
                pdu: ByteArray?
            ) {
                pdu?.let {
                    mMeshManagerApi.handleNotifications(mtu, it)
                }
            }

            override fun onDataSent(device: BluetoothDevice?, mtu: Int, pdu: ByteArray?) {
                pdu?.let {
                    mMeshManagerApi.handleWriteCallbacks(mtu, it)
                }
            }
        })
    }

    fun gxUnicastAddress(meshNode: UnprovisionedMeshNode){

        meshNode.provisioningCapabilities?.let {

            mMeshManagerApi.meshNetwork?.let { work ->

                val elementCount: Int = it.numberOfElements.toInt()

                val provisioner: Provisioner = work.selectedProvisioner

                val unicast = work.nextAvailableUnicastAddress(elementCount, provisioner)

                work.assignUnicastAddress(unicast)

            }
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

    fun getNotGroupDeviceGroups() : MutableList<NewDeviceGroupBean>?{

        mMeshManagerApi.meshNetwork?.let { work ->

            val groups = mutableListOf<NewDeviceGroupBean>()

            work.groups.forEach {

                val g_info01 = NewDeviceGroupBean()
                if (it.address == 49152){
                    //未分组
                    g_info01.address = it.address.toString()
                    g_info01.dgName = it.name
                    g_info01.isUngrouped = true

                    groups.add(g_info01)

                    return@forEach
                }
            }

            if (groups.isNotEmpty()){
                return groups
            }

        }

        return null
    }

    fun byteArrayToHexStr(bytes: ByteArray): String? {
        var strHex: String
        val sb = StringBuilder()
        for (aByte in bytes) {
            strHex = Integer.toHexString(aByte.toInt() and 0xFF)
            sb.append("").append(if (strHex.length == 1) "0" else "")
                .append(strHex) // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().toUpperCase(Locale.ROOT)
    }

    fun hasDataFirst(){

        //查询是否存在场景
        EasyHttp.post(mActivity)
            .api(ScenesListApi())
            .request(object : HttpCallback<HttpData<ScenesListApi.ScenesBean>>(mActivity) {

                override fun onStart(call: Call?) {
                }

                override fun onSucceed(result: HttpData<ScenesListApi.ScenesBean>?) {
                    super.onSucceed(result)

                    result?.objx?.let {

                        if (it.content.isEmpty()) {

                            creatScenes01(false)

                        } else {

                            it.content.forEach {
                                if (it.isCurrent) {
                                    BaseInfoData.scenes_cur = it

                                    //开启连接
                                    if (mActivity.isBluetooth) {
                                        startScan()
                                    }
                                    BusUtils.postSticky("deal_with")
                                }
                            }
                        }

                    } ?: creatScenes01(false)

                }

            })

    }

    fun notDataFirst(){

        mMeshManagerApi.exportMeshNetwork()?.let { json ->

            //上传mesh数据
            EasyHttp.post(mActivity)
                .api(
                    UpLoadMeshConfigApi(json)
                )
                .request(object : HttpCallback<HttpData<String>>(mActivity) {

                    override fun onSucceed(result: HttpData<String>?) {

                        //查询是否存在场景
                        EasyHttp.post(mActivity)
                            .api(ScenesListApi())
                            .request(object : HttpCallback<HttpData<ScenesListApi.ScenesBean>>(
                                mActivity
                            ) {

                                override fun onStart(call: Call?) {
                                }

                                override fun onSucceed(result: HttpData<ScenesListApi.ScenesBean>?) {
                                    super.onSucceed(result)

                                    result?.objx?.let {

                                        if (it.content.isEmpty()) {

                                            creatScenes01(true)
                                        } else {

                                            it.content.forEach {
                                                if (it.isCurrent) {
                                                    BaseInfoData.scenes_cur = it
                                                    BusUtils.postSticky("init_mesh_success")
                                                }
                                            }
                                        }

                                    } ?: creatScenes01(true)

                                }

                            })


                    }

                    override fun onStart(call: Call?) {
                    }
                })

        }
    }

    fun creatScenes01(isFirst: Boolean){

        mMeshManagerApi.meshNetwork?.let {

            val appkey = it.createAppKey()
            //绑定network
            appkey.boundNetKeyIndex = 0

            val api = AddScenesApi2(
                mActivity.getString(R.string.scene_t01),
                true,
                byteArrayToHexStr(appkey.key)!!,
                getNotGroupDeviceGroups()
            )

            if(it.addAppKey(appkey)){

                EasyHttp.post(mActivity)
                    .api(api)
                    .request(object : HttpCallback<HttpData<JSONObject>>(mActivity) {

                        override fun onSucceed(result: HttpData<JSONObject>?) {
                            //初始化完成

                            if (!isFirst) {
                                BusUtils.postSticky("deal_with")
                            } else {

                                //第一次的时候
                                BusUtils.postSticky("init_mesh_success")
                            }
                        }

                        override fun onStart(call: Call?) {
                        }
                    })
            }
        }
    }

    //更新mesh数据
    fun upLoadMeshData(has2Ui: Boolean){

        mMeshManagerApi.exportMeshNetwork()?.let { json ->

            setNetMeshConfig(
                UpLoadMeshConfigApi(
                    json
                ),
                has2Ui
            )
        }
    }


    //处理场景数据
    fun ChuLiAllData01(){

        mMeshManagerApi.meshNetwork?.let { work ->

            BaseInfoData.scenes_cur?.let { scenes ->

                val dGroup = scenes.deviceGroups

                //用于存储已存在的id
                val ids = mutableListOf<GroupID>()

                dGroup.forEach {

                    val dat01 = GroupID()
                    dat01.gId = it.id
                    dat01.gAddress = it.address

                    val dat02 = mutableListOf<DevID>()
                    it.devices.forEach {

                        dat02.add(

                            DevID(
                                dAddress = it.address,
                                dId = it.id,
                                isIsPowserOn = it.isIsPowserOn,
                                lightness = it.lightness,
                                effect = it.effect,
                                preset = it.preset,
                                deltaUV = it.deltaUV,
                                temperature = it.temperature,
                                hue = it.hue,
                                saturation = it.saturation,
                                deviceType = it.deviceType,
                                currentDeviceType = it.currentDeviceType
                            )
                        )
                    }
                    dat01.devId = dat02

                    ids.add(dat01)
                }

                //清空原来数据
                dGroup.clear()

                //获取能用节点
                val nodes_list = mutableListOf<ProvisionedMeshNode>()
                //节点
                val mNodes = work.nodes
                mNodes.forEach {

                    work.selectedProvisioner?.let { pro ->

                        if (it.uuid != pro.provisionerUuid) {
                            nodes_list.add(it)
                        }
                    }
                }

                //删除分组
                if (operating_mesh == 3){
                    val iter = ids.iterator()
                    while (iter.hasNext()){
                        val nIt = iter.next()
                        if (nIt.gAddress == operating_group_address.toString()){
                            iter.remove()
                        }
                    }
                }

                val groups = mutableListOf<NewDeviceGroupBean>()

                work.groups.forEach { itGroup ->

                    val mAddress01 = itGroup.address

                    ids.forEach { Idit ->

                        if (Idit.gAddress == mAddress01.toString()){

                            val g_info01 = NewDeviceGroupBean()

                            g_info01.address = mAddress01.toString()
                            g_info01.dgName = itGroup.name
                            g_info01.id = Idit.gId

                            if (itGroup.address == 49152){
                                //未分组
                                g_info01.isUngrouped = true
                            }

                            g_info01.devices = getDeviceItemData(
                                nodes_list,
                                work,
                                itGroup.address,
                                Idit.devId
                            )

                            groups.add(g_info01)

                        }
                    }
                }

                //创建分组
                if (operating_mesh == 1){
                    work.groups.filter { it.address == operating_group_address }.run {
                        if (isNotEmpty()){

                            val creatData = get(0)

                            val g_info01 = NewDeviceGroupBean()

                            g_info01.address = creatData.address.toString()
                            g_info01.dgName = creatData.name

                            if (creatData.address == 49152){
                                //未分组
                                g_info01.isUngrouped = true
                            }

                            g_info01.devices = getDeviceItemData(
                                nodes_list,
                                work,
                                creatData.address,
                                null
                            )


                            groups.add(g_info01)

                        }
                    }

                }

                scenes.deviceGroups = groups

                upDataScenes()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun getDeviceItemData(
        nodes_list: MutableList<ProvisionedMeshNode>,
        mMeshNetwork: MeshNetwork, mAddress: Int, devId: MutableList<DevID>?
    )
            : MutableList<NewDeviceGroupBean.DevicesBean> {

        val d_list = mutableListOf<NewDeviceGroupBean.DevicesBean>()

        if (nodes_list.isNotEmpty()){

            nodes_list.forEach {

                val e01 = it.elements

                if (e01.isNotEmpty()){

                    val element = e01[e01.keys.hashCode()]

                    element?.meshModels?.values?.let { meshModels ->

                        val isSize = element.meshModels.size
                        val mDeviceType = if (isSize == 6){
                            1 //hsi = 1
                        }else{
                            2 //cct=2
                        }

                        //hsi = 1、  cct=2、 eff=3、hcct = 4

                        val onOff = meshModels.filter { it.modelName == "Generic On Off Server" }

                        if (onOff.isNotEmpty()){

                            val datass = onOff[0]

                            //没有订阅节点  就是未分组
                            if (datass.subscribedAddresses.isEmpty() && mAddress == 49152){

                                val d01 = NewDeviceGroupBean.DevicesBean().apply {
                                    name = it.nodeName
                                    orginName = it.nodeName
                                    address = it.unicastAddress
                                    isIsPowserOn = false
                                    lightness = 0
                                    effect = 0
                                    preset = 0
                                    deltaUV = 0
                                    temperature = 0
                                    hue = 0
                                    saturation = 0
                                    deviceType = mDeviceType
                                    currentDeviceType = 1
                                }

                                devId?.run {
                                    forEach {
                                        if (it.dAddress == element.elementAddress){
                                            d01.id = it.dId
                                            d01.isIsPowserOn = it.isIsPowserOn
                                            d01.lightness = it.lightness
                                            d01.effect = it.effect
                                            d01.preset = it.preset
                                            d01.deltaUV = it.deltaUV
                                            d01.temperature = it.temperature
                                            d01.hue = it.hue
                                            d01.saturation = it.saturation
                                            d01.deviceType = it.deviceType
                                            d01.currentDeviceType = it.currentDeviceType
                                            return@run
                                        }
                                    }
                                }

                                d_list.add(d01)

                            }else{


                                datass.subscribedAddresses.forEachIndexed { index, i ->

                                    if (i == mAddress ){

                                        val d01 = NewDeviceGroupBean.DevicesBean().apply {
                                            name = it.nodeName
                                            deviceid = it.uuid
                                            orginName = it.nodeName
                                            address = it.unicastAddress
                                            isIsPowserOn = false
                                            lightness = 0
                                            effect = 0
                                            preset = 0
                                            deltaUV = 0
                                            temperature = 0
                                            hue = 0
                                            saturation = 0
                                            deviceType = mDeviceType
                                            currentDeviceType = 1
                                        }

                                        devId?.run {
                                            forEach {
                                                if (it.dAddress == element.elementAddress){
                                                    d01.id = it.dId
                                                    d01.isIsPowserOn = it.isIsPowserOn
                                                    d01.lightness = it.lightness
                                                    d01.effect = it.effect
                                                    d01.preset = it.preset
                                                    d01.deltaUV = it.deltaUV
                                                    d01.temperature = it.temperature
                                                    d01.hue = it.hue
                                                    d01.saturation = it.saturation
                                                    d01.deviceType = it.deviceType
                                                    d01.currentDeviceType = it.currentDeviceType
                                                    return@run
                                                }
                                            }
                                        }

                                        d_list.add(d01)
                                    }

                                }
                            }
                        }
                    }
                }
            }

        }

        return d_list
    }

    //创建分组
    fun creatMeshGroup(name: String){

        operating_mesh = 1

        mMeshManagerApi.meshNetwork?.let {

            val group = it.createGroup(it.selectedProvisioner, name)
            operating_group_address = group.address

            it.addGroup(group)
        }
    }

    //修改名称
    fun editGroupName(name: String, address: Int){

        operating_mesh = 2

        operating_group_address = address

        mMeshManagerApi.meshNetwork?.let {

            it.getGroup(address)?.let { g_it ->
                g_it.name = name
                it.updateGroup(g_it)
            }
        }
    }

    //移除组
    fun removeGroup(address: Int){

        //操作更新组
        operating_mesh = 3
        operating_group_address = address

        mMeshManagerApi.meshNetwork?.let {

            val mGroup = it.getGroup(address)
            if (it.isGroupExist(mGroup)){
                it.removeGroup(mGroup)
            }
        }
    }

    fun getLinkData() : LinkSelect?{
        return select_link.lastOrNull { it.isLink == true }
    }

    //重连框
    val LinkView by lazy {
        XPopup.Builder(mActivity)
            .hasShadowBg(false)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false)
            .asCustom(
                LoadingPopupView(mActivity).apply {
                    setTitle(mActivity.getString(R.string.pop_t_str01))
                }
            )
    }

    fun connetBleLink(result: ScanResult){

        mSresult = result

        initBleMeshManager()

        LinkView.show()

        mActivity.postDelayed({

            bleMeshManager.connect(result.device).retry(3, 200).enqueue()

        }, 300)

    }


    //配网连接设备
    fun connetBle(result: ScanResult, isFirst: Boolean){

        mSresult = result

        initBleMeshManager()

        mActivity.postDelayed({

            //第一次连接出现进度条
            if (isFirst) {
                //显示进度条
                linkProgress.postValue(0)
                linkPop.show()
            }

            //继续配网
            isContinue = true

            bleMeshManager.connect(result.device).retry(3, 200).enqueue()

        }, 300)

    }

    fun stopScan() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.stopScan(scanCallback)
    }

    fun startScan() {
        // Scanning settings
        val settings =
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // Refresh the devices list every second
                .setReportDelay(0) // Hardware filtering has some issues on selected devices
                .setUseHardwareFilteringIfSupported(false) // Samsung S6 and S6 Edge report equal value of RSSI for all devices. In this app we ignore the RSSI.
                .build()

        val filters: MutableList<ScanFilter> =
            ArrayList()

        filters.add(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(BleMeshManager.MESH_PROXY_UUID)).build()
        )

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

                result.scanRecord?.let {

                    getServiceData(result, BleMeshManager.MESH_PROXY_UUID)?.let { serviceData ->

                        if (mMeshManagerApi.isAdvertisedWithNodeIdentity(serviceData)) {

                            stopScan()

                            mProvisionedMeshNode?.let {

                                if (mMeshManagerApi.nodeIdentityMatches(it, serviceData)) {

                                    mSetupProvisionedNode = true

                                    mActivity.postDelayed(
                                        {
                                            connetBle(result, false)
                                        },
                                        800
                                    )
                                }
                            }

                        }else{

                            val dName = it.deviceName

                            mMeshManagerApi.meshNetwork?.let { work ->

                                val has_device = work.nodes.any { it.nodeName == dName}

                                //检查本地是否有该设备
                                if (has_device){

                                    stopScan()

                                    //重新连接
                                    mSetupProvisionedNode = false

                                    mActivity.postDelayed(
                                        {
                                            connetBleLink(result)
                                        },
                                        800
                                    )
                                }

                            }

                        }
                    }
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
            }
            override fun onScanFailed(errorCode: Int) {}
        }

    fun getNextLinkData() : LinkSelect?{

        select_link.firstOrNull { it.isLink == false }?.let {

            it.isLink = true

            return getLinkData()

        } ?: return null

    }

    //停止配网
    fun isCarryOn() : Boolean{

        if (!isContinue){

            mSetupProvisionedNode = false

            select_link.clear()

            operating_mesh = 0

            if (linkProgress.value!! > 1){
                //重置设备
                mProvisionedMeshNode?.unicastAddress?.let {
                    mMeshManagerApi.createMeshPdu(it, ConfigNodeReset())
                }
            }

            bleMeshManager.disconnect()

            stopScan()

            return false
        }

        return true
    }

    //获取心跳
    fun getHeartbeat(){

        mMeshManagerApi.meshNetwork?.let {

            BaseInfoData.scenes_cur?.let { baseIt ->

                val key = baseIt.applicationKey

                it.appKeys.filter { byteArrayToHexStr(it.key) == key}.run {

                    if (isNotEmpty()){

                        //获取能用节点
                        val nodes_list = mutableListOf<ProvisionedMeshNode>()
                        //节点
                        val mNodes = it.nodes
                        mNodes.forEach {pm ->

                            it.selectedProvisioner?.let { pro ->

                                if (pm.uuid != pro.provisionerUuid) {
                                    nodes_list.add(pm)
                                }
                            }
                        }

                        nodes_list.forEach {

                            val message = VendorModelMessageAcked(
                                get(0)!!,
                                0x1887,
                                0x0059,
                                0x14 or 0xC0,
                                byteArrayOf()
                            )

                            //mMeshManagerApi.createMeshPdu(it.unicastAddress, HeartBeatGet(get(0)!!))
                            mMeshManagerApi.createMeshPdu(it.unicastAddress, message)
                        }

                        //提交数据
                        mMeshManagerApi.exportMeshNetwork()?.let {

                            setNetMeshConfig2(
                                UpLoadMeshConfigApi(
                                    it
                                )
                            )
                        }

                    }
                }
            }
        }

    }


    //检查灯光
    fun opneLight(address: Int){

        mMeshManagerApi.meshNetwork?.let {

            BaseInfoData.scenes_cur?.let { baseIt ->

                val key = baseIt.applicationKey

                it.appKeys.filter { byteArrayToHexStr(it.key) == key}.run {

                    if (isNotEmpty()){

                        val message = VendorModelMessageUnacked(
                            get(0)!!,
                            0x1888,
                            0x0059,
                            0x09,
                            getCheckParameter()
                        )

                        operating_mesh = 5

                        mMeshManagerApi.createMeshPdu(address, message)

                    }

                }
            }
        }
    }

    fun getCheckParameter() : ByteArray{

        val mBu = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
        mBu.put(1)
        mBu.put(BaseInfoData.getNextTid().toByte())

        return mBu.array()
    }


    //节点添加到分组
    @SuppressLint("RestrictedApi")
    fun AddGroupNode(address: Int, unAddress: Int){

        mMeshManagerApi.meshNetwork?.let { mWork ->

            val node = mWork.getNode(unAddress)

            node.elements?.run {

                if (isNotEmpty()){
                    val element = get(keys.hashCode())

                    element?.meshModels?.values?.let { meshModel ->

                        val onOff = meshModel.filter { it.modelName == "Generic On Off Server" }

                        if (onOff.isNotEmpty()){

                            val key01 = node.addedAppKeys

                            if (key01.isNotEmpty()){

                                mMeshManagerApi.meshNetwork?.appKeys?.forEach {

                                    if (key01[0].index == it.keyIndex){

                                        val group = mWork.getGroup(address)

                                        val message : MeshMessage

                                        if (group != null){

                                            //新增节点到组
                                            operating_mesh = 6

                                            if (group.addressLabel == null){

                                                message = ConfigModelSubscriptionAdd(
                                                    element.elementAddress,
                                                    group.address, onOff[0].modelId
                                                )

                                            }else{

                                                message = ConfigModelSubscriptionVirtualAddressAdd(
                                                    element.elementAddress,
                                                    group.addressLabel!!, onOff[0].modelId
                                                )

                                            }

                                            //添加分组
                                            mMeshManagerApi.createMeshPdu(
                                                node.unicastAddress,
                                                message
                                            )
                                        }

                                        return
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    fun onDestroy(){
        bleMeshManager.disconnect()
        bleMeshManager.close()
    }


    /****网络请求部分****/

    fun getNetMeshConfig(){

        EasyHttp.post(mActivity)
            .api(GetMeshConfigApi())
            .request(object : HttpCallback<HttpData<GetMeshConfigApi.MConfigBean>>(mActivity) {

                override fun onSucceed(result: HttpData<GetMeshConfigApi.MConfigBean>?) {
                    super.onSucceed(result)

                    result?.objx?.let {

                        val config = it.meshConfig
                        if (config == "") {
                            mMeshManagerApi.loadMeshNetwork()
                        } else {
                            mMeshManagerApi.importMeshNetworkJson(config)
                        }

                    } ?: mMeshManagerApi.loadMeshNetwork()
                }
            })
    }

    fun setNetMeshConfig(api: UpLoadMeshConfigApi, has2Ui: Boolean){

        EasyHttp.post(mActivity)
            .api(api)
            .request(object : HttpCallback<HttpData<String>>(mActivity) {

                override fun onSucceed(result: HttpData<String>?) {

                    //更新场景数据
                    mActivity.postDelayed({

                        ChuLiAllData01()

                        operating_mesh = 0

                        if (has2Ui) {
                            BusUtils.postSticky("deal_with")
                        }

                    }, 150)

                }

                override fun onFail(e: Exception?) {
                    operating_mesh = 0
                }

                override fun onStart(call: Call?) {
                }
            })

    }

    fun setNetMeshConfig2(api: UpLoadMeshConfigApi){

        EasyHttp.post(mActivity)
            .api(api)
            .request(object : HttpCallback<HttpData<String>>(mActivity) {

                override fun onSucceed(result: HttpData<String>?) {

                    //更新场景数据
                    mActivity.postDelayed({

                        operating_mesh = 0

                    }, 150)

                }

                override fun onFail(e: Exception?) {
                    operating_mesh = 0
                }

                override fun onStart(call: Call?) {
                }
            })

    }

    fun upDataScenes(){

        BaseInfoData.scenes_cur?.let {

            EasyHttp.post(mActivity)
                .api(
                    UpDataNScenesApi(
                        id = it.id,
                        scenesName = it.scenesName,
                        isCurrent = it.isCurrent,
                        applicationKey = it.applicationKey,
                        deviceGroups = it.deviceGroups
                    )
                )
                .request(object : HttpCallback<HttpData<JSONObject>>(mActivity) {

                    override fun onSucceed(result: HttpData<JSONObject>?) {
                    }

                    override fun onStart(call: Call?) {
                    }
                })

        }
    }

    /****网络请求部分****/


}