package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.tw.artin.bean.NewDeviceGroupBean

class UpDataNScenesApi(
    var id : Int = 0,
    var scenesName : String? = "",
    var isCurrent : Boolean? = false,
    var applicationKey : String? = "",
    var deviceGroups : MutableList<NewDeviceGroupBean>? = null
) : IRequestApi {

    override fun getApi(): String {
        return "/scenes/updateScenes.jhtml"
    }
}