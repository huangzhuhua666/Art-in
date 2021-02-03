package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi


class ReadMessageApi(
    var ids : MutableList<Int> = mutableListOf()
) : IRequestApi {

    override fun getApi(): String {
        return "/push_message/updateStatuses.jhtml"
    }

}