package com.tw.artin.ui3.adapter

import com.chad.library.adapter.base.binder.QuickItemBinder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tw.artin.R

/**
 * Create by hzh on 2021/02/05.
 */
class TextBinder : QuickItemBinder<String>() {

    override fun getLayoutId(): Int = R.layout.item_text

    override fun convert(holder: BaseViewHolder, data: String) {
        holder.setText(R.id.tv, data)
    }
}