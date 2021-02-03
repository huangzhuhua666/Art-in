package com.tw.artin.http

import com.hjq.http.config.IRequestServer
import com.hjq.http.model.BodyType

class ReleaseServer : IRequestServer {

    override fun getHost(): String {
        return "http://mng.art-in.5mall.com"
    }

    override fun getType(): BodyType {
        return BodyType.JSON
    }

}