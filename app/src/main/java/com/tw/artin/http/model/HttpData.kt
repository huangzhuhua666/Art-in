package com.tw.artin.http.model

import com.google.gson.annotations.SerializedName

data class HttpData<T>(
    var content : String = "",
    var time : String = "",
    var rmid : String = "",
    var type : String = "",
    var objx : T)


data class UpLoadData(
    @SerializedName("file_info")
    var fileInfo: FileInfo = FileInfo(),
    @SerializedName("message")
    var message: Message = Message(),
    @SerializedName("url")
    var url: String = ""
)

data class FileInfo(
    @SerializedName("isAuth")
    var isAuth: Boolean = false,
    @SerializedName("isZoom")
    var isZoom: Boolean = false,
    @SerializedName("largeHeight")
    var largeHeight: Int = 0,
    @SerializedName("largeWidth")
    var largeWidth: Int = 0,
    @SerializedName("mediumHeight")
    var mediumHeight: Int = 0,
    @SerializedName("mediumWidth")
    var mediumWidth: Int = 0,
    @SerializedName("name")
    var name: String = "",
    @SerializedName("source")
    var source: String = "",
    @SerializedName("thumbnailHeight")
    var thumbnailHeight: Int = 0,
    @SerializedName("thumbnailWidth")
    var thumbnailWidth: Int = 0,
    @SerializedName("url")
    var url: String = ""
)

data class Message(
    @SerializedName("content")
    var content: String = "",
    @SerializedName("type")
    var type: String = ""
)