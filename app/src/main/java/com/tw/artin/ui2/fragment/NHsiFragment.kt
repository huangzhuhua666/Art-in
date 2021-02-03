package com.tw.artin.ui2.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.ScreenUtils
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwFragment
import com.tw.artin.bean.NControllerGruoupBean
import com.tw.artin.bean.NControllerNoGroupBean
import com.tw.artin.MainTabActivity2
import com.tw.artin.ui2.adapter.NHsiAdapter
import com.tw.artin.util.UtilsBigDecimal
import kotlinx.android.synthetic.main.cct_fragment.*
import kotlinx.android.synthetic.main.hsi_fragment.*
import kotlinx.android.synthetic.main.hsi_fragment.iv_onoff
import kotlinx.android.synthetic.main.hsi_fragment.total_bar
import no.nordicsemi.android.mesh.transport.LightHslSet
import java.text.DecimalFormat

class NHsiFragment : TwFragment<MainTabActivity2>() {

    //旋转角度
    var angle = 0

    var mOnOff = false

    // [0]组适配器  [1]节点
    // [0](组设备 0显示  1滑动)
    // [1](设备 0显示  1滑动)
    val changeList = mutableListOf(0,0)

    var mHue : Float = 0f
    var mSat : Float = 0f

    //是否接收总开关
    var isReceiveData_total_onOff = false

    val mAdapter by lazy {
        NHsiAdapter(this)
    }

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.hsi_fragment
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {

        BusUtils.register(this)

        with(hsi_color){

            hsi_color.layoutParams.apply {
                width =  ScreenUtils.getScreenWidth()/2
                height =  ScreenUtils.getScreenWidth()/2
            }

            setOnTouchListener { v, event ->

                if (event.action == MotionEvent.ACTION_DOWN){
                    hsi_nscroll.requestDisallowInterceptTouchEvent(true)
                }

                if (event.action == MotionEvent.ACTION_UP){
                    hsi_nscroll.requestDisallowInterceptTouchEvent(false)
                }

                return@setOnTouchListener false
            }

            setColorListener(object : ColorEnvelopeListener {

                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {

                    if (fromUser){

                        envelope?.let {

                            val hsv = floatArrayOf(0f,0f,0f)
                            Color.colorToHSV(it.color, hsv)

                            mSat = hsv[1]
                            mHue = hsv[0]

                            OpDatas(null)

                        }
                    }
                }
            })

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

                        mAdapter.setTotalLight(text)
                    }

                }

            })

        }

        with(hsi_recycle){
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(getAttachActivity())
            adapter = mAdapter
        }

        setOnClickListener(R.id.iv_xz,R.id.iv_onoff)

    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v?.id){

            R.id.iv_xz -> setRotation()

            R.id.iv_onoff ->{

                if (mOnOff){
                    mOnOff = false
                    iv_onoff.setImageResource(R.mipmap.shebei_btn_off)
                }else{
                    mOnOff = true
                    iv_onoff.setImageResource(R.mipmap.shebei_btn_on)
                }

                isReceiveData_total_onOff = true
                getAttachActivity().shearControl.setTotalOnOff(mOnOff,1)

            }
        }
    }

    fun OpDatas(ld : String?){

        val data01 = mAdapter.getSelectData()
        if (data01 == ""){
            return
        }

        val mData = data01.split(",")

        var da01 = 1

        if (ld == null){
            //亮度
            da01 = UtilsBigDecimal.mul(mData[0].toDouble(),655.35).toInt()
        }else{
            //亮度
            da01 = UtilsBigDecimal.mul(ld.toDouble(),655.35).toInt()
        }

        val lightHue1 = UtilsBigDecimal.mul(mHue.toDouble(),182.0416)
        val lightHue2 = lightHue1.toInt()

        val lightSaturation = DecimalFormat("0.00").format(mSat).toDouble()
        val lightSaturation1 = UtilsBigDecimal.mul(lightSaturation,65535.0)
        var lightSaturation2 = lightSaturation1.toInt()

        if (lightSaturation2 > 65535){
            lightSaturation2 = 65535
        }

        setHsiData(
            mData[1].toInt(),
            mData[2].toInt(),
            da01,
            lightHue2,
            lightSaturation2
        )
    }

    fun setHsiData(address : Int, appkeyindex : Int,lightLightness : Int,
                   lightHue : Int,lightSaturation : Int){

        getAttachActivity().shearControl.let {

            val appkey = it.mMeshManagerApi.meshNetwork?.getAppKey(appkeyindex)

            val hsi_set = LightHslSet(appkey!!,
                lightLightness,
                lightHue,
                lightSaturation,
                BaseInfoData.getNextTid())

            it.mMeshManagerApi.createMeshPdu(address,hsi_set)
        }
    }

    private fun setRotation(){

        if (angle == 360){
            angle = 0
        }else{
            angle += 90
        }

        hsi_color.rotation = angle.toFloat()
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


    fun getLedData(){

        changeList[0] = 0
        changeList[1] = 0

        parentFragment?.let {
            it as NControllerFragment

            val datas = it.getScenesDatas()

            //去掉仅支持cct的分组
            val mIt = datas.iterator()
            while (mIt.hasNext()){

                val mNext = mIt.next()

                if (mNext is NControllerGruoupBean){

                    //隐藏仅支持cct的分组
                    if (mNext.deviceType == 2){
                        mIt.remove()
                    }

                }else if(mNext is NControllerNoGroupBean){

                    //隐藏仅支持cct的分组
                    if (mNext.deviceType == 2){
                        mIt.remove()
                    }
                }
            }

            mAdapter.setList(datas)

            if (datas.isNotEmpty()){

                //默认选中第一个
                if (it.mCurrentIndex == -1){
                    it.mCurrentIndex = 0
                }

                if (it.mCurrentIndex > mAdapter.itemCount - 1){
                    it.mCurrentIndex = 0
                }

                var has_get_data = false
                val infos = mAdapter.getItem(it.mCurrentIndex)

                if (infos is NControllerGruoupBean){
                    if (infos.lightness == 0 && infos.hue == 0 && infos.saturation == 0){
                        has_get_data = true
                    }

                    if (!has_get_data){
                        mHue = infos.hue!!.toFloat()
                        tv_hue.text = "${infos.hue!!.toInt()}°"

                        val sta = infos.saturation!!
                        tv_sat.text = "$sta%"
                        mSat = UtilsBigDecimal.div(sta.toDouble(),100.0).toFloat()
                    }

                }else if (infos is NControllerNoGroupBean){
                    if (infos.lightness == 0 && infos.hue == 0 && infos.saturation == 0){
                        has_get_data = true
                    }

                    if (!has_get_data){
                        mHue = infos.hue!!.toFloat()
                        tv_hue.text = "${infos.hue!!.toInt()}°"

                        val sta = infos.saturation!!
                        tv_sat.text = "$sta%"
                        mSat = UtilsBigDecimal.div(sta.toDouble(),100.0).toFloat()
                    }
                }

                val hsi = floatArrayOf(
                    mHue,
                    mSat,
                    1f
                )

                val color = Color.HSVToColor(hsi)

                hsi_color.setInitialColor(color)

                mAdapter.selectItem(it.mCurrentIndex,has_get_data)

            }
        }
    }

    @BusUtils.Bus(tag = "hsiOnOff",sticky = true)
    fun onEvent1(datas : MutableMap<String,String>){

        if (isReceiveData_total_onOff){

            val kg = datas.get("onoff")

            mAdapter.setOnOffTotal(kg!!)

            isReceiveData_total_onOff = false

        }else{
            mAdapter.setOnOff(datas)
        }

    }

    @BusUtils.Bus(tag = "getHsiData",sticky = true)
    fun onEvent2(datas : MutableMap<String,Float>){

        tv_hue.text = "${datas.get("hue")!!.toInt()}°"
        mHue = datas.get("hue")!!.toFloat()

        val sta = UtilsBigDecimal.mul(datas.get("sat")!!.toDouble(),100.0).toInt()
        tv_sat.text = "${sta}%"
        mSat = datas.get("sat")!!.toFloat()

        datas.get("lightness")?.let {

            mAdapter.setCurLightness(it.toInt(),
                datas.get("hue")!!.toInt(),
                sta)

        }

        val hsi = floatArrayOf(
            datas.get("hue")!!,
            datas.get("sat")!!,
            1f
        )

        val color = Color.HSVToColor(hsi)

        hsi_color.setInitialColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BusUtils.unregister(this)
    }


}