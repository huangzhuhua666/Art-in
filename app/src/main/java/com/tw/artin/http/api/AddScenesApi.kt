package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType
import com.tw.artin.bean.NewDeviceGroupBean

class AddScenesApi(
    var scenesName : String = "",
    var isCurrent : Boolean = false,
    var deviceGroups : MutableList<NewDeviceGroupBean>? = null
) : IRequestApi {

    override fun getApi(): String {
        return "/scenes/createScenes.jhtml"
    }

}