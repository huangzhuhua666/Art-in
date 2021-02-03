package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType
import com.google.gson.annotations.SerializedName


class MyInfoApi : IRequestApi, IRequestType {

    override fun getApi(): String {
        return "/member/getUserInfo.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }

    data class MyInfoBean(
        @SerializedName("address")
        var address: String = "",
        @SerializedName("bCreater")
        var bCreater: String = "",
        @SerializedName("bModifier")
        var bModifier: String = "",
        @SerializedName("birth")
        var birth: String = "",
        @SerializedName("companyInfoId")
        var companyInfoId: Int = 0,
        @SerializedName("createDate")
        var createDate: Long = 0,
        @SerializedName("email")
        var email: String = "",
        @SerializedName("id")
        var id: Int = 0,
        @SerializedName("imageName")
        var imageName: String = "",
        @SerializedName("isEnabled")
        var isEnabled: Boolean = false,
        @SerializedName("language")
        var language: String = "",
        @SerializedName("loginDate")
        var loginDate: String = "",
        @SerializedName("loginIp")
        var loginIp: String = "",
        @SerializedName("meshConfig")
        var meshConfig: String = "",
        @SerializedName("mobile")
        var mobile: String = "",
        @SerializedName("modifyDate")
        var modifyDate: Long = 0,
        @SerializedName("name")
        var name: String = "",
        @SerializedName("password")
        var password: String = "",
        @SerializedName("registerIp")
        var registerIp: String = "",
        @SerializedName("token")
        var token: String = "",
        @SerializedName("username")
        var username: String = "",
        @SerializedName("zipCode")
        var zipCode: String = ""
    )

}