package com.tw.artin.ui2.adapter

import android.graphics.Color
import android.view.View
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tw.artin.R
import com.tw.artin.bean.DevicesBean

class NSearchItemProvider : BaseItemProvider<DevicesBean>() {

    override val itemViewType: Int = 1

    override val layoutId: Int = R.layout.search_item

    override fun convert(helper: BaseViewHolder, item: DevicesBean) {

        item.scan_datas?.scanRecord?.let {
            helper.setText(R.id.tv_s_name,it.deviceName)
        }

        if (item.isSelect!!){
            helper.setBackgroundColor(R.id.layout_content, Color.parseColor("#464040"))
        }else{
            helper.setBackgroundColor(R.id.layout_content, Color.BLACK)
        }

    }

    override fun onClick(helper: BaseViewHolder, view: View, data: DevicesBean, position: Int) {

        data.isSelect = !data.isSelect!!
        getAdapter()?.notifyItemChanged(position)
    }

}