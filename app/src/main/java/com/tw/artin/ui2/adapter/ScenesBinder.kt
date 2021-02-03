package com.tw.artin.ui2.adapter

import android.annotation.SuppressLint
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.binder.QuickItemBinder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnInputConfirmListener
import com.tw.artin.R
import com.tw.artin.base.BaseInfoData
import com.tw.artin.bean.DelNoteBean
import com.tw.artin.http.api.ScenesListApi
import com.tw.artin.ui2.fragment.ScenesFragment
import kotlinx.android.synthetic.main.scenes_fragment.*
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
class ScenesBinder(val mFragment : ScenesFragment) : QuickItemBinder<ScenesListApi.Content>() {

    val sdf by lazy {
        SimpleDateFormat("yyyy-MM-dd")
    }

    init {
        addChildClickViewIds(R.id.iv_scen_more)
    }

    override fun convert(holder: BaseViewHolder, data: ScenesListApi.Content) {

        holder.setText(R.id.tv_scenes01,data.scenesName)

        if (data.isCurrent){
            holder.setText(R.id.tv_scenes02,context.getString(R.string.led_info_t16))
        }else{
            holder.setText(R.id.tv_scenes02,"")
        }

        holder.setText(R.id.tv_scenes03,
            "${context.getString(R.string.led_info_t17)}${data.deviceCount}")

        holder.setText(R.id.tv_scenes04,
            "${context.getString(R.string.led_info_t18)}${data.deviceGroupCount}")

        holder.setText(R.id.tv_scenes05,
            "${context.getString(R.string.led_info_t19)}${sdf.format(data.createDate)}")

        holder.setText(R.id.tv_scenes06,
            "${context.getString(R.string.led_info_t20)}${sdf.format(data.modifyDate)}")

    }

    override fun getLayoutId(): Int {
        return R.layout.scenes_item_binder
    }

    override fun onChildClick(
        holder: BaseViewHolder,
        view: View,
        data: ScenesListApi.Content,
        position: Int
    ) {
        super.onChildClick(holder, view, data, position)

        val str01 = context.getString(R.string.device_text09)
        val str02 = context.getString(R.string.led_info_t21)
        val str03 = context.getString(R.string.led_info_t22)

        val s_data  = if (data.isCurrent){
            arrayOf(str01,str02,str03)
        }else{
            arrayOf(str01,str03)
        }

        XPopup.Builder(context)
            .hasShadowBg(false)
            .atView(view) // 依附于所点击的View，内部会自动判断在上方或者下方显示
            .asAttachList(
                s_data,
                intArrayOf(0)
            ) { position, text ->

                if (text == str01){
                    //重命名
                    showRnameDialog(data)

                }else if(text == str02){
                    //更新场景
                    mFragment.getAttachActivity().shearControl.ChuLiAllData01()
                    mFragment.postDelayed({
                        mFragment.getList()
                    },500)

                }else{
                    //删除
                    mFragment.delScenes(data)
                }
            }
            .bindLayout(R.layout.bind_edit_layout)
            .bindItemLayout(R.layout.edit_bind_item)
            .show()

    }

    fun showRnameDialog(info : ScenesListApi.Content){

        XPopup.Builder(context)
            .hasShadowBg(false)
            .autoOpenSoftInput(true)
            .asInputConfirm(
                context.getString(R.string.device_text09),"",info.scenesName,
                context.getString(R.string.led_info_t14),
                object : OnInputConfirmListener {
                    override fun onConfirm(text: String?) {

                        text?.let {

                            if (it.isEmpty()){
                                ToastUtils.showShort(R.string.led_info_t14)
                                return
                            }

                            info.scenesName = it

                            mFragment.upDataScenes(info)
                        }
                    }
                }
            )
            .bindLayout(R.layout.dialog_input_message)
            .show()

    }

    override fun onClick(
        holder: BaseViewHolder,
        view: View,
        data: ScenesListApi.Content,
        position: Int
    ) {
        super.onClick(holder, view, data, position)

        if (data.isCurrent){
            return
        }

        XPopup.Builder(context)
            .hasShadowBg(false)
            .asConfirm(context.getString(R.string.exit_app_t01),
                context.getString(R.string.led_info_t25)
            )
            {

                if (data.isCurrent){
                    return@asConfirm
                }

                adapter.data.filter { it is ScenesListApi.Content && it.isCurrent }.run {

                    if (isNotEmpty()){

                        BaseInfoData.scenes_node_del_change.clear()
                        BaseInfoData.scenes_node_add_change.clear()

                        BaseInfoData.scenes_cur?.let {

                            it.deviceGroups.forEach {it01 ->

                                if (it01.devices.isNotEmpty() && !it01.isUngrouped){

                                    it01.devices.forEach {it02 ->

                                        BaseInfoData.scenes_node_del_change.add(
                                            DelNoteBean(it02.address,it01.address.toInt())
                                        )
                                    }
                                }
                            }

                            //删除没节点数据
                            val node_it = BaseInfoData.scenes_node_del_change.iterator()
                            while (node_it.hasNext()){
                                val info = node_it.next()
                                if (info.node_address == 0){
                                    node_it.remove()
                                }
                            }

                            mFragment.Select_pos = position

                            //没订阅任何组，直接切换
                            if (BaseInfoData.scenes_node_del_change.isEmpty()){

                                mFragment.getAttachActivity().shearControl.operating_mesh = 7
                                mFragment.onEvent()
                            }else{
                                //删除分组订阅
                                mFragment.getAttachActivity().shearControl.delGroupMoreNode()
                            }

                        }

                    }else{

                        mFragment.Select_pos = 0

                        mFragment.getAttachActivity().shearControl.operating_mesh = 7
                        mFragment.onEvent()
                    }
                }
            }
            .bindLayout(R.layout.dialog_message)
            .show()

    }



}