package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType
import com.google.gson.annotations.SerializedName
import com.tw.artin.bean.NewDeviceGroupBean


class ScenesListApi(
    var scenesName : String? = "",
    var pageNumber : String? = "1",
    var pageSize : String? = "100"
) : IRequestApi, IRequestType {

    override fun getApi(): String {
        return "/scenes/findPage.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }

    data class ScenesBean(
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
        @SerializedName("address")
        var address: String = "",
        @SerializedName("applicationKey")
        var applicationKey: String = "",
        @SerializedName("bCreater")
        var bCreater: String = "",
        @SerializedName("bModifier")
        var bModifier: String = "",
        @SerializedName("companyInfoId")
        var companyInfoId: Int = 0,
        @SerializedName("createDate")
        var createDate: Long = 0,
        @SerializedName("deviceCount")
        var deviceCount: Int = 0,
        @SerializedName("deviceGroupCount")
        var deviceGroupCount: Int = 0,
        @SerializedName("deviceGroups")
        var deviceGroups: MutableList<NewDeviceGroupBean> = mutableListOf(),
        @SerializedName("id")
        var id: Int = 0,
        @SerializedName("isCurrent")
        var isCurrent: Boolean = false,
        @SerializedName("modifyDate")
        var modifyDate: Long = 0,
        @SerializedName("order")
        var order: Any = Any(),
        @SerializedName("scenesName")
        var scenesName: String = "",
        @SerializedName("userId")
        var userId: Int = 0
    )

    data class Pageable(
        @SerializedName("pageNumber")
        var pageNumber: Int = 0,
        @SerializedName("pageSize")
        var pageSize: Int = 0
    )

    /*data class DeviceGroup(
        @SerializedName("address")
        var address: String = "",
        @SerializedName("bCreater")
        var bCreater: String = "",
        @SerializedName("bModifier")
        var bModifier: String = "",
        @SerializedName("companyInfoId")
        var companyInfoId: Int = 0,
        @SerializedName("createDate")
        var createDate: Long = 0,
        @SerializedName("devices")
        var devices: List<Device> = listOf(),
        @SerializedName("dgName")
        var dgName: String = "",
        @SerializedName("id")
        var id: Int = 0,
        @SerializedName("modifyDate")
        var modifyDate: Long = 0,
        @SerializedName("order")
        var order: Any = Any(),
        @SerializedName("scenesId")
        var scenesId: Int = 0,
        @SerializedName("ungrouped")
        var ungrouped: Boolean = false
    )



    data class Device(
        @SerializedName("address")
        var address: Int = 0,
        @SerializedName("bCreater")
        var bCreater: String = "",
        @SerializedName("bModifier")
        var bModifier: String = "",
        @SerializedName("companyInfoId")
        var companyInfoId: Int = 0,
        @SerializedName("createDate")
        var createDate: String = "",
        @SerializedName("currentDeviceType")
        var currentDeviceType: Int = 0,
        @SerializedName("deltaUV")
        var deltaUV: Int = 0,
        @SerializedName("deviceGroupId")
        var deviceGroupId: Int = 0,
        @SerializedName("deviceType")
        var deviceType: Int = 0,
        @SerializedName("deviceid")
        var deviceid: String = "",
        @SerializedName("effect")
        var effect: Int = 0,
        @SerializedName("hue")
        var hue: Int = 0,
        @SerializedName("id")
        var id: Int = 0,
        @SerializedName("isPowserOn")
        var isPowserOn: Boolean = false,
        @SerializedName("lightness")
        var lightness: Int = 0,
        @SerializedName("model")
        var model: String = "",
        @SerializedName("modifyDate")
        var modifyDate: String = "",
        @SerializedName("name")
        var name: String = "",
        @SerializedName("order")
        var order: Int = 0,
        @SerializedName("orginName")
        var orginName: String = "",
        @SerializedName("preset")
        var preset: Int = 0,
        @SerializedName("saturation")
        var saturation: Int = 0,
        @SerializedName("temperature")
        var temperature: Int = 0,
        @SerializedName("userId")
        var userId: Int = 0
    )*/

}