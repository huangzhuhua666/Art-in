package com.tw.artin.ui2.fragment

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwFragment
import com.tw.artin.bean.EffectBean
import com.tw.artin.bean.NControllerGruoupBean
import com.tw.artin.bean.NControllerNoGroupBean
import com.tw.artin.MainTabActivity2
import com.tw.artin.ui2.adapter.NEffectAdapter
import com.tw.artin.ui2.adapter.NEffectBinder
import com.tw.artin.util.UtilsBigDecimal
import com.tw.artin.view.UniversalItemDecoration
import kotlinx.android.synthetic.main.effect_fragment.*
import kotlinx.android.synthetic.main.effect_fragment.iv_onoff
import kotlinx.android.synthetic.main.effect_fragment.total_bar
import no.nordicsemi.android.mesh.transport.VendorModelMessageAcked
import java.nio.ByteBuffer
import java.nio.ByteOrder

class NEffectFragment : TwFragment<MainTabActivity2>(){

    var mOnOff = false
    var isReceiveData_total_onOff = false

    // [0]组适配器  [1]节点
    // [0](组设备 0显示  1滑动)
    // [1](设备 0显示  1滑动)
    val changeList = mutableListOf(0,0)

    val mAdapter01 by lazy {
        BaseBinderAdapter().apply {

            addItemBinder(NEffectBinder())

            setOnItemClickListener { adapter, view, position ->

                val info = adapter.getItem(position) as EffectBean

                if (!info.isSelect){

                    data.forEach {
                        it as EffectBean
                        it.isSelect = it.name01 == info.name01
                    }

                    adapter.notifyDataSetChanged()

                    OpDatas(null)
                }
            }
        }
    }

    val mAdapter02 by lazy {
        NEffectAdapter(this)
    }

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.effect_fragment
    }

    override fun initView() {

        BusUtils.register(this)

        with(effect_recycle01){
            layoutManager = GridLayoutManager(getAttachActivity(),3)

            addItemDecoration(object : UniversalItemDecoration(){
                override fun getItemOffsets(position: Int): Decoration {

                    val decoration = ColorDecoration()
                    decoration.decorationColor = Color.BLACK

                    if (position % 3 == 0){
                        decoration.right = ConvertUtils.dp2px(4f)
                    }else if (position % 3 == 1){
                        decoration.left = ConvertUtils.dp2px(4f)
                        decoration.right = ConvertUtils.dp2px(4f)
                    }else{
                        decoration.left = ConvertUtils.dp2px(4f)
                    }

                    return decoration
                }
            })

            adapter = mAdapter01
        }

        with(total_bar){

            setOnRangeChangedListener(object : OnRangeChangedListener {

                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                }

                override fun onRangeChanged(
                    view: RangeSeekBar?,
                    leftValue: Float,
                    rightValue: Float,
                    isFromUser: Boolean
                ) {

                    if (isFromUser){
                        if (leftValue < 1){
                            view?.setProgress(1f)
                        }
                    }

                }

                override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

                    if (isLeft){
                        val leftValue = view?.leftSeekBar?.progress

                        val text = UtilsBigDecimal.GetIntString(leftValue!!.toDouble())
                        val da01 = UtilsBigDecimal.mul(text.toDouble(),655.35).toInt()

                        getAttachActivity().shearControl.setTotalLightness(da01)

                        changeList[0] = 0
                        changeList[1] = 0

                        mAdapter02.setTotalLight(text)
                    }

                }

            })

        }

        with(effect_recycle02){
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(getAttachActivity())
            adapter = mAdapter02
        }

        setOnClickListener(R.id.iv_onoff)

    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v?.id){

            R.id.iv_onoff ->{

                if (mOnOff){
                    mOnOff = false
                    iv_onoff.setImageResource(R.mipmap.shebei_btn_off)
                }else{
                    mOnOff = true
                    iv_onoff.setImageResource(R.mipmap.shebei_btn_on)
                }

                isReceiveData_total_onOff = true
                getAttachActivity().shearControl.setTotalOnOff(mOnOff,2)

            }
        }
    }


    fun OpDatas(ld : String?){

        val data01 = mAdapter02.getSelectData()
        if (data01 == ""){
            return
        }

        val data02 = getSelectEffect()
        if (data02 == ""){
            return
        }

        val mData = data01.split(",")

        val mData2 = data02.split(",")

        var da01 = 1

        if (ld == null){
            //亮度
            da01 = UtilsBigDecimal.mul(mData[0].toDouble(),655.35).toInt()
        }else{
            //亮度
            da01 = UtilsBigDecimal.mul(ld.toDouble(),655.35).toInt()
        }

        setEffectData(
            mData[1].toInt(),
            mData[2].toInt(),
            da01,
            mData2[0].toInt(),
            mData2[1].toInt()
        )
    }

    fun getSelectEffect() : String{

        mAdapter01.data.filter { it is EffectBean && it.isSelect }.run {

            if (isEmpty()){
                return ""
            }

            val sb = StringBuilder()

            val info = get(0) as EffectBean

            sb.append(info.effect)
            sb.append(",")
            sb.append(info.preset)

            val txt = sb.toString()
            sb.clear()

            return txt
        }
    }

    fun setEffectData(address : Int, appkeyindex : Int,lightLightness : Int,
                      effect : Int,preset : Int){

        getAttachActivity().shearControl.let {

            getAttachActivity().shearControl.vendorModelMessage_type = 0

            val appkey = it.mMeshManagerApi.meshNetwork?.getAppKey(appkeyindex)

            val message = VendorModelMessageAcked(
                appkey!!,
                0x1888,
                0x0059,
                0x06 or 0xC0,
                getEffectParameter(lightLightness,effect,preset)
            )

            LogUtils.dTag("effect setAddress",address)
            it.mMeshManagerApi.createMeshPdu(address,message)
        }
    }

    fun getEffectParameter(lightLightness : Int,effect : Int,preset : Int) : ByteArray{

        val mBu = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)

        mBu.putShort(lightLightness.toShort())
        mBu.put(effect.toByte())//效果类型
        mBu.put(preset.toByte())//具体类型
        mBu.put(BaseInfoData.getNextTid().toByte())

        return mBu.array()
    }

    fun getLedData(){

        changeList[0] = 0
        changeList[1] = 0

        parentFragment?.let {
            it as NControllerFragment

            val datas = it.getScenesDatas()

            mAdapter02.setList(datas)

            if (datas.isNotEmpty()){

                //默认选中第一个
                if (it.mCurrentIndex == -1){
                    it.mCurrentIndex = 0
                }

                if (it.mCurrentIndex > mAdapter02.itemCount - 1){
                    it.mCurrentIndex = 0
                }

                var has_get_data = false
                val infos = mAdapter02.getItem(it.mCurrentIndex)

                if (infos is NControllerGruoupBean){
                    if (infos.lightness == 0 && infos.effect == 0 && infos.preset == 0){
                        has_get_data = true
                    }

                    //仅支持cct
                    if (infos.deviceType == 2){
                        mAdapter01.setList(BaseInfoData.getEffectList02())
                    }else{
                        mAdapter01.setList(BaseInfoData.getEffectList01())
                    }

                    setSelectEffect(infos.effect!!,infos.preset!!)

                }else if (infos is NControllerNoGroupBean){
                    if (infos.lightness == 0 && infos.hue == 0 && infos.saturation == 0){
                        has_get_data = true
                    }

                    //仅支持cct
                    if (infos.deviceType == 2){
                        mAdapter01.setList(BaseInfoData.getEffectList02())
                    }else{
                        mAdapter01.setList(BaseInfoData.getEffectList01())
                    }

                    setSelectEffect(infos.effect!!,infos.preset!!)
                }

                mAdapter02.selectItem(it.mCurrentIndex,has_get_data)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            getLedData()
        }
    }

    override fun onResume() {
        super.onResume()
        getLedData()
    }

    override fun onDestroy() {
        BusUtils.unregister(this)
        super.onDestroy()
    }

    @BusUtils.Bus(tag = "EffectOnOff",sticky = true)
    fun onEvent1(datas : MutableMap<String,String>){

        if (isReceiveData_total_onOff){

            val kg = datas.get("onoff")

            mAdapter02.setOnOffTotal(kg!!)

            isReceiveData_total_onOff = false

        }else{
            mAdapter02.setOnOff(datas)
        }
    }


    @BusUtils.Bus(tag = "EffectGet",sticky = true)
    fun onEvent2(data : MutableMap<String,Int>){

        val mLightness = UtilsBigDecimal.div(data.get("lightness")!!.toDouble(),655.35).toInt()

        mAdapter01.data.forEach {
            it as EffectBean
            it.isSelect = it.effect == data.get("effect") && it.preset == data.get("preset")
        }
        mAdapter01.notifyDataSetChanged()

        mAdapter02.setCurLightness(mLightness,
            data.get("effect")!!.toInt(),
            data.get("preset")!!.toInt())
    }

    fun setSelectEffect(mEffect : Int,mPreset: Int){

        mAdapter01.data.forEach {
            it as EffectBean
            it.isSelect = it.effect == mEffect && it.preset == mPreset
        }
        mAdapter01.notifyDataSetChanged()

    }


}