package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType
import com.google.gson.annotations.SerializedName


class MessageApi(
    var title : String? = "",
    var pageNumber : Int? = 1,
    var pageSize : Int? = 100
) : IRequestApi, IRequestType {

    override fun getApi(): String {
        return "/push_message/findPage.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }

    data class MsgBean(
        @SerializedName("content")
        var content: List<Content> = listOf(),
        @SerializedName("pageNumber")
        var pageNumber: Int = 0,
        @SerializedName("pageSize")
        var pageSize: Int = 0,
        @SerializedName("pageable")
        var pageable: Pageable = Pageable(),
        @SerializedName("total")
        var total: Int = 0,
        @SerializedName("totalPages")
        var totalPages: Int = 0
    )

    data class Content(
        @SerializedName("bCreater")
        var bCreater: String = "",
        @SerializedName("bModifier")
        var bModifier: String = "",
        @SerializedName("cancelDate")
        var cancelDate: Any = Any(),
        @SerializedName("companyInfoId")
        var companyInfoId: Int = 0,
        @SerializedName("conditions")
        var conditions: Int = 0,
        @SerializedName("content")
        var content: String = "",
        @SerializedName("createDate")
        var createDate: Long = 0,
        @SerializedName("id")
        var id: Int = 0,
        @SerializedName("modifyDate")
        var modifyDate: Long = 0,
        @SerializedName("order")
        var order: Any = Any(),
        @SerializedName("publishDate")
        var publishDate: Long = 0,
        @SerializedName("status")
        var status: Int = 0,
        @SerializedName("title")
        var title: String = ""
    )

    data class Pageable(
        @SerializedName("pageNumber")
        var pageNumber: Int = 0,
        @SerializedName("pageSize")
        var pageSize: Int = 0
    )


}