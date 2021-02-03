package com.tw.artin.ui2.fragment

import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.gcssloop.widget.ArcSeekBar
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwFragment
import com.tw.artin.bean.NControllerGruoupBean
import com.tw.artin.bean.NControllerNoGroupBean
import com.tw.artin.MainTabActivity2
import com.tw.artin.ui2.adapter.NCctAdapter
import com.tw.artin.vp.ControllerContract
import com.tw.artin.util.UtilsBigDecimal
import kotlinx.android.synthetic.main.cct_fragment.*
import no.nordicsemi.android.mesh.transport.LightCtlSet

class NCctFragment : TwFragment<MainTabActivity2>(), ControllerContract.View{

    var mOnOff = false

    //[0] gm  [1]色温  [2]组适配器  [3]节点
    // [0](gm 0显示  1滑动  2点击)
    // [1](色温 0显示  1滑动  2点击)
    // [2](组设备 0显示  1滑动)
    // [3](设备 0显示  1滑动)
    val changeList = mutableListOf(0,0,0,0)

    //是否接收总开关
    var isReceiveData_total_onOff = false

    val mAdapter by lazy {
        NCctAdapter(this)
    }

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.cct_fragment
    }

    override fun initView() {

        BusUtils.register(this)

        // G/M
        with(rsb_bar){

            setOnRangeChangedListener(object : OnRangeChangedListener {

                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                    changeList[0] = 1
                }

                override fun onRangeChanged(
                    view: RangeSeekBar?,
                    leftValue: Float,
                    rightValue: Float,
                    isFromUser: Boolean
                ) {

                    if (!isFromUser && changeList[0] == 2){

                        val text = UtilsBigDecimal.GetIntString(leftValue.toDouble())

                        if (text == "-0"){
                            tv_gm.text = "0"
                        }else{
                            tv_gm.text = text
                        }

                        OpDatas(null)

                        changeList[0] = 0

                        /*if (!isReceiveData){
                            OpDatas(null)
                        }else{
                            isReceiveData = false
                        }*/


                    }else{

                        if (leftValue<-50){
                            view?.setProgress(-50f)
                        }

                        if (leftValue>50){
                            view?.setProgress(50f)
                        }

                    }
                }

                override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

                    if (isLeft && changeList[0] == 1){

                        val leftValue = view?.leftSeekBar?.progress!!

                        val text = UtilsBigDecimal.GetIntString(leftValue.toDouble())

                        if (text == "-0"){
                            tv_gm.text = "0"
                        }else{
                            tv_gm.text = text
                        }

                        OpDatas(null)

                        changeList[0] = 0
                    }
                }

            })

            setProgress(0f)

        }

        //半圆
        with(arc_bar){

            View.GONE

            val mWidth = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(60f)

            layoutParams.let {
                it.width = mWidth
                it.height = mWidth
            }

            (tv_k01.layoutParams as FrameLayout.LayoutParams).bottomMargin = SizeUtils.dp2px(mWidth/10f)

            (tv_k02.layoutParams as FrameLayout.LayoutParams).leftMargin = SizeUtils.dp2px(mWidth/6.6f)

            setOnProgressChangeListener(object : ArcSeekBar.OnProgressChangeListener{

                override fun onProgressChanged(
                    seekBar: ArcSeekBar?,
                    progress: Int,
                    isUser: Boolean
                ) {

                    LogUtils.dTag("temp onProgressChanged","isUser = $isUser progress = $progress")

                    if (!isUser && changeList[1] == 2){
                        val pro = seekBar?.progress
                        tv_cct.text = "${pro}K"
                        nScroll.requestDisallowInterceptTouchEvent(false)

                        OpDatas(null)

                        changeList[1] = 0

                        /*if (!isReceiveData2){
                            OpDatas(null)
                        }else{
                            isReceiveData2 = false
                        }*/

                    }
                }

                override fun onStartTrackingTouch(seekBar: ArcSeekBar?) {
                    changeList[1] = 1
                    nScroll.requestDisallowInterceptTouchEvent(true)
                }

                override fun onStopTrackingTouch(seekBar: ArcSeekBar?) {

                    if (changeList[1] == 1){

                        val pro = seekBar?.progress
                        tv_cct.text = "${pro}K"
                        nScroll.requestDisallowInterceptTouchEvent(false)

                        OpDatas(null)

                        changeList[1] = 0
                    }

                    /*if (!isReceiveData2){
                        OpDatas(null)
                    }else{
                        isReceiveData2 = false
                    }*/
                }

            })

            progress = 2000

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

                        changeList[2] = 0
                        changeList[3] = 0

                        mAdapter.setTotalLight(text)
                    }
                }

            })

        }

        with(cct_recycle){
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(getAttachActivity())
            adapter = mAdapter
        }

        setOnClickListener(R.id.tv_zero,R.id.tv_k01,R.id.tv_k02,R.id.iv_onoff)

    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v?.id){

            R.id.tv_zero -> {
                //点击
                changeList[0] = 2
                rsb_bar.setProgress(0f)
            }

            R.id.tv_k01 -> {
                //点击
                changeList[1] = 2
                arc_bar.progress = 3200
            }

            R.id.tv_k02 -> {
                //点击
                changeList[1] = 2
                arc_bar.progress = 5600
            }

            R.id.iv_onoff -> {

                if (mOnOff){
                    mOnOff = false
                    iv_onoff.setImageResource(R.mipmap.shebei_btn_off)
                }else{
                    mOnOff = true
                    iv_onoff.setImageResource(R.mipmap.shebei_btn_on)
                }

                isReceiveData_total_onOff = true
                getAttachActivity().shearControl.setTotalOnOff(mOnOff,0)

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

        val c1 = tv_cct.text.toString()
        val cct = c1.substring(0,c1.length-1)

        //色温
        val da02 = cct.toInt()

        val gm1 = tv_gm.text.toString()
        val gm2 = UtilsBigDecimal.div(gm1.toDouble(),50.0)
        val gm3 = UtilsBigDecimal.mul(gm2,32768.0)

        // g/m
        var da03 = UtilsBigDecimal.GetFInt(gm3).toInt()
        if (da03 < -32768){
            da03 = -32768
        }

        if (da03 > 32767){
            da03 = 32767
        }


        setCctData(
            mData[1].toInt(),
            mData[2].toInt(),
            da01,
            da02,
            da03
        )

    }

    fun setCctData(address : Int, appkeyindex : Int,lightLightness : Int,
                   lightTemperature : Int,lightDeltaUv : Int){

        getAttachActivity().shearControl.let {

            val appkey = it.mMeshManagerApi.meshNetwork?.getAppKey(appkeyindex)

            val ctl_set = LightCtlSet(appkey!!,
                lightLightness,
                lightTemperature,
                lightDeltaUv,
                BaseInfoData.getNextTid())

            it.mMeshManagerApi.createMeshPdu(address,ctl_set)
        }
    }

    @BusUtils.Bus(tag = "getCctData",sticky = true)
    fun onEvent1(datas : MutableMap<String,Float>){

        datas.get("temperature")?.toInt()?.let {
            changeList[1] = 0
            arc_bar.progress = it
            tv_cct.text = "${it}K"
        }


        datas.get("deltauv")?.let {
            changeList[0] = 0
            rsb_bar.setProgress(it)
            tv_gm.text = it.toInt().toString()
        }

        datas.get("lightness")?.let {
            changeList[2] = 0
            changeList[3] = 0
            mAdapter.setCurLightness(it.toInt(),arc_bar.progress,rsb_bar.progressLeft)
        }

    }

    @BusUtils.Bus(tag = "cctOnOff",sticky = true)
    fun onEvent2(datas : MutableMap<String,String>){

        if (isReceiveData_total_onOff){

            val kg = datas.get("onoff")

            mAdapter.setOnOffTotal(kg!!)

            isReceiveData_total_onOff = false

        }else{
            mAdapter.setOnOff(datas)
        }

    }

    override fun onResume() {
        super.onResume()
        getLedData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            getLedData()
        }
    }

    fun getLedData(){

        changeList[0] = 0
        changeList[1] = 0
        changeList[2] = 0
        changeList[3] = 0

        parentFragment?.let {
            it as NControllerFragment

            val datas = it.getScenesDatas()

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

                    if (infos.lightness == 0 && infos.temperature == 0 && infos.deltaUV == 0){
                        has_get_data = true
                    }

                    if (!has_get_data){

                        rsb_bar.setProgress(infos.deltaUV!!.toFloat())
                        tv_gm.text = infos.deltaUV.toString()

                        arc_bar.progress = infos.temperature!!.toInt()
                        tv_cct.text = "${infos.temperature}K"

                    }

                }else if (infos is NControllerNoGroupBean){

                    if (infos.lightness == 0 && infos.temperature == 0 && infos.deltaUV == 0){
                        has_get_data = true
                    }

                    if (!has_get_data){

                        rsb_bar.setProgress(infos.deltaUV!!.toFloat())
                        tv_gm.text = infos.deltaUV.toString()

                        arc_bar.progress = infos.temperature!!.toInt()
                        tv_cct.text = "${infos.temperature}K"
                    }

                }

                mAdapter.selectItem(it.mCurrentIndex,has_get_data)

            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BusUtils.unregister(this)
    }
}