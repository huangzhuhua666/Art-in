package com.tw.artin.ui2.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.blankj.utilcode.util.BusUtils
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.tw.artin.base.BaseInfoData
import com.tw.artin.bean.*
import com.tw.artin.ui2.fragment.NCctFragment
import com.tw.artin.ui2.fragment.NControllerFragment
import com.tw.artin.util.UtilsBigDecimal
import no.nordicsemi.android.mesh.transport.GenericOnOffSet
import no.nordicsemi.android.mesh.transport.LightCtlGet
import java.lang.StringBuilder
import kotlin.math.ln
import kotlin.math.pow

class NCctAdapter(val mFragment : NCctFragment) : BaseNodeAdapter(){

    //0没有下一步  1选中后，操作开关   2选中后，操作亮度
    var has_next = 0

    var nextBean : BaseNode? = null
    var nextBeanString : String = ""

    init {
        addNodeProvider(NCctGProvider(mFragment))
        addNodeProvider(NCctItemProvider())
        addNodeProvider(NCctNGProvider(mFragment))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {

        val info = data.get(position)

        when(data[position]){

            is NControllerGruoupBean -> return 0

            is NControllerItemBean -> return 1

            is NControllerNoGroupBean -> return 2

            else -> return -1
        }
    }

    fun CheckModel(pos : Int) : Boolean{

        var isGo = false

        mFragment.parentFragment?.let {

            if (it is NControllerFragment){

                val sData = data[pos]

                if (sData is NControllerGruoupBean){
                    //非手动选择 且 非当前状态  跳转
                    if (sData.currentDeviceType != 1 && !it.mHeadClick){
                        BusUtils.postSticky("move_controller",sData.currentDeviceType!! - 1)
                        isGo = true
                    }
                }else if (sData is NControllerNoGroupBean){
                    //非手动选择 且 非当前状态  跳转
                    if (sData.currentDeviceType != 1 && !it.mHeadClick){
                        BusUtils.postSticky("move_controller",sData.currentDeviceType!! - 1)
                        isGo = true
                    }
                }
            }
        }

        return isGo

    }

    //选中设备或分组
    fun selectItem(pos : Int,hasGetData : Boolean){

        if (CheckModel(pos)){
            return
        }

        var mAddress = 0
        var mIsGroup = false

        val infos = data[pos]
        if (infos is NControllerGruoupBean){
            if (infos.deviceType == 2){
                BusUtils.postSticky("gone_hsi",true)
            }else{
                BusUtils.postSticky("gone_hsi",false)
            }
        }else if(infos is NControllerNoGroupBean){
            if (infos.deviceType == 2){
                BusUtils.postSticky("gone_hsi",true)
            }else{
                BusUtils.postSticky("gone_hsi",false)
            }
        }

        data.forEachIndexed { index, baseNode ->

            when(baseNode){

                is NControllerGruoupBean ->{

                    if (index == pos){
                        mAddress = baseNode.address!!.toInt()
                        mIsGroup = true
                    }

                    baseNode.isSelect = index == pos

                    baseNode.childNode?.forEach {
                        it as NControllerItemBean
                        it.isSelect = index == pos
                    }
                }

                is NControllerNoGroupBean ->{

                    if (index == pos){
                        mAddress = baseNode.unicastAddress!!.toInt()
                        mIsGroup = false
                    }

                    baseNode.isSelect = index == pos
                }
            }
        }

        notifyDataSetChanged()

        if (hasGetData){
            getCctData(0,mAddress,mIsGroup)
        }
    }

    //获取cct数值
    fun getCctData(appkey_index : Int,address : Int,isGroup : Boolean){

        mFragment.getAttachActivity().shearControl.let {

            it.operating_index = 0

            val sb = StringBuilder()
            if (isGroup){
                sb.append("true,")
            }else{
                sb.append("false,")
            }
            sb.append(address)

            it.operatingDatas = sb.toString()
            sb.clear()

            val key = it.mMeshManagerApi.meshNetwork?.appKeys?.get(appkey_index)

            if (key != null){
                val message = LightCtlGet(key)
                it.mMeshManagerApi.createMeshPdu(address,message)

                //获取打开关闭数值
                /*mFragment.postDelayed(Runnable {
                    val message2 = GenericOnOffGet(key)
                    it.mMeshManagerApi.createMeshPdu(address,message2)
                },500)*/
            }
        }
    }

    fun setOnOffOp(appkey_index : Int,address : Int,isGroup : Boolean,onOffStatue : Boolean){

        mFragment.getAttachActivity().shearControl.let {

            it.operating_index = 0

            val sb = StringBuilder()
            if (isGroup){
                sb.append("true,")
            }else{
                sb.append("false,")
            }
            sb.append(address)

            it.operatingDatas = sb.toString()
            sb.clear()

            val key = it.mMeshManagerApi.meshNetwork?.appKeys?.get(appkey_index)

            if (key != null){

                //获取打开关闭数值
                val message2 = GenericOnOffSet(key,onOffStatue, BaseInfoData.getNextTid())
                it.mMeshManagerApi.createMeshPdu(address,message2)

            }
        }

    }

    //清空状态
    fun setNotSelect(){

        data.forEachIndexed { index, baseNode ->

            if (baseNode is NControllerGruoupBean){
                baseNode.isSelect = false
            }

            if (baseNode is NControllerNoGroupBean){

                baseNode.isSelect = false
            }

            if (baseNode is NControllerItemBean){

                baseNode.isSelect = false
            }
        }
    }


    fun setCurLightness(mLightness : Int,mTemperature : Int,mDeltauv : Int){

        data.filter { it is NControllerGruoupBean && it.isSelect }.run {
            if (isNotEmpty()){
                val info = get(0) as NControllerGruoupBean
                info.lightness = mLightness
                info.temperature = mTemperature
                info.deltaUV = mDeltauv
                info.currentDeviceType = 1//cct

                BaseInfoData.scenes_cur?.let {

                    it.deviceGroups.filter { it.dgName == info.group_name &&
                            it.address == info.address.toString() }.let { gIt ->

                        if (gIt.isNotEmpty()){
                            gIt.get(0).devices.forEach { dIt ->
                                dIt.lightness = info.lightness!!
                                dIt.temperature = info.temperature!!
                                dIt.deltaUV = info.deltaUV!!
                                dIt.currentDeviceType = 1
                            }
                        }
                    }
                }
            }
        }

        data.filter { it is NControllerNoGroupBean && it.isSelect }.run {
            if (isNotEmpty()){
                val info = get(0) as NControllerNoGroupBean
                info.lightness = mLightness
                info.temperature = mTemperature
                info.deltaUV = mDeltauv
                info.currentDeviceType = 1//cct

                BaseInfoData.scenes_cur?.let {

                    it.deviceGroups.filter { it.isUngrouped }.let { gIt ->

                        if (gIt.isNotEmpty()){
                            gIt.get(0).devices.forEach { dIt ->

                                if (dIt.address == info.unicastAddress){

                                    dIt.lightness = info.lightness!!
                                    dIt.temperature = info.temperature!!
                                    dIt.deltaUV = info.deltaUV!!
                                    dIt.currentDeviceType = 1
                                }
                            }
                        }
                    }
                }
            }
        }

        notifyDataSetChanged()

        mFragment.getAttachActivity().shearControl.upLoadDatas()

        if (has_next == 1){

            //操作开关
            mFragment.postDelayed({
                setNext01()
            },400)

        }else if(has_next == 2){

            //操作亮度
            mFragment.postDelayed({
                setNext02()
            },400)

        }

    }

    fun setNext02(){

        mFragment.OpDatas(nextBeanString)
        //没有下一步
        has_next = 0
        nextBeanString = ""
    }

    fun setNext01(){

        nextBean?.let {

            if (it is NControllerGruoupBean){

                setOnOffOp(0,
                    it.address!!,
                    true,
                    !it.isIsPowserOn!!)

                //没有下一步
                has_next = 0
                nextBean = null

            }else if(it is NControllerNoGroupBean){

                setOnOffOp(
                    it.boundAppKeyIndexes!!,
                    it.unicastAddress!!,
                    false,
                    !it.isIsPowserOn!!)

                //没有下一步
                has_next = 0
                nextBean = null
            }

        }

    }

    fun setOnOff(hm : MutableMap<String,String>){

        hm.get("datas")?.let {

            val sp = it.split(",")

            if (sp[0] == "true"){

                data.filter { it is NControllerGruoupBean && it.address == sp[1].toInt()}.run {
                    if (isNotEmpty()){
                        val info = get(0) as NControllerGruoupBean
                        info.isIsPowserOn = hm.get("onoff")!!.toBoolean()

                        BaseInfoData.scenes_cur?.let { bIt ->
                            bIt.deviceGroups.filter { it.address == info.address.toString() }.run {
                                if (isNotEmpty()){

                                    get(0).devices.forEach {
                                        it.isIsPowserOn = info.isIsPowserOn!!
                                    }
                                }
                            }
                        }
                    }
                }

            }else{

                data.filter { it is NControllerNoGroupBean && it.unicastAddress ==  sp[1].toInt()}.run {
                    if (isNotEmpty()){
                        val info = get(0) as NControllerNoGroupBean
                        info.isIsPowserOn = hm.get("onoff")!!.toBoolean()

                        BaseInfoData.scenes_cur?.let { bIt ->
                            bIt.deviceGroups.filter { it.isUngrouped }.run {
                                if (isNotEmpty()){
                                    get(0).devices.filter { it.address == info.unicastAddress }.run {
                                        if (isNotEmpty()){
                                            get(0).isIsPowserOn = info.isIsPowserOn!!
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }

            notifyDataSetChanged()

            //更新数据
            mFragment.getAttachActivity().shearControl.upLoadDatas()
        }

    }

    //设置总开关
    fun setOnOffTotal(onoff : String){

        data.filterIsInstance<NControllerGruoupBean>().run {

            if (isNotEmpty()){

                forEach {
                    it.isIsPowserOn = onoff.toBoolean()
                }

            }
        }

        data.filterIsInstance<NControllerNoGroupBean>().run {

            if (isNotEmpty()){

                forEach {
                    it.isIsPowserOn = onoff.toBoolean()
                }

            }
        }

        notifyDataSetChanged()

        BaseInfoData.scenes_cur?.let {
            it.deviceGroups.forEach {
                it.devices.forEach {
                    it.isIsPowserOn = onoff.toBoolean()
                }
            }
        }

        mFragment.getAttachActivity().shearControl.upLoadDatas()
    }

    //设置总亮度
    fun setTotalLight(brighteness : String){

        data.filterIsInstance<NControllerGruoupBean>().run {

            if (isNotEmpty()){

                forEach {
                    it.lightness = brighteness.toInt()
                }

            }
        }

        data.filterIsInstance<NControllerNoGroupBean>().run {

            if (isNotEmpty()){

                forEach {
                    it.lightness = brighteness.toInt()
                }

            }
        }

        notifyDataSetChanged()

        BaseInfoData.scenes_cur?.let {
            it.deviceGroups.forEach {
                it.devices.forEach {
                    it.lightness = brighteness.toInt()
                }
            }
        }

        mFragment.getAttachActivity().shearControl.upLoadDatas()

    }

    //获取选中设备亮度
    fun getSelectData() : String{

        val sb = StringBuilder()

        data.filter { it is NControllerGruoupBean && it.isSelect  }.run {
            if (isNotEmpty()){
                (get(0) as NControllerGruoupBean)?.let {
                    sb.append(it.lightness)
                    sb.append(",")
                    sb.append(it.address)
                    sb.append(",")
                    sb.append(0)
                }
            }
        }

        data.filter { it is NControllerNoGroupBean && it.isSelect  }.run {
            if (isNotEmpty()){
                (get(0) as NControllerNoGroupBean)?.let {
                    sb.append(it.lightness)
                    sb.append(",")
                    sb.append(it.unicastAddress)
                    sb.append(",")
                    sb.append(it.boundAppKeyIndexes)
                }
            }
        }

        return sb.toString()
    }

    fun setCctBg(cct : Int,drawable : GradientDrawable) {

        var mR = 0
        var mG = 0
        var mB = 0

        val mCct = cct / 100

        if (mCct <= 66){

            mR = 255

        }else{

            mR = mCct - 60

            val bd01 = UtilsBigDecimal.mul(329.698727446,mR.toDouble().pow(-0.1332047592))

            mR = bd01.toInt()

            if (mR < 0){
                mR = 0
            }

            if (mR > 255){
                mR = 255
            }
        }


        if (mCct <= 66){

            mG = mCct

            val bd01 = UtilsBigDecimal.mul(99.4708025861, ln(mG.toDouble()))
            val bd02 = UtilsBigDecimal.sub(bd01,161.1195681661)
            mG = bd02.toInt()

            if (mG < 0){
                mG = 0
            }

            if (mG > 255){
                mG = 255
            }

        }else{

            mG = mCct - 60

            val bd01 = UtilsBigDecimal.mul(288.1221695283,mG.toDouble().pow(-0.0755148492))
            mG = bd01.toInt()

            if (mG < 0){
                mG = 0
            }

            if (mG > 255){
                mG = 255
            }
        }


        if (mCct >= 66){
            mB = 255
        }else{

            if (mCct <= 19){
                mB = 0
            }else{

                mB = mCct - 10

                val bd01 = UtilsBigDecimal.mul(138.5177312231, ln(mB.toDouble()))
                val bd02 = UtilsBigDecimal.sub(bd01,305.0447927307)

                mB = bd02.toInt()

                if (mB < 0){
                    mB = 0
                }

                if (mB > 255){
                    mB = 255
                }
            }
        }

        drawable.setColor(Color.rgb(mR,mG,mB))
    }

}