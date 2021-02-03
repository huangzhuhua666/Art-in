package com.tw.artin.ui.adapter

import com.chad.library.adapter.base.BaseProviderMultiAdapter
import com.tw.artin.bean.DevicesBean

class DfuAdapter  : BaseProviderMultiAdapter<DevicesBean>() {

    init {
        addItemProvider(TopProvider())
        addItemProvider(TopItemProvider())
    }

    override fun getItemType(data: List<DevicesBean>, position: Int): Int {
        return data[position].type
    }

}