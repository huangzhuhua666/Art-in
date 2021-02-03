package com.tw.artin.vp

import com.tw.artin.base.common.TwActivity
import no.nordicsemi.android.mesh.MeshManagerApi


class MainPresenter(
    val mView: MainContract.View,
    val mActivity: TwActivity
) : MainContract.Presenter {

    override fun initBle(api: MeshManagerApi) {

    }


    override fun onDestroy() {

    }
}