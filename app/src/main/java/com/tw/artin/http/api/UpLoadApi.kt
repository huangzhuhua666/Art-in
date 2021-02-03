package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestType
import com.hjq.http.model.BodyType
import java.io.File

class UpLoadApi(
    var file : File
)  : IRequestApi, IRequestType {

    override fun getApi(): String {
        return "/common/file/upload.jhtml"
    }

    override fun getType(): BodyType {
        return BodyType.FORM
    }
}