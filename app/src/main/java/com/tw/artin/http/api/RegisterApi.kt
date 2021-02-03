package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi

class RegisterApi (
    var code : String = "",
    var password : String = "",
    var username : String = "",
    var mobile : String? = "",
    var email : String? = ""
) : IRequestApi {

    override fun getApi(): String {
        return "/member/registered.jhtml"
    }


}