package com.tw.artin.http.api

import com.hjq.http.config.IRequestApi

class UpLoadMeshConfigApi(
    var meshConfig : String
) : IRequestApi {

    override fun getApi(): String {
        return "/mesh_config/updateMeshConfig.jhtml"
    }

}