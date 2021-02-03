package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi

class UpDataScenesApi(
    var id : Int = 0,
    var scenesName : String? = "",
    var isCurrent : Boolean? = false,
) : IRequestApi {

    override fun getApi(): String {
        return "/scenes/updateScenes.jhtml"
    }
}