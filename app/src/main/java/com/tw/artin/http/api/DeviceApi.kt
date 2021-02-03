package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.google.gson.annotations.SerializedName


class DeviceApi(
    val keyword : String = "",
    val pn : Int = 1,
    val ps : Int = 30
) : IRequestApi {

    override fun getApi(): String {
        return "/openapi/product/findList"
    }

    class DeviceListData : ArrayList<DeviceListDataItem>()

    data class DeviceListDataItem(
        @SerializedName("ext_param")
        var extParam: String = "",
        @SerializedName("image")
        var image: String = "",
        @SerializedName("name")
        var name: String = "",
        @SerializedName("sn")
        var sn: String = ""
    )

}