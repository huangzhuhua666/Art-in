package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType
import com.google.gson.annotations.SerializedName


class DfuApi(
    var model : String = ""
) : IRequestApi,IRequestType {

    override fun getApi(): String {
        return "/firmware/findByModel.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }

    data class DfuBean(
        @SerializedName("attach")
        var attach: String = "",
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
        @SerializedName("md5")
        var md5: String = "",
        @SerializedName("model")
        var model: String = "",
        @SerializedName("modifyDate")
        var modifyDate: Long = 0,
        @SerializedName("remark")
        var remark: String = "",
        @SerializedName("version")
        var version: String = ""
    )

}