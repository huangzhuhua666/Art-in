package com.tw.artin.ui.adapter

import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.binder.QuickItemBinder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnInputConfirmListener
import com.tw.artin.R
import com.tw.artin.http.api.ScenesListApi
import com.tw.artin.ui.fragment.NScenesFragment
import java.text.SimpleDateFormat

class NScenesBinder(val mFragment : NScenesFragment) : QuickItemBinder<ScenesListApi.Content>() {

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
            //arrayOf(str01,str02)
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
                    mFragment.getAttachActivity().mainControl.ChuLiAllData01()
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

                            mFragment.upDataScenes(info,info.applicationKey)
                        }
                    }
                }
            )
            .bindLayout(R.layout.dialog_input_message)
            .show()

    }

}