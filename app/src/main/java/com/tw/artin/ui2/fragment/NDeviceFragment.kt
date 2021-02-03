package com.tw.artin.ui2.fragment

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.lxj.xpopup.XPopup
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwFragment
import com.tw.artin.bean.DevicesBean
import com.tw.artin.MainTabActivity2
import com.tw.artin.ui2.adapter.NDeviceListAdapter
import com.tw.artin.view.CreatGroupPop2
import com.tw.artin.view.UniversalItemDecoration
import com.tw.artin.vp2.NDeviceFragmentPresenter
import kotlinx.android.synthetic.main.device_fragment.*

class NDeviceFragment : TwFragment<MainTabActivity2>(){

    val presenter by lazy {
        NDeviceFragmentPresenter(this)
    }

    val mAdapter by lazy {

        NDeviceListAdapter(this).apply {

            animationEnable = false

            draggableModule.isDragEnabled = true

            draggableModule.setOnItemDragListener(object : OnItemDragListener {

                override fun onItemDragMoving(
                    source: RecyclerView.ViewHolder?,
                    from: Int,
                    target: RecyclerView.ViewHolder?,
                    to: Int
                ) {
                }

                override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                }

                override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                    setMoveGroup(pos)
                }
            })
        }
    }

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.device_fragment
    }

    override fun initView() {

        BusUtils.register(this)

        with(device_recy){

            (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

            layoutManager = LinearLayoutManager(getAttachActivity())

            addItemDecoration(object : UniversalItemDecoration(){
                override fun getItemOffsets(position: Int): Decoration {
                    return ColorDecoration().apply {
                        bottom = 1
                        decorationColor = Color.parseColor("#363636")
                    }
                }
            })

            adapter = mAdapter
        }

    }

    override fun initData() {
    }

    override fun onRightClick(v: View?) {
        super.onRightClick(v)


        BaseInfoData.scenes_cur?.let {

            XPopup.Builder(getAttachActivity())
                .hasShadowBg(false)
                .autoOpenSoftInput(true)
                .asCustom(
                    CreatGroupPop2(getAttachActivity(), this,null,null).apply {

                        setOnSelectListener(object : CreatGroupPop2.InputListener{
                            override fun onInputData(txt: String) {

                                getAttachActivity().shearControl.creatMeshGroup(txt)
                            }
                        })
                    }
                )
                .show()

        }?: ToastUtils.showShort(R.string.scene_t02)

    }

    override fun isStatusBarEnabled(): Boolean {
        return !super.isStatusBarEnabled()
    }

    override fun onDestroy() {
        BusUtils.unregister(this)
        super.onDestroy()
    }

    //处理更新数据
    @BusUtils.Bus(tag = "init_mesh_success",sticky = true,threadMode = BusUtils.ThreadMode.MAIN)
    fun onEvent0(){

        mAdapter.setNewInstance(
            mutableListOf(
                DevicesBean(type = 0,isSearch = false)
            )
        )

        presenter.searchBle()
    }

    @BusUtils.Bus(tag = "search_device",sticky = true)
    fun onEvent1(isBluetooth : Boolean){

        //开启蓝牙成功
        if (isBluetooth){

            presenter.starSearchBle()

            getAttachActivity().shearControl.let {

                if (it.select_link.isEmpty()){

                    if (getAttachActivity().isBluetooth){
                        it.startScan()
                    }
                }
            }

        }else{
            //关闭蓝牙
            presenter.stopBle()

            getAttachActivity().shearControl.stopScan()
        }
    }

    //处理更新数据
    @BusUtils.Bus(tag = "deal_with",sticky = true)
    fun onEvent2(){
        mAdapter.DealWith()
    }

    //绑定成功后删除 新设备中数据
    @BusUtils.Bus(tag = "remove_scan",sticky = true)
    fun onEvent3(bluetooth_name : String){
        mAdapter.moveScanData(bluetooth_name)
    }

    //处理更新数据
    @BusUtils.Bus(tag = "onLineStatue",sticky = true)
    fun onEvent4(){

        mAdapter.DealWith()

        getAttachActivity().shearControl.let {

            if (!it.checkDevOnline() && it.select_link.isEmpty()){

                if (getAttachActivity().isBluetooth){
                    it.startScan()
                }

            }else{

                it.stopScan()
            }
        }
    }

    //处理更新数据
    @BusUtils.Bus(tag = "getDfuList",sticky = true)
    fun onEvent5(){

        val call_back = mAdapter.data.filter { it.type == 2 || it.type == 3 }

        BusUtils.postSticky("setDfuData",call_back)
    }

    override fun onResume() {
        super.onResume()
        mAdapter.notifyDataSetChanged()
    }


}