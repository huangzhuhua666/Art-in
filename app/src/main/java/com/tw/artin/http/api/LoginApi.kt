package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestServer
import com.hjq.http.model.BodyType
import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestType


class LoginApi(
    var language : String = "",
    var username : String = "",
    var password : String = "",
    var mobile : String? = "",
    var email : String? = ""
) : IRequestApi, IRequestType {

    override fun getApi(): String {
        return "/member/login.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }

    data class LoginBean(
        @SerializedName("address")
        var address: Any = Any(),
        @SerializedName("bCreater")
        var bCreater: Any = Any(),
        @SerializedName("bModifier")
        var bModifier: String = "",
        @SerializedName("birth")
        var birth: Any = Any(),
        @SerializedName("companyInfoId")
        var companyInfoId: Int = 0,
        @SerializedName("createDate")
        var createDate: Long = 0,
        @SerializedName("email")
        var email: Any = Any(),
        @SerializedName("id")
        var id: Int = 0,
        @SerializedName("imageName")
        var imageName: Any = Any(),
        @SerializedName("isEnabled")
        var isEnabled: Boolean = false,
        @SerializedName("language")
        var language: String = "",
        @SerializedName("loginDate")
        var loginDate: Any = Any(),
        @SerializedName("loginIp")
        var loginIp: Any = Any(),
        @SerializedName("meshConfig")
        var meshConfig: MeshConfig = MeshConfig(),
        @SerializedName("mobile")
        var mobile: String = "",
        @SerializedName("modifyDate")
        var modifyDate: Long = 0,
        @SerializedName("name")
        var name: Any = Any(),
        @SerializedName("password")
        var password: String = "",
        @SerializedName("registerIp")
        var registerIp: Any = Any(),
        @SerializedName("token")
        var token: String = "",
        @SerializedName("username")
        var username: String = "",
        @SerializedName("zipCode")
        var zipCode: Any = Any()
    )

    data class MeshConfig(
        @SerializedName("bCreater")
        var bCreater: String = "",
        @SerializedName("bModifier")
        var bModifier: String = "",
        @SerializedName("companyInfoId")
        var companyInfoId: Int = 0,
        @SerializedName("createDate")
        var createDate: Long = 0,
        @SerializedName("id")
        var id: Int = 0,
        @SerializedName("meshConfig")
        var meshConfig: String = "",
        @SerializedName("modifyDate")
        var modifyDate: Long = 0,
        @SerializedName("userId")
        var userId: Int = 0
    )


}