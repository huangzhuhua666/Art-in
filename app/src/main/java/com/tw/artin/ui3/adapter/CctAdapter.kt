package com.tw.artin.ui3.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.blankj.utilcode.util.BusUtils
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.tw.artin.base.BaseInfoData
import com.tw.artin.bean.NControllerGruoupBean
import com.tw.artin.bean.NControllerItemBean
import com.tw.artin.bean.NControllerNoGroupBean
import com.tw.artin.ui3.fragment.ControllerFragment
import com.tw.artin.util.UtilsBigDecimal
import no.nordicsemi.android.mesh.transport.GenericOnOffSet
import no.nordicsemi.android.mesh.transport.LightCtlGet
import kotlin.math.ln
import kotlin.math.pow

class CctAdapter(val mFragment: ControllerFragment) : BaseNodeAdapter() {

    //0没有下一步  1选中后，操作开关   2选中后，操作亮度
    var has_next = 0

    var nextBean : BaseNode? = null
    var nextBeanString: String = ""

    init {
        addNodeProvider(CctGProvider(mFragment))
        addNodeProvider(CctItemProvider())
        addNodeProvider(CctNGProvider(mFragment))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {

        return when (data[position]) {

            is NControllerGruoupBean -> 0

            is NControllerItemBean -> 1

            is NControllerNoGroupBean -> 2

            else -> -1
        }
    }

    //选中设备或分组
    fun selectItem(pos: Int, hasGetData: Boolean) {

        if (CheckModel(pos)) {
            return
        }

        var mAddress = 0
        var mIsGroup = false

        val infos = data[pos]
        if (infos is NControllerGruoupBean) {
            if (infos.deviceType == 2) {
                BusUtils.postSticky("gone_hsi", true)
                BusUtils.post("refresh_top_data")
            } else {
                BusUtils.postSticky("gone_hsi", false)
                BusUtils.post("refresh_top_data")
            }
        } else if (infos is NControllerNoGroupBean) {
            if (infos.deviceType == 2) {
                BusUtils.postSticky("gone_hsi", true)
                BusUtils.post("refresh_top_data")
            } else {
                BusUtils.postSticky("gone_hsi", false)
                BusUtils.post("refresh_top_data")
            }
        }

        data.forEachIndexed { index, baseNode ->

            when (baseNode) {

                is NControllerGruoupBean -> {

                    if (index == pos) {
                        mAddress = baseNode.address!!.toInt()
                        mIsGroup = true
                    }

                    baseNode.isSelect = index == pos

                    baseNode.childNode?.forEach {
                        it as NControllerItemBean
                        it.isSelect = index == pos
                    }
                }

                is NControllerNoGroupBean -> {

                    if (index == pos) {
                        mAddress = baseNode.unicastAddress!!.toInt()
                        mIsGroup = false
                    }

                    baseNode.isSelect = index == pos
                }
            }
        }

        notifyDataSetChanged()

        if (hasGetData) {
            getCctData(0, mAddress, mIsGroup)
        }
    }

    fun CheckModel(pos: Int): Boolean {

        var isGo = false

        val sData = data[pos]

        if (sData is NControllerGruoupBean) {
            //非手动选择 且 非当前状态  跳转
            if (sData.currentDeviceType != 1 && !mFragment.mHeadClick) {
                BusUtils.postSticky("move_controller", sData.currentDeviceType!! - 1)
                isGo = true
            }
        } else if (sData is NControllerNoGroupBean) {
            //非手动选择 且 非当前状态  跳转
            if (sData.currentDeviceType != 1 && !mFragment.mHeadClick) {
                BusUtils.postSticky("move_controller", sData.currentDeviceType!! - 1)
                isGo = true
            }
        }

        return isGo

    }

    //获取cct数值
    fun getCctData(appkey_index: Int, address: Int, isGroup: Boolean) {

        mFragment.getAttachActivity().shearControl.let {

            it.operating_index = 0

            val sb = StringBuilder()
            if (isGroup) {
                sb.append("true,")
            } else {
                sb.append("false,")
            }
            sb.append(address)

            it.operatingDatas = sb.toString()
            sb.clear()

            val key = it.mMeshManagerApi.meshNetwork?.appKeys?.get(appkey_index)

            if (key != null) {
                val message = LightCtlGet(key)
                it.mMeshManagerApi.createMeshPdu(address, message)
            }
        }
    }

    fun setCctBg(cct: Int, drawable: GradientDrawable) {

        var mR = 0
        var mG = 0
        var mB = 0

        val mCct = cct / 100

        if (mCct <= 66) {

            mR = 255

        } else {

            mR = mCct - 60

            val bd01 = UtilsBigDecimal.mul(329.698727446, mR.toDouble().pow(-0.1332047592))

            mR = bd01.toInt()

            if (mR < 0) {
                mR = 0
            }

            if (mR > 255) {
                mR = 255
            }
        }


        if (mCct <= 66) {

            mG = mCct

            val bd01 = UtilsBigDecimal.mul(99.4708025861, ln(mG.toDouble()))
            val bd02 = UtilsBigDecimal.sub(bd01, 161.1195681661)
            mG = bd02.toInt()

            if (mG < 0) {
                mG = 0
            }

            if (mG > 255) {
                mG = 255
            }

        } else {

            mG = mCct - 60

            val bd01 = UtilsBigDecimal.mul(288.1221695283, mG.toDouble().pow(-0.0755148492))
            mG = bd01.toInt()

            if (mG < 0) {
                mG = 0
            }

            if (mG > 255) {
                mG = 255
            }
        }


        if (mCct >= 66) {
            mB = 255
        } else {

            if (mCct <= 19) {
                mB = 0
            } else {

                mB = mCct - 10

                val bd01 = UtilsBigDecimal.mul(138.5177312231, ln(mB.toDouble()))
                val bd02 = UtilsBigDecimal.sub(bd01, 305.0447927307)

                mB = bd02.toInt()

                if (mB < 0) {
                    mB = 0
                }

                if (mB > 255) {
                    mB = 255
                }
            }
        }

        drawable.setColor(Color.rgb(mR, mG, mB))
    }

    fun setOnOffOp(appkey_index : Int,address : Int,isGroup : Boolean,onOffStatue : Boolean){

        mFragment.getAttachActivity().shearControl.let {

            it.operating_index = 0

            val sb = java.lang.StringBuilder()
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
}