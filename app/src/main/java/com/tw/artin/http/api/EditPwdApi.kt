package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType

class EditPwdApi(var password : String) : IRequestApi, IRequestType {

    override fun getApi(): String {
        return "/member/updatePassword.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }
}