package com.tw.artin.vp

import com.tw.artin.base.BaseInfVp
import no.nordicsemi.android.mesh.MeshManagerApi

class MainContract {

    interface Presenter : BaseInfVp.BasePresenter {

        //初始化蓝牙和各种监听
        fun initBle(api : MeshManagerApi)

    }

    interface View : BaseInfVp.BaseView {

        fun getHomeIdOk()
    }

}