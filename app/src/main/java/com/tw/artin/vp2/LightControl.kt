package com.tw.artin.vp2

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
import no.nordicsemi.android.mesh.*
import no.nordicsemi.android.mesh.provisionerstates.ProvisioningState
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode
import no.nordicsemi.android.mesh.transport.*
import no.nordicsemi.android.mesh.utils.MeshAddress
import no.nordicsemi.android.support.v18.scanner.*
import okhttp3.Call
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


class LightControl(val mActivity: MainTabActivity2){

    //状态值用于进度条显示
    private var statueMessage = MutableLiveData<String>()

    lateinit var bleMeshManager : BleMeshManager

    lateinit var mMeshManagerApi : MeshManagerApi

    //连接对象
    var mSresult : ScanResult? = null

    private var mSetupProvisionedNode = false

    private var mProvisionedMeshNode: ProvisionedMeshNode? = null

    //选中需要连接数组
    var select_link = mutableListOf<LinkSelect>()

    //已分配设备是否在线
    var isonline = mutableListOf<String>()

    //设备名称 = 节点名称
    var node_name : String? = null

    //操作 cct 0  hsi 1  effect 2
    var operating_index = 0
    var operatingDatas = ""

    private val ATTENTION_TIMER = 5

    //更新组1  试灯2  配网更新节点3
    // 删除订阅组4  新增订阅分组5  删除节点6
    // 删除多个节点分组订阅7  订阅多个节点8   删除整个场景中的节点9
    var operating_mesh = 0
    var operating_group_type = 0  //1创建  2修改   3删除
    var operating_group_address = 0

    var vendorModelMessage_type = 0 //0预设效果  1获取设备详情CTL  2获取设备详情HSL  3进入升级模式

    //新增订阅后是否需要删除订阅
    var has_add_node = false
    var node_add_address = 0
    var node_add_uuid = ""

    //联网提示进度条
    val messageView by lazy {
        XPopup.Builder(mActivity)
            .hasShadowBg(false)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false)
            .asCustom(
                LoadingPopupView(mActivity).apply {
                    statueMessage.observeForever {
                        setTitle(it)
                    }
                }
            )
    }


    //初始化
    fun init(){

        initBleMeshManager()

        initMeshManagerApi()

        //mMeshManagerApi.loadMeshNetwork()
        getNetMeshConfig()

        /*setNetMeshConfig(
            UpLoadMeshConfigApi(
                ""
            )
        )*/
    }

    private fun initMeshManagerApi(){

        mMeshManagerApi = MeshManagerApi(mActivity)

        mMeshManagerApi.setMeshManagerCallbacks(object : MeshManagerCallbacks {

            override fun onNetworkLoaded(meshNetwork: MeshNetwork?) {

                //无网络数据，加入本地数据库  上传数据
                meshNetwork?.let {

                    upLoadDatas()

                    BusUtils.postSticky("init_mesh_success")

                    //获取场景，没有就创建
                    getScenesNet()

                    //dealWithDatas(meshNetwork)
                }
            }

            override fun onNetworkUpdated(meshNetwork: MeshNetwork?) {
                //数据更新时候
                when (operating_mesh) {

                    //组数据  上传
                    1 -> {
                        upLoadDatas()
                        //dealWithDatas(meshNetwork)
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
                        //dealWithDatas(meshNetwork)
                    }

                }

            }

            override fun onNetworkLoadFailed(error: String?) {
                ToastUtils.showShort(R.string.pop_t_str06)
            }

            override fun onNetworkImported(meshNetwork: MeshNetwork?) {

                if (mMeshManagerApi.meshNetwork?.meshUUID != meshNetwork?.meshUUID) {
                    mMeshManagerApi.deleteMeshNetworkFromDb(mMeshManagerApi.meshNetwork)
                }

                BusUtils.postSticky("init_mesh_success")

                //获取场景，没有就创建
                getScenesNet()

                //dealWithDatas(meshNetwork)
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
                state: ProvisioningState.States,
                data: ByteArray?
            ) {

                meshNode?.let {

                    gxUnicastAddress(it)

                    if (state == ProvisioningState.States.PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING) {

                        mMeshManagerApi.setProvisioningAuthentication(
                            "847B5943C0A05F41370A4171E5319ED2"
                        )


                    } else if (state == ProvisioningState.States.PROVISIONING_CAPABILITIES) {

                        if (mSetupProvisionedNode) {

                            //设置节点名称
                            node_name?.let { name ->
                                it.nodeName = name
                            }

                            mActivity.postDelayed({
                                mMeshManagerApi.startProvisioningWithStaticOOB(it)
                            }, 500)

                        }

                    } else if (state == ProvisioningState.States.PROVISIONING_INVITE) {
                        statueMessage.postValue("")
                    }

                    statueMessage.postValue(
                        statueToString(state)
                    )

                }
            }

            override fun onProvisioningFailed(
                meshNode: UnprovisionedMeshNode?,
                state: ProvisioningState.States,
                data: ByteArray?
            ) {
                messageView.dismiss()

                meshNode?.let {

                    gxUnicastAddress(it)

                    statueMessage.postValue(
                        statueToString(state)
                    )
                }
            }

            override fun onProvisioningCompleted(
                meshNode: ProvisionedMeshNode?,
                state: ProvisioningState.States,
                data: ByteArray?
            ) {

                if (state == ProvisioningState.States.PROVISIONING_COMPLETE) {

                    meshNode?.let {
                        mProvisionedMeshNode = it
                    }

                    bleMeshManager.disconnect().enqueue()

                    dealWithDatas(mMeshManagerApi.meshNetwork)

                    stopScan()

                    mActivity.postDelayed(
                        {
                            startScan()
                        }, 800
                    )

                } else {
                    messageView.dismiss()
                }

                statueMessage.postValue(
                    statueToString(state)
                )

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

                        statueMessage.postValue(
                            statueToString(ProvisioningState.States.COMPOSITION_DATA_GET_SENT)
                        )

                    } else if (meshMessage is ConfigDefaultTtlGet) {

                        statueMessage.postValue(
                            statueToString(ProvisioningState.States.SENDING_DEFAULT_TTL_GET)
                        )

                    } else if (meshMessage is ConfigAppKeyAdd) {

                        statueMessage.postValue(
                            statueToString(ProvisioningState.States.SENDING_APP_KEY_ADD)
                        )

                    } else if (meshMessage is ConfigNetworkTransmitSet) {

                        statueMessage.postValue(
                            statueToString(ProvisioningState.States.SENDING_NETWORK_TRANSMIT_SET)
                        )

                    } else if (meshMessage is ConfigModelSubscriptionDelete) {

                        statueMessage.postValue(
                            mActivity.getString(R.string.pop_t_str35)
                        )

                    } else if (meshMessage is ConfigModelSubscriptionAdd) {

                        statueMessage.postValue(
                            mActivity.getString(R.string.pop_t_str36)
                        )

                    } else if (meshMessage is ConfigModelAppBind) {

                        statueMessage.postValue(
                            mActivity.getString(R.string.pop_t_str32)
                        )


                    } else if (meshMessage is ConfigNodeReset) {

                        statueMessage.postValue(
                            mActivity.getString(R.string.pop_t_str37)
                        )
                    }

                }

            }

            override fun onMeshMessageReceived(src: Int, meshMessage: MeshMessage) {

                mMeshManagerApi.meshNetwork?.getNode(src)?.let {

                    mProvisionedMeshNode = it

                    when (meshMessage) {

                        is ConfigCompositionDataStatus -> {

                            if (mSetupProvisionedNode) {

                                mActivity.postDelayed(
                                    {
                                        val configDefaultTtlGet = ConfigDefaultTtlGet()
                                        mMeshManagerApi.createMeshPdu(
                                            it.unicastAddress,
                                            configDefaultTtlGet
                                        )

                                    }, 500
                                )
                            } else {

                            }

                        }

                        is ConfigDefaultTtlStatus -> {

                            if (mSetupProvisionedNode) {

                                mActivity.postDelayed(

                                    {

                                        val appKey: ApplicationKey? =
                                            mMeshManagerApi.meshNetwork?.appKeys?.get(
                                                0
                                            )

                                        val index = it.addedNetKeys[0].index
                                        val networkKey: NetworkKey? =
                                            mMeshManagerApi.meshNetwork?.netKeys?.get(
                                                index
                                            )

                                        if (appKey != null && networkKey != null) {

                                            val configAppKeyAdd =
                                                ConfigAppKeyAdd(networkKey, appKey)
                                            mMeshManagerApi.createMeshPdu(
                                                it.unicastAddress,
                                                configAppKeyAdd
                                            )
                                        }

                                    }, 500
                                )

                            } else {

                            }

                        }

                        is ConfigAppKeyStatus -> {

                            if (mSetupProvisionedNode) {

                                if (meshMessage.isSuccessful) {

                                    mActivity.postDelayed(

                                        {

                                            val networkTransmitSet =
                                                ConfigNetworkTransmitSet(2, 1)

                                            mMeshManagerApi.createMeshPdu(
                                                it.unicastAddress,
                                                networkTransmitSet
                                            )

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

                                    //配置  onoff服务钥匙
                                    addKeyBindng(it)

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

                            bleMeshManager.setClearCacheRequired()

                            if (operating_mesh == 9) {

                                //删除场景中节点
                                mMeshManagerApi.exportMeshNetwork()?.let { json ->

                                    setNetMeshConfig2(
                                        UpLoadMeshConfigApi(json)
                                    )

                                }

                            } else {

                            }

                        }

                        is GenericOnOffStatus -> {

                            //cct 操作
                            if (operating_index == 0) {

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
                            }

                        }

                        is VendorModelMessageStatus -> {

                            LogUtils.d(meshMessage)

                            //获取特效状态
                            if (meshMessage.opCode == 89) {

                                val buffer = ByteBuffer.wrap(meshMessage.myParameters).order(
                                    ByteOrder.LITTLE_ENDIAN
                                )

                                if (vendorModelMessage_type == 1) {

                                    //获取 ctl详情
                                    val lightness_step = buffer.get()
                                    val hue_step = buffer.get()
                                    val sat_step = buffer.get()

                                    val lightness_min = buffer.short
                                    val lightness_max = buffer.short
                                    val hue_min = buffer.short
                                    val hue_max = buffer.short
                                    val sat_min = buffer.short
                                    val sat_max = buffer.short

                                    BusUtils.postSticky(
                                        "getLedInfo",
                                        "$lightness_min-$lightness_max,$hue_min-$hue_max,$sat_min-$sat_max"
                                    )

                                    upLoadDatas()
                                    dealWithDatas(mMeshManagerApi.meshNetwork)

                                } else if (vendorModelMessage_type == 2) {

                                    //获取 hsi详情
                                    val lightness_step = buffer.get()
                                    val hue_step = buffer.get()
                                    val sat_step = buffer.get()

                                    val lightness_min = buffer.short
                                    val lightness_max = buffer.short
                                    val hue_min = buffer.short
                                    val hue_max = buffer.short
                                    val sat_min = buffer.short
                                    val sat_max = buffer.short

                                    BusUtils.postSticky(
                                        "getLedInfo",
                                        "$lightness_min-$lightness_max,$hue_min-$hue_max,$sat_min-$sat_max"
                                    )

                                    upLoadDatas()
                                    dealWithDatas(mMeshManagerApi.meshNetwork)

                                } else if(vendorModelMessage_type == 3) {

                                    //升级模式是否已经打开
                                    BusUtils.post("goDfu")

                                }else {
                                    val lightness = buffer.short.toInt() and 0xFFFF
                                    val effect = buffer.get()
                                    val preset = buffer.get()

                                    BusUtils.postSticky(
                                        "EffectGet",
                                        mutableMapOf(
                                            "lightness" to lightness,
                                            "effect" to effect.toInt(),
                                            "preset" to preset.toInt()
                                        )
                                    )

                                }

                            } else {

                            }

                        }

                        is ConfigModelSubscriptionStatus -> {

                            //订阅分组状态
                            if (meshMessage.isSuccessful) {

                                //配网
                                if (operating_mesh == 3) {

                                    val g_datas = mMeshManagerApi.meshNetwork?.groups?.filter {
                                        it.name == mActivity.resources.getString(
                                            R.string.device_text05
                                        )
                                    }

                                    g_datas?.let {

                                        //查看是否还有别的需要连接的设备
                                        val lsIt = getNextLinkData()

                                        if (lsIt == null) {

                                            messageView.dismiss()

                                            node_name?.let {
                                                setDevOnLine(it, "true")
                                            }

                                            dealWithDatas(mMeshManagerApi.meshNetwork)

                                            select_link.clear()

                                            upLoadDatas()

                                        } else {

                                            stopScan()

                                            node_name?.let {
                                                setDevOnLine(it, "true")
                                            }

                                            dealWithDatas(mMeshManagerApi.meshNetwork)

                                            connetBle(lsIt.scan_datas)
                                        }
                                    }

                                } else if (operating_mesh == 4) {

                                    if (has_add_node) {

                                        AddGroupNode(node_add_address, node_add_uuid)

                                        has_add_node = false

                                    } else {

                                        upLoadDatas()
                                        //删除订阅组
                                        messageView.dismiss()

                                        mActivity.postDelayed({
                                            dealWithDatas(mMeshManagerApi.meshNetwork)
                                        }, 400)
                                    }

                                } else if (operating_mesh == 5) {

                                    //新增订阅组
                                    upLoadDatas()

                                    messageView.dismiss()

                                    mActivity.postDelayed({
                                        dealWithDatas(mMeshManagerApi.meshNetwork)
                                    }, 300)

                                } else if (operating_mesh == 7) {
                                    //切换场景时候，取消订阅
                                    BaseInfoData.scenes_node_del_change.removeAt(0)

                                    if (BaseInfoData.scenes_node_del_change.isEmpty()) {
                                        messageView.dismiss()
                                        BusUtils.postSticky("Change_Current")
                                    } else {
                                        //继续删除订阅
                                        delGroupMoreNode()
                                    }


                                } else if (operating_mesh == 8) {

                                    //切换场景时候，订阅新分组
                                    BaseInfoData.scenes_node_add_change.removeAt(0)

                                    if (BaseInfoData.scenes_node_add_change.isEmpty()) {

                                        messageView.dismiss()
                                        dealWithDatas(mMeshManagerApi.meshNetwork)
                                        //完成切换
                                        operating_mesh = 0
                                    } else {
                                        //继续订阅分组
                                        AddGroupMoreNode()
                                    }
                                } else {
                                    messageView.dismiss()
                                }


                            } else {

                            }

                        }

                        is ConfigModelAppStatus -> {

                            //绑定钥匙成功
                            if (meshMessage.isSuccessful) {

                                statueMessage.postValue(
                                    mActivity.getString(R.string.pop_t_str33)
                                )

                                //配置上未分组订阅
                                addSubscriptions(it)

                            } else {

                                statueMessage.postValue(
                                    mActivity.getString(R.string.pop_t_str34)
                                )

                                mActivity.postDelayed({
                                    messageView.dismiss()
                                }, 800)

                            }
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

    fun upLoadDatas() {

        //上传到meshconfig 到服务器
        mMeshManagerApi.exportMeshNetwork()?.let { json ->

            setNetMeshConfig(
                UpLoadMeshConfigApi(
                    json
                )
            )

        }

    }

    private fun initBleMeshManager(){

        bleMeshManager = BleMeshManager(mActivity)

        bleMeshManager.setGattCallbacks(object : BleMeshManagerCallbacks {

            override fun onDeviceConnecting(device: BluetoothDevice) {

                if (!mActivity.isFinishing) {
                    messageView.show()
                }

                statueMessage.postValue(mActivity.getString(R.string.pop_t_str01))
            }

            override fun onDeviceConnected(device: BluetoothDevice) {
                statueMessage.postValue(mActivity.getString(R.string.pop_t_str02))
            }

            override fun onDeviceDisconnecting(device: BluetoothDevice) {
                statueMessage.postValue(mActivity.getString(R.string.pop_t_str03))
            }

            override fun onDeviceDisconnected(device: BluetoothDevice) {

                //设置状态
                setDevOnLine(device.name, "false")

                //移除新设备
                BusUtils.postSticky("remove_scan", device.name)
                mMeshManagerApi.meshNetwork?.proxyFilter = null
            }

            override fun onLinkLossOccurred(device: BluetoothDevice) {
            }

            override fun onServicesDiscovered(
                device: BluetoothDevice,
                optionalServicesFound: Boolean
            ) {
                statueMessage.postValue(mActivity.getString(R.string.pop_t_str04))
            }

            override fun onDeviceReady(device: BluetoothDevice) {

                mActivity.hideDialog()

                if (bleMeshManager.isProvisioningComplete) {

                    if (mSetupProvisionedNode) {

                        mMeshManagerApi.meshNetwork?.let {

                            if (it.selectedProvisioner.provisionerAddress != null) {

                                //配置节点预设信息
                                mProvisionedMeshNode?.let {

                                    if (!mActivity.isFinishing) {
                                        messageView.show()
                                    }

                                    mMeshManagerApi
                                        .createMeshPdu(
                                            it.unicastAddress,
                                            ConfigCompositionDataGet()
                                        )
                                }

                            } else {
                                mSetupProvisionedNode = false
                            }

                        }


                    } else {
                        //设置状态
                        messageView.dismiss()
                        //setDevOnLine(device.name, "true")
                    }


                } else {

                    mSetupProvisionedNode = true
                    //亮灯验证节点
                    mSresult?.let {

                        getServiceData(it, BleMeshManager.MESH_PROVISIONING_UUID)?.let { ba ->

                            node_name = device.name

                            val uuid = mMeshManagerApi.getDeviceUuid(ba)
                            mMeshManagerApi.identifyNode(uuid, ATTENTION_TIMER)

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
                statueMessage.postValue(message)
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


    fun getServiceData(
        result: ScanResult,
        serviceUuid: UUID
    ): ByteArray? {
        val scanRecord =
            result.scanRecord
        return scanRecord?.getServiceData(ParcelUuid(serviceUuid))
    }

    @SuppressLint("RestrictedApi")
    fun dealWithDatas(meshNetwork: MeshNetwork?){

        meshNetwork?.let { meshIt ->

            if (!meshIt.isProvisionerSelected){
                val provisioner = meshIt.provisioners[0]
                provisioner.isLastSelected = true
                meshIt.selectProvisioner(provisioner)
            }

            //获取能用节点
            val nodes_list = mutableListOf<ProvisionedMeshNode>()
            //节点
            val mNodes = meshIt.nodes
            mNodes.forEach {

                meshIt.selectedProvisioner?.let { pro ->

                    if (it.uuid != pro.provisionerUuid) {
                        nodes_list.add(it)
                    }
                }
            }

            if (nodes_list.isNotEmpty()) {

                if (isonline.isEmpty()){

                    nodes_list.forEach {
                        isonline.add(
                            "${it.nodeName},false"
                        )
                    }

                    BusUtils.postSticky("onLineStatue")

                }else{

                    nodes_list.forEach {

                        val name = it.nodeName

                        val has_data = isonline.any { it.split(",")[0] == name }

                        if (!has_data){
                            isonline.add(
                                "${name},false"
                            )
                        }
                    }
                }
            }

            //处理更新数据
            BusUtils.postSticky("deal_with")
        }
    }

    fun statueToString(state: ProvisioningState.States) : String{

        return when(state){

            ProvisioningState.States.PROVISIONING_INVITE -> mActivity.getString(R.string.pop_t_str07)

            ProvisioningState.States.PROVISIONING_CAPABILITIES -> mActivity.getString(R.string.pop_t_str08)

            ProvisioningState.States.PROVISIONING_START -> mActivity.getString(R.string.pop_t_str09)

            ProvisioningState.States.PROVISIONING_PUBLIC_KEY_SENT -> mActivity.getString(R.string.pop_t_str10)

            ProvisioningState.States.PROVISIONING_PUBLIC_KEY_RECEIVED -> mActivity.getString(R.string.pop_t_str11)

            ProvisioningState.States.PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING,
            ProvisioningState.States.PROVISIONING_AUTHENTICATION_OUTPUT_OOB_WAITING,
            ProvisioningState.States.PROVISIONING_AUTHENTICATION_INPUT_OOB_WAITING
            -> mActivity.getString(R.string.pop_t_str12)

            ProvisioningState.States.PROVISIONING_AUTHENTICATION_INPUT_ENTERED -> mActivity.getString(
                R.string.pop_t_str13
            )

            ProvisioningState.States.PROVISIONING_INPUT_COMPLETE -> mActivity.getString(R.string.pop_t_str14)

            ProvisioningState.States.PROVISIONING_CONFIRMATION_SENT -> mActivity.getString(R.string.pop_t_str15)

            ProvisioningState.States.PROVISIONING_CONFIRMATION_RECEIVED -> mActivity.getString(R.string.pop_t_str16)

            ProvisioningState.States.PROVISIONING_RANDOM_SENT -> mActivity.getString(R.string.pop_t_str17)

            ProvisioningState.States.PROVISIONING_RANDOM_RECEIVED -> mActivity.getString(R.string.pop_t_str18)

            ProvisioningState.States.PROVISIONING_DATA_SENT -> mActivity.getString(R.string.pop_t_str19)

            ProvisioningState.States.PROVISIONING_COMPLETE -> mActivity.getString(R.string.pop_t_str20)

            ProvisioningState.States.PROVISIONING_FAILED -> mActivity.getString(R.string.pop_t_str21)

            ProvisioningState.States.COMPOSITION_DATA_GET_SENT -> mActivity.getString(R.string.pop_t_str22)

            ProvisioningState.States.COMPOSITION_DATA_STATUS_RECEIVED -> mActivity.getString(R.string.pop_t_str23)

            ProvisioningState.States.SENDING_DEFAULT_TTL_GET -> mActivity.getString(R.string.pop_t_str24)

            ProvisioningState.States.DEFAULT_TTL_STATUS_RECEIVED -> mActivity.getString(R.string.pop_t_str25)

            ProvisioningState.States.SENDING_APP_KEY_ADD -> mActivity.getString(R.string.pop_t_str26)

            ProvisioningState.States.APP_KEY_STATUS_RECEIVED -> mActivity.getString(R.string.pop_t_str27)

            ProvisioningState.States.SENDING_NETWORK_TRANSMIT_SET -> mActivity.getString(R.string.pop_t_str28)

            ProvisioningState.States.NETWORK_TRANSMIT_STATUS_RECEIVED -> mActivity.getString(R.string.pop_t_str29)

            ProvisioningState.States.SENDING_BLOCK_ACKNOWLEDGEMENT -> mActivity.getString(R.string.pop_t_str30)

            ProvisioningState.States.BLOCK_ACKNOWLEDGEMENT_RECEIVED -> mActivity.getString(R.string.pop_t_str31)

            else -> ""
        }
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

                        stopScan()

                        if (mMeshManagerApi.isAdvertisedWithNodeIdentity(serviceData)) {

                            mProvisionedMeshNode?.let {
                                if (mMeshManagerApi.nodeIdentityMatches(it, serviceData)) {

                                    mSetupProvisionedNode = true

                                    mActivity.postDelayed(
                                        {
                                            connetBle(result)
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

                                    //重新连接
                                    mSetupProvisionedNode = false

                                    mActivity.postDelayed(
                                        {
                                            connetBle(result)
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

    fun stopScan() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.stopScan(scanCallback)
    }

    //先配置钥匙
    @SuppressLint("RestrictedApi")
    fun addKeyBindng(meshNode: ProvisionedMeshNode){

        meshNode.elements?.let { eleIt ->

            if (eleIt.isNotEmpty()){

                val element = eleIt[eleIt.keys.hashCode()]

                element?.meshModels?.values?.let { modelsIt ->

                    val onOff = modelsIt.filter { it.modelName == "Generic On Off Server" }

                    if (onOff.isNotEmpty()){

                        val onOffServer = onOff.get(0)

                        //该服务还没钥匙
                        if (onOffServer.boundAppKeyIndexes.isEmpty()){

                            meshNode.addedAppKeys.forEach { nodekey ->

                                val n_index = nodekey.index

                                mMeshManagerApi.meshNetwork?.appKeys?.forEach { appkey ->

                                    if (n_index == appkey.keyIndex){

                                        val configModelAppUnbind = ConfigModelAppBind(
                                            element.elementAddress,
                                            onOff[0].modelId,
                                            appkey.keyIndex
                                        )

                                        //添加新的钥匙 Generic On Off Server
                                        mMeshManagerApi.createMeshPdu(
                                            meshNode.unicastAddress,
                                            configModelAppUnbind
                                        )

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


    //检查appkey是否配置成功
    @SuppressLint("RestrictedApi")
    fun CheckKeyBindng(meshNode: ProvisionedMeshNode) : Boolean{

        meshNode.elements?.let { eleIt ->

            if (eleIt.isNotEmpty()){

                val element = eleIt[eleIt.keys.hashCode()]

                element?.meshModels?.values?.let { modelsIt ->

                    val onOff = modelsIt.filter { it.modelName == "Generic On Off Server" }

                    if (onOff.isNotEmpty()){

                        val onOffServer = onOff.get(0)

                        //该服务还没钥匙
                        return onOffServer.boundAppKeyIndexes.isNotEmpty()
                    }
                }

            }else{

                return false
            }
        }

        return false
    }

    //添加Generic On Off Server 添加钥匙和订阅未分组
    @SuppressLint("RestrictedApi")
    fun addSubscriptions(meshNode: ProvisionedMeshNode){

        meshNode.elements?.run {

            if (isNotEmpty()){

                val element = get(keys.hashCode())

                element?.meshModels?.values?.let { meshModel ->

                    val onOff = meshModel.filter { it.modelName == "Generic On Off Server" }

                    if (onOff.isNotEmpty()){

                        val key01 = meshNode.addedAppKeys

                        if (key01.isNotEmpty()){

                            mMeshManagerApi.meshNetwork?.appKeys?.forEach {

                                if (key01[0].index == it.keyIndex){

                                    //添加订阅到未分组
                                    mMeshManagerApi.meshNetwork?.let { groupit ->

                                        val wfz = mActivity.getString(R.string.device_text05)

                                        val groups = groupit.groups.filter { it.name == wfz }

                                        if (groups.isNotEmpty()){

                                            val configModelSubscriptionAdd : MeshMessage

                                            if (groups[0].addressLabel == null){

                                                configModelSubscriptionAdd = ConfigModelSubscriptionAdd(
                                                    element.elementAddress,
                                                    groups[0].address, onOff[0].modelId
                                                )

                                            }else{

                                                configModelSubscriptionAdd = ConfigModelSubscriptionVirtualAddressAdd(
                                                    element.elementAddress,
                                                    groups[0].addressLabel!!, onOff[0].modelId
                                                )

                                            }
                                            //添加到未分组
                                            mMeshManagerApi.createMeshPdu(
                                                meshNode.unicastAddress,
                                                configModelSubscriptionAdd
                                            )

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

    }


    //检查是否配置上分组
    @SuppressLint("RestrictedApi")
    fun CheckSubscriptionGroup(meshNode: ProvisionedMeshNode, address: Int) : Boolean{

        meshNode.elements?.let { eleIt ->

            if (eleIt.isNotEmpty()){

                val element = eleIt[eleIt.keys.hashCode()]

                element?.meshModels?.values?.let { modelsIt ->

                    val onOff = modelsIt.filter { it.modelName == "Generic On Off Server" }

                    if (onOff.isNotEmpty()){

                        val onOffServer = onOff.get(0)

                        //是否已经配上
                        return onOffServer.subscribedAddresses.any { it == address }

                    }
                }

            }else{

                return false
            }
        }

        return false
    }

    fun getNextLinkData() : LinkSelect?{

        select_link.firstOrNull { it.isLink == false }?.let {

            it.isLink = true

            return getLinkData()

        } ?: return null

    }

    fun getLinkData() : LinkSelect?{
        return select_link.lastOrNull { it.isLink == true }
    }

    fun connetBle(result: ScanResult){

        mSresult = result

        initBleMeshManager()

        mActivity.postDelayed({
            bleMeshManager.connect(result.device).retry(3, 200).enqueue()
        }, 300)

    }

    fun removeGroup(address: Int){

        //操作更新组
        operating_mesh = 1
        operating_group_type = 3
        operating_group_address = address

        mMeshManagerApi.meshNetwork?.let {

            val mGroup = it.getGroup(address)
            if (it.isGroupExist(mGroup)){
                it.removeGroup(mGroup)
            }
        }
    }

    fun editGroupName(name: String, address: Int){

        //操作更新组
        operating_mesh = 1
        operating_group_type = 2
        operating_group_address = address

        mMeshManagerApi.meshNetwork?.let {

            it.getGroup(address)?.let { g_it ->
                g_it.name = name
                it.updateGroup(g_it)
            }
        }
    }

    fun creatMeshGroup(name: String){

        //操作更新组
        operating_mesh = 1
        operating_group_type = 1

        mMeshManagerApi.meshNetwork?.let {

            val group = it.createGroup(it.selectedProvisioner, name)
            operating_group_address = group.address

            it.addGroup(group)
        }
    }

    //切换分组时候，首先删除订阅
    @SuppressLint("RestrictedApi")
    fun delGroupMoreNode(){

        if (!mActivity.isFinishing){
            messageView.show()
        }

        mMeshManagerApi.meshNetwork?.let { mWork ->

            val address = BaseInfoData.scenes_node_del_change.get(0).node_address
            val g_address = BaseInfoData.scenes_node_del_change.get(0).group_address

            val node = mWork.getNode(address)

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

                                        val group = mWork.getGroup(g_address)

                                        if (group != null){

                                            val message : MeshMessage

                                            if (MeshAddress.isValidGroupAddress(g_address)) {

                                                message =
                                                    ConfigModelSubscriptionDelete(
                                                        element.elementAddress,
                                                        group.address,
                                                        onOff[0].modelId
                                                    )

                                            } else {

                                                onOff[0].getLabelUUID(address)

                                                val uuid = onOff[0].getLabelUUID(address)

                                                message =
                                                    ConfigModelSubscriptionVirtualAddressDelete(
                                                        element.elementAddress,
                                                        uuid,
                                                        onOff[0].modelId
                                                    )
                                            }

                                            operating_mesh = 7

                                            //删除分组订阅（切换场景时候使用）
                                            mMeshManagerApi.createMeshPdu(
                                                node.unicastAddress,
                                                message
                                            )

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

    }


    @SuppressLint("RestrictedApi")
    fun delGroupNodeInfo(address: Int, g_address: Int){

        if (!mActivity.isFinishing){
            messageView.show()
        }

        mMeshManagerApi.meshNetwork?.let { mWork ->

            val node = mWork.getNode(address)

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

                                        val group = mWork.getGroup(g_address)

                                        if (group != null){

                                            val message : MeshMessage

                                            if (MeshAddress.isValidGroupAddress(g_address)) {

                                                message =
                                                    ConfigModelSubscriptionDelete(
                                                        element.elementAddress,
                                                        group.address,
                                                        onOff[0].modelId
                                                    )

                                            } else {

                                                onOff[0].getLabelUUID(address)

                                                val uuid = onOff[0].getLabelUUID(address)

                                                message =
                                                    ConfigModelSubscriptionVirtualAddressDelete(
                                                        element.elementAddress,
                                                        uuid,
                                                        onOff[0].modelId
                                                    )
                                            }

                                            operating_mesh = 4
                                            has_add_node = false

                                            //删除分组订阅
                                            mMeshManagerApi.createMeshPdu(
                                                node.unicastAddress,
                                                message
                                            )

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

    }


    //切换分组时候，最后订阅分组
    @SuppressLint("RestrictedApi")
    fun AddGroupMoreNode(){

        if (!mActivity.isFinishing){
            messageView.show()
        }

        val address = BaseInfoData.scenes_node_add_change.get(0).node_address
        val g_address = BaseInfoData.scenes_node_add_change.get(0).group_address

        mMeshManagerApi.meshNetwork?.let { mWork ->

            val node = mWork.getNode(address)

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

                                        val group = mWork.getGroup(g_address)

                                        val message : MeshMessage

                                        if (group != null){

                                            val mSize = onOff[0].subscribedAddresses.size

                                            has_add_node = mSize != 1

                                            //新增节点到组
                                            operating_mesh = 5

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

                                            operating_mesh = 8

                                            //添加分组
                                            mMeshManagerApi.createMeshPdu(
                                                node.unicastAddress,
                                                message
                                            )

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

    }


    @SuppressLint("RestrictedApi")
    fun delGroupNode(address: Int, uuid: String){

        if (!mActivity.isFinishing){
            messageView.show()
        }

        mMeshManagerApi.meshNetwork?.let { mWork ->

            val node = mWork.getNode(uuid)

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

                                        if (group != null){

                                            val message : MeshMessage

                                            if (MeshAddress.isValidGroupAddress(address)) {

                                                message =
                                                    ConfigModelSubscriptionDelete(
                                                        element.elementAddress,
                                                        group.address,
                                                        onOff[0].modelId
                                                    )

                                            } else {

                                                onOff[0].getLabelUUID(address)

                                                val uuid = onOff[0].getLabelUUID(address)

                                                message =
                                                    ConfigModelSubscriptionVirtualAddressDelete(
                                                        element.elementAddress,
                                                        uuid,
                                                        onOff[0].modelId
                                                    )
                                            }

                                            operating_mesh = 4

                                            //删除分组
                                            mMeshManagerApi.createMeshPdu(
                                                node.unicastAddress,
                                                message
                                            )

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

    }

    //节点添加到分组
    @SuppressLint("RestrictedApi")
    fun AddGroupNode(address: Int, uuid: String){

        if (!mActivity.isFinishing){
            messageView.show()
        }

        mMeshManagerApi.meshNetwork?.let { mWork ->

            val node = mWork.getNode(uuid)

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

                                            val mSize = onOff[0].subscribedAddresses.size

                                            has_add_node = mSize != 1

                                            if (has_add_node){

                                                //首先删除自己所在的分组订阅
                                                delGroupNode(onOff[0].subscribedAddresses[1], uuid)

                                                node_add_address = address
                                                node_add_uuid = uuid

                                            }else{

                                                //新增节点到组
                                                operating_mesh = 5

                                                mMeshManagerApi.meshNetwork?.let {
                                                   val appkey = it.createAppKey()
                                                    it.addAppKey(appkey)
                                                    appkey.boundNetKeyIndex = 0
                                                }

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

    }

    //获取当前设备信息
    fun lightInfo(address: Int, appKeyIndex: Int, size: Int){

        mMeshManagerApi.meshNetwork?.let {

            val appkey = it.getAppKey(appKeyIndex)

            val message = if (size == 6){

                vendorModelMessage_type = 2

                VendorModelMessageAcked(
                    appkey!!,
                    0x1888,
                    0x0059,
                    0x0C or 0xC0,
                    byteArrayOf()
                )

            }else{

                vendorModelMessage_type = 1

                VendorModelMessageAcked(
                    appkey!!,
                    0x1888,
                    0x0059,
                    0x0A or 0xC0,
                    byteArrayOf()
                )
            }

            mMeshManagerApi.createMeshPdu(address, message)
        }
    }

    //检查灯光
    fun opneLight(address: Int, appKeyIndex: Int){

        operating_mesh = 2

        mMeshManagerApi.meshNetwork?.let {

            val appkey = it.getAppKey(appKeyIndex)

            val message = VendorModelMessageUnacked(
                appkey!!,
                0x1888,
                0x0059,
                0x09,
                getCheckParameter()
            )

            mMeshManagerApi.createMeshPdu(address, message)
        }
    }

    fun getCheckParameter() : ByteArray{

        val mBu = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
        mBu.put(1)
        mBu.put(BaseInfoData.getNextTid().toByte())

        return mBu.array()
    }

    //删除节点
    fun delNote(mUnicastAddress: Int){

        mMeshManagerApi.meshNetwork?.let { work ->

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

            if (nodes_list.isEmpty()){
                return
            }

            nodes_list.filter { it.unicastAddress == mUnicastAddress}.run {

                if (isEmpty()){
                    return
                }

                operating_mesh = 6

                //删除该节点
                work.deleteNode(get(0))
            }
        }
    }

    //删除节点
    fun resetNoteScenes(mUnicastAddress: Int){

        operating_mesh = 9

        mMeshManagerApi.createMeshPdu(mUnicastAddress, ConfigNodeReset())
    }

    fun getDevOnLine(key: String) : Boolean{

        if (isonline.isEmpty()){
            return false
        }else{

            isonline.forEachIndexed { index, s ->

                val key02 = s.split(",")

                if (key == key02[0]){

                    return key02[1] == "true"
                }
            }

        }

        return false
    }

    fun setDevOnLine(key: String, value: String){

        if (isonline.isNotEmpty()){

            var mIndex = -1

            isonline.forEachIndexed { index, s ->

                val key02 = s.split(",")

                if (key == key02[0]){
                    mIndex = index
                }
            }

            if (mIndex != -1){
                isonline[mIndex] = "$key,$value"
                BusUtils.postSticky("onLineStatue")
            }
        }
    }

    //检查是否全部设备在线
    fun checkDevOnline() : Boolean{
        return isonline.all { it.split(",")[1] == "true" }
    }

    fun onDestroy(){
        bleMeshManager.disconnect()
        bleMeshManager.close()
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
                if (operating_group_type == 3){
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
                if (operating_group_type == 1){
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

                operating_group_type = 0

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

        return d_list
    }


    fun getLedInfoElementSize(mUnicastAddress: Int) : Int{

        mMeshManagerApi.meshNetwork?.let { meshwork ->

            //获取能用节点
            val nodes_list = mutableListOf<ProvisionedMeshNode>()
            //节点
            val mNodes = meshwork.nodes
            mNodes.forEach {

                meshwork.selectedProvisioner?.let { pro ->

                    if (it.uuid != pro.provisionerUuid){

                        nodes_list.add(it)
                    }
                }
            }

            if (nodes_list.isEmpty()){
                return 0
            }

            nodes_list.forEach {

                if (it.unicastAddress == mUnicastAddress){

                    val e01 = it.elements

                    if (e01.isNotEmpty()){

                        val element = e01[e01.keys.hashCode()]

                        val models = element?.meshModels

                        return models?.size ?: 0
                    }
                }
            }

        }

        return 0

    }

    //总开关打开
    fun setTotalOnOff(isOnOff: Boolean, index: Int){

        operating_index = index

        val wxz = mMeshManagerApi.meshNetwork?.groups?.get(0)

        val appkey = mMeshManagerApi.meshNetwork?.getAppKey(0)

        val message = GenericOnOffSet(
            appkey!!, isOnOff, BaseInfoData.getNextTid()
        )

        mMeshManagerApi.createMeshPdu(wxz!!.address, message)

    }

    //总开关设置亮度
    fun setTotalLightness(lightLightness: Int){

        val wxz = mMeshManagerApi.meshNetwork?.groups?.get(0)

        val appkey = mMeshManagerApi.meshNetwork?.getAppKey(0)

        val message = VendorModelMessageUnacked(
            appkey!!,
            0x1888,
            0x0059,
            0x10 or 0xC0,
            getParameter(lightLightness)
        )

        mMeshManagerApi.createMeshPdu(wxz!!.address, message)
    }

    fun getParameter(lightLightness: Int) : ByteArray{

        val mBu = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN)

        mBu.put(0)
        mBu.putShort(0.toShort())//当前亮度
        mBu.putShort(lightLightness.toShort())//目标亮度
        mBu.put(BaseInfoData.getNextTid().toByte())

        return mBu.array()
    }

    //设置固件升级模式
    fun setDfuModel(mAddress : Int){

        vendorModelMessage_type = 3

        val appkey = mMeshManagerApi.meshNetwork?.getAppKey(0)

        val message = VendorModelMessageUnacked(
            appkey!!,
            0x1887,
            0x0059,
            0x16 or 0xC0,
            getDfuParameter()
        )

        mMeshManagerApi.createMeshPdu(mAddress, message)

        mActivity.postDelayed({
            vendorModelMessage_type = 0
        },10000)
    }

    fun getDfuParameter() : ByteArray{

        val mBu = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN)

        mBu.putShort(0xABCD.toShort())//当前亮度
        mBu.put(BaseInfoData.getNextTid().toByte())

        return mBu.array()
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

    fun setNetMeshConfig(api: UpLoadMeshConfigApi){

        EasyHttp.post(mActivity)
            .api(api)
            .request(object : HttpCallback<HttpData<String>>(mActivity) {

                override fun onSucceed(result: HttpData<String>?) {

                    //更新场景数据
                    mActivity.postDelayed({
                        ChuLiAllData01()
                    }, 150)

                }

                override fun onFail(e: Exception?) {
                    operating_mesh = 0
                }

                override fun onStart(call: Call?) {
                }
            })

    }

    //上传本地数据，然后删除场景中所有node
    fun setNetMeshConfig2(api: UpLoadMeshConfigApi){

        EasyHttp.post(mActivity)
            .api(api)
            .request(object : HttpCallback<HttpData<String>>(mActivity) {

                override fun onSucceed(result: HttpData<String>?) {

                    delNodeForScenes(
                        DelNodeApi(
                            mActivity.delNoteScenes_address
                        )
                    )
                }

                override fun onFail(e: Exception?) {
                    operating_mesh = 0
                }

                override fun onStart(call: Call?) {
                }
            })

    }

    //获取场景列表
    fun getScenesNet(){

        EasyHttp.post(mActivity)
            .api(ScenesListApi())
            .request(object : HttpCallback<HttpData<ScenesListApi.ScenesBean>>(mActivity) {

                override fun onStart(call: Call?) {
                }

                override fun onSucceed(result: HttpData<ScenesListApi.ScenesBean>?) {
                    super.onSucceed(result)

                    result?.objx?.let {

                        if (it.content.isEmpty()) {

                            creatScenes()

                        } else {

                            it.content.forEach {

                                if (it.isCurrent) {
                                    BaseInfoData.scenes_cur = it

                                    if (operating_mesh == 1 || operating_mesh == 6 || operating_mesh == 0) {
                                        dealWithDatas(mMeshManagerApi.meshNetwork)
                                    }

                                }
                            }

                            operating_mesh = 0

                        }

                    } ?: creatScenes()

                }

            })

    }

    //获取场景列表，删除
    fun getScenesNet2(){

        EasyHttp.post(mActivity)
            .api(ScenesListApi())
            .request(object : HttpCallback<HttpData<ScenesListApi.ScenesBean>>(mActivity) {

                override fun onSucceed(result: HttpData<ScenesListApi.ScenesBean>?) {
                    super.onSucceed(result)

                    result?.objx?.let {

                        if (it.content.isEmpty()) {

                            creatScenes()

                        } else {

                            it.content.forEach {

                                if (it.isCurrent) {
                                    BaseInfoData.scenes_cur = it

                                    if (operating_mesh == 1 || operating_mesh == 6 || operating_mesh == 0) {
                                        dealWithDatas(mMeshManagerApi.meshNetwork)
                                    }

                                }
                            }

                            operating_mesh = 0

                        }

                    } ?: creatScenes()

                }

            })

    }

    //不存在自动创建一个场景
    fun creatScenes(){

        EasyHttp.post(mActivity)
            .api(
                AddScenesApi(
                    mActivity.getString(R.string.scene_t01), true
                )
            )
            .request(object : HttpCallback<HttpData<ScenesListApi.ScenesBean>>(mActivity) {

                override fun onSucceed(result: HttpData<ScenesListApi.ScenesBean>?) {
                    super.onSucceed(result)

                    getScenesNet()
                }

                override fun onStart(call: Call?) {
                }

            })
    }

    //更新场景
    fun upDataScenes(){

        BaseInfoData.scenes_cur?.let {

            val api = UpDataScenesApi2(
                id = it.id,
                scenesName = it.scenesName,
                isCurrent = it.isCurrent
            )

            it.deviceGroups?.let {
                api.deviceGroups = it
            }

            EasyHttp.post(mActivity)
                .api(api)
                .request(object : HttpCallback<HttpData<JSONObject>>(mActivity) {

                    override fun onSucceed(result: HttpData<JSONObject>?) {
                        getScenesNet()
                    }

                    override fun onStart(call: Call?) {
                    }
                })

        }
    }

    //删除场景中的节点
    fun delNodeForScenes(api: DelNodeApi){

        EasyHttp.post(mActivity)
            .api(api)
            .request(object : HttpCallback<HttpData<JSONObject>>(mActivity) {

                override fun onSucceed(result: HttpData<JSONObject>?) {

                    operating_mesh = 0
                    getScenesNet()
                }

                override fun onStart(call: Call?) {
                }
            })

    }

    /****网络请求部分****/

}