package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType

class SendMsgApi(
    var account : String = "",
    var type : Int = 1
) : IRequestApi, IRequestType {

    override fun getApi(): String {
        return "/member/sendCode.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }


}