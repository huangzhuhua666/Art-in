package com.tw.artin.ui2.adapter

import android.graphics.Color
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tw.artin.R
import com.tw.artin.bean.NControllerItemBean

class NHsiItemProvider : BaseNodeProvider() {

    override val itemViewType: Int = 1

    override val layoutId: Int = R.layout.cct_item

    override fun convert(helper: BaseViewHolder, item: BaseNode) {

        if(item is NControllerItemBean){

            helper.setText(R.id.tv_cct_note_name,item.note_name)

            if (item.isSelect){
                helper.setBackgroundColor(R.id.tv_cct_note_name, Color.parseColor("#585858"))
                helper.setBackgroundColor(R.id.v_kong, Color.parseColor("#585858"))
                helper.setGone(R.id.v_select,false)
            }else{
                helper.setBackgroundColor(R.id.tv_cct_note_name, Color.BLACK)
                helper.setBackgroundColor(R.id.v_kong, Color.BLACK)
                helper.setGone(R.id.v_select,true)
            }

        }

    }
}