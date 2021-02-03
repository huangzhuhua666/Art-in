package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType
import com.google.gson.annotations.SerializedName


class GetMeshConfigApi  : IRequestApi, IRequestType {

    override fun getApi(): String {
        return "/mesh_config/getMeshConfig.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }

    data class MConfigBean(
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