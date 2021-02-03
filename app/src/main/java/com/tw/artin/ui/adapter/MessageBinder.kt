package com.tw.artin.ui.adapter

import com.chad.library.adapter.base.binder.QuickItemBinder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tw.artin.R
import com.tw.artin.http.api.MessageApi
import java.text.SimpleDateFormat
import java.util.*

class MessageBinder : QuickItemBinder<MessageApi.Content>(){

    val sdf by lazy {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    override fun convert(holder: BaseViewHolder, data: MessageApi.Content) {

        holder.setText(R.id.tv_msg_title,data.title)

        holder.setText(R.id.tv_msg_title,sdf.format(data.publishDate))

        holder.setText(R.id.tv_msg_content,data.content)

    }

    override fun getLayoutId(): Int {
        return R.layout.item_msg_binder
    }

}