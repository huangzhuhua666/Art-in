package com.tw.artin.ui.adapter

import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tw.artin.R
import com.tw.artin.base.BaseInfoData
import com.tw.artin.bean.DevicesBean

class TopProvider : BaseItemProvider<DevicesBean>(){

    override val itemViewType: Int = 2

    override val layoutId: Int = R.layout.top_ditem

    override fun convert(helper: BaseViewHolder, item: DevicesBean) {

        if (item.address == 49152){
            helper.setText(R.id.tv_group_name,item.group_name)
        }else{
            val head = (BaseInfoData.getHeadIndex(item.address!!) + 65).toChar()
            helper.setText(R.id.tv_group_name,"$head ${item.group_name}")
        }

    }
}