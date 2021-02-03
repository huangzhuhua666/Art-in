package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi

class FeekBackApi(
    var title : String = "",
    var content : String = "",
    var attach1 : String? = "",
    var attach2 : String? = "",
    var attach3 : String? = ""
) : IRequestApi {

    override fun getApi(): String {
        return "/feedback/submit.jhtml"
    }
}