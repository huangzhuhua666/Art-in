package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType

class DelScenesApi(
    var id : Int = 0
) : IRequestApi,IRequestType {

    override fun getApi(): String {
        return "/scenes/deleteScenes.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }
}