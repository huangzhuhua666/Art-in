package com.tw.artin.ui.adapter

import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tw.artin.R
import com.tw.artin.bean.DevicesBean

class TopItemProvider : BaseItemProvider<DevicesBean>(){

    override val itemViewType: Int = 3

    override val layoutId: Int = R.layout.top_ditem01

    override fun convert(helper: BaseViewHolder, item: DevicesBean) {

        helper.setText(R.id.tv_not_name,item.note_name)
    }
}