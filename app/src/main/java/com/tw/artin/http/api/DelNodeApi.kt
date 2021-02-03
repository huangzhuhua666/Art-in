package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType

class DelNodeApi(
    val address : Int
) : IRequestApi,IRequestType {

    override fun getApi(): String {
        return "/scenes/deleteDevicesByAddress.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }
}