package com.tw.artin.vp

import com.tw.artin.base.BaseInfVp
import no.nordicsemi.android.mesh.MeshManagerApi
import no.nordicsemi.android.mesh.MeshNetwork

class ControllerContract {

    interface Presenter : BaseInfVp.BasePresenter {

        fun getCctData(meshwork : MeshNetwork?)

    }

    interface View : BaseInfVp.BaseView {

    }

}