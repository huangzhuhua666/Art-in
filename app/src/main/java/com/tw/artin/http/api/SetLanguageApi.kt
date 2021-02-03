package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi

class SetLanguageApi(
    var language : String = ""
) : IRequestApi {

    override fun getApi(): String {
        return "/member/updateLanguage.jhtml"
    }



}