package com.tw.artin.ui2.adapter

import android.view.View
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxj.xpopup.XPopup
import com.tw.artin.R
import com.tw.artin.base.BaseInfoData
import com.tw.artin.bean.DevicesBean
import com.tw.artin.ui2.fragment.NDeviceFragment
import com.tw.artin.view.CreatGroupPop2

class NGroupProvider (val mFragment : NDeviceFragment) : BaseItemProvider<DevicesBean>() {

    override val itemViewType: Int = 2

    override val layoutId: Int = R.layout.notgroup_top_item

    init {
        addChildClickViewIds(R.id.iv_group_edit)
    }

    override fun convert(helper: BaseViewHolder, item: DevicesBean) {

        if (item.address == 49152){
            helper.setText(R.id.tv_group_name,item.group_name)
        }else{
            val head = (BaseInfoData.getHeadIndex(item.address!!) + 65).toChar()
            helper.setText(R.id.tv_group_name,"$head ${item.group_name}")
        }

        if (item.isNotGroup!!){
            helper.setGone(R.id.iv_group_edit,true)
        }else{
            helper.setGone(R.id.iv_group_edit,false)
        }

    }


    override fun onChildClick(helper: BaseViewHolder, view: View, data: DevicesBean, position: Int) {

        val str01 = context.getString(R.string.device_text09)
        val str02 = context.getString(R.string.device_text10)
        XPopup.Builder(context)
            .hasShadowBg(false)
            .atView(view) // 依附于所点击的View，内部会自动判断在上方或者下方显示
            .asAttachList(
                arrayOf(str01, str02),
                intArrayOf(0)
            ) { position, text ->

                //重命名
                if (position == 0){

                    showInputDialog(data.group_name!!,data.address!!)

                }else{
                    //删除
                    getAdapter()?.data?.filter { it.type == 3 && it.group_child_name == data.group_name }?.let {

                        if (it.isEmpty()){
                            //直接删除分组
                            mFragment.getAttachActivity().shearControl.removeGroup(data.address!!)

                        }else{

                            it.forEach {dbIt ->

                                mFragment.getAttachActivity()
                                    .shearControl.delGroupNode(
                                        dbIt.group_address!!,
                                        dbIt.deviceuuid.toString())

                            }

                            mFragment.getAttachActivity().shearControl.removeGroup(data.address!!)

                        }
                    }

                }

            }
            .bindItemLayout(R.layout.edit_bind_item)
            .bindLayout(R.layout.bind_edit_layout)
            .show()


    }

    private fun showInputDialog(g_name : String, address : Int){

        val str03 = context.getString(R.string.device_text12)

        XPopup.Builder(context)
            .hasShadowBg(false)
            .autoOpenSoftInput(true)
            .asCustom(
                CreatGroupPop2(context, mFragment,g_name,str03).apply {

                    setOnSelectListener(object : CreatGroupPop2.InputListener{
                        override fun onInputData(txt: String) {

                            mFragment.getAttachActivity().shearControl.editGroupName(txt,address)

                        }
                    })
                }
            )
            .show()

    }

}