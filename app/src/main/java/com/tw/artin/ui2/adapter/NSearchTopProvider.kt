package com.tw.artin.ui2.adapter

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tw.artin.R
import com.tw.artin.base.BaseInfoData
import com.tw.artin.bean.DevicesBean
import com.tw.artin.bean.LinkSelect
import com.tw.artin.ui2.fragment.NDeviceFragment

class NSearchTopProvider(val mFragment : NDeviceFragment) : BaseItemProvider<DevicesBean>() {

    override val itemViewType: Int = 0

    override val layoutId: Int = R.layout.search_top_item

    init {
        addChildClickViewIds(R.id.iv_play, R.id.tv_all_select, R.id.tv_add, R.id.iv_stop)
    }

    override fun convert(helper: BaseViewHolder, item: DevicesBean) {

        val rotate = AnimationUtils.loadAnimation(context,R.anim.rotate_anim)

        if (item.isSearch!!){

            helper.setGone(R.id.iv_play,true)

            helper.setGone(R.id.iv_xz,false)
            //旋转图片
            helper.getView<ImageView>(R.id.iv_xz).apply {
                animation = rotate
                startAnimation(rotate)
            }

            helper.setGone(R.id.iv_stop,false)

        }else{

            helper.setGone(R.id.iv_play,false)

            helper.setGone(R.id.iv_xz,true)
            //旋转图片
            helper.getView<ImageView>(R.id.iv_xz).apply {
                clearAnimation()
            }

            helper.setGone(R.id.iv_stop,true)
        }

    }

    override fun onChildClick(
        helper: BaseViewHolder,
        view: View,
        data: DevicesBean,
        position: Int
    ) {

        when(view.id){

            R.id.iv_play -> {
                mFragment.presenter.searchBle()
            }

            R.id.iv_stop ->{
                mFragment.presenter.stopBle()
            }

            R.id.tv_all_select -> {

                getAdapter()?.data?.let { datas ->

                    datas.filter { it.type == 1 }.run {
                        forEach {
                            it.isSelect = true
                        }
                    }

                    getAdapter()?.notifyDataSetChanged()
                }
            }

            R.id.tv_add -> {

                if (BaseInfoData.scenes_cur == null){
                    ToastUtils.showShort(R.string.scene_t02)
                    return
                }

                getAdapter()?.data?.let { datas ->

                    datas.filter { it.type == 1 && it.isSelect == true}.run {

                        if (isEmpty()){
                            ToastUtils.showShort(R.string.device_scan_error2)
                        }else{

                            mFragment.getAttachActivity().shearControl.let {

                                //清空选择的需要连接的设备
                                it.select_link.clear()

                                mapIndexed { index, devicesBean ->

                                    LinkSelect(
                                        devicesBean.scan_datas!!,
                                        index == 0
                                    )
                                }.run {
                                    it.select_link.addAll(this)

                                    //停止扫描
                                    //mFragment.presenter.stopBle()

                                    //配网
                                    it.operating_mesh = 3

                                    it.getLinkData()?.let { lIt ->
                                        it.connetBle(lIt.scan_datas)
                                    }
                                }

                            }
                        }
                    }

                    getAdapter()?.notifyDataSetChanged()
                }

            }

        }

    }

}