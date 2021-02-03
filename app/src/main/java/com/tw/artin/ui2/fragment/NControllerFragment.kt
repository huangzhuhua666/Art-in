package com.tw.artin.ui2.fragment

import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.BusUtils.ThreadMode
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwFragment
import com.tw.artin.bean.NControllerGruoupBean
import com.tw.artin.bean.NControllerItemBean
import com.tw.artin.bean.NControllerNoGroupBean
import com.tw.artin.MainTabActivity2
import com.tw.artin.util.FragmentUtils
import com.tw.artin.view.OnTabSelectListener
import kotlinx.android.synthetic.main.controller_fragment.*
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode

class NControllerFragment : TwFragment<MainTabActivity2>(){

    var cct_fragment: NCctFragment? = null
    var hsi_fragment: NHsiFragment? = null
    var effect_fragment: NEffectFragment? = null

    var main_pos = 0

    //默认选中的组
    var mCurrentIndex = -1
    var mHeadClick = false

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.controller_fragment
    }

    override fun initView() {

        BusUtils.register(this)

        getStatusBarConfig()?.statusBarView(v_view)?.init()

        tab_top_layout.setTabsNotViewPager(
            arrayListOf("CCT","HSI","EFFECT")
        )

        tab_top_layout.setOnTabSelectListener(object : OnTabSelectListener {

            override fun onTabSelect(position: Int) {
                if (position != main_pos){
                    mHeadClick = true
                    changeFragment(position)
                }
            }

            override fun onTabReselect(position: Int) {
                LogUtils.d("aaa")
            }
        })

        cct_fragment = childFragmentManager.findFragmentByTag(
            NCctFragment::class.java.name) as NCctFragment?

        if (cct_fragment == null) {
            cct_fragment = NCctFragment()
            FragmentUtils.addFragmentWithTag(
                childFragmentManager,
                cct_fragment,
                R.id.container_layout,
                NCctFragment::class.java.name
            )
        } else {
            FragmentUtils.showFragment(childFragmentManager, cct_fragment)
        }
    }

    override fun initData() {
    }

    private fun changeFragment(pos: Int) {
        if (main_pos == pos)
            return
        main_pos = pos
        when (main_pos) {

            0 -> {

                cct_fragment = childFragmentManager.findFragmentByTag(
                    NCctFragment::class.java.name) as NCctFragment?

                if (cct_fragment == null) {
                    cct_fragment = NCctFragment()
                    FragmentUtils.addFragmentWithTag(
                        childFragmentManager,
                        cct_fragment,
                        R.id.container_layout,
                        NCctFragment::class.java.name
                    )
                } else {
                    FragmentUtils.showFragment(childFragmentManager, cct_fragment)
                }

            }

            1 -> {

                hsi_fragment = childFragmentManager.findFragmentByTag(
                    NHsiFragment::class.java.name) as NHsiFragment?

                if (hsi_fragment == null) {
                    hsi_fragment = NHsiFragment()
                    FragmentUtils.addFragmentWithTag(
                        childFragmentManager,
                        hsi_fragment,
                        R.id.container_layout,
                        NHsiFragment::class.java.name
                    )
                } else {
                    FragmentUtils.showFragment(childFragmentManager, hsi_fragment)
                }

            }
            2 -> {

                effect_fragment = childFragmentManager.findFragmentByTag(
                    NEffectFragment::class.java.name) as NEffectFragment?

                if (effect_fragment == null) {
                    effect_fragment = NEffectFragment()
                    FragmentUtils.addFragmentWithTag(
                        childFragmentManager,
                        effect_fragment,
                        R.id.container_layout,
                        NEffectFragment::class.java.name
                    )
                } else {
                    FragmentUtils.showFragment(childFragmentManager, effect_fragment)
                }
            }
        }
    }

    fun getScenesDatas() : MutableList<BaseNode>{

        val call_back = mutableListOf<BaseNode>()

        BaseInfoData.scenes_cur?.let { scenIt ->

            getAttachActivity().shearControl.mMeshManagerApi.meshNetwork?.let { mWork ->

                val dGroup = scenIt.deviceGroups.filter { !it.isUngrouped }

                if (dGroup.isNotEmpty()){

                    dGroup.forEach {gIt ->

                        if (gIt.devices.isNotEmpty()){

                            val bean = NControllerGruoupBean(
                                group_name = gIt.dgName,
                                address = gIt.address.toInt(),
                                isSelect = false,
                                isIsPowserOn = false,
                                lightness = 0,
                                effect = 0,
                                preset = 0,
                                deltaUV = 0,
                                temperature = 0,
                                hue = 0,
                                saturation = 0,
                                deviceType = 0,
                                currentDeviceType = 1,
                                childNode = mutableListOf()
                            )

                            val child = mutableListOf<BaseNode>()

                            gIt.devices.forEachIndexed { index, devicesBean ->

                                if (index == 0){
                                    bean.isIsPowserOn = devicesBean.isIsPowserOn
                                    bean.lightness = devicesBean.lightness
                                    bean.effect = devicesBean.effect
                                    bean.deltaUV = devicesBean.deltaUV
                                    bean.temperature = devicesBean.temperature
                                    bean.hue = devicesBean.hue
                                    bean.saturation = devicesBean.saturation
                                    bean.deviceType = devicesBean.deviceType
                                    bean.currentDeviceType = devicesBean.currentDeviceType
                                }

                                val ibean = NControllerItemBean(
                                    devicesBean.name,
                                    devicesBean.address,
                                    false,
                                    mutableListOf()
                                )

                                child.add(ibean)
                            }

                            bean.childNode = child

                            call_back.add(bean)
                        }

                    }
                }

                //未分组数据
                val not_Group = scenIt.deviceGroups.filter { it.isUngrouped }

                if (not_Group.isNotEmpty()){

                    not_Group[0].devices.forEach {

                        var has_address = false

                        if (call_back.isNotEmpty()){
                            call_back.forEach {gIt ->
                                if (gIt is NControllerGruoupBean){
                                    gIt.childNode?.forEach { chIt ->
                                        chIt as NControllerItemBean
                                        if (chIt.unicastAddress == it.address){
                                            has_address = true
                                        }
                                    }
                                }
                            }
                        }


                        if (!has_address){

                            val Pnode = mWork.getNode(it.address)
                            val index = getBoundAppKeyIndexes(Pnode,scenIt.deviceGroups[0].address.toInt())

                            val bean = NControllerNoGroupBean(
                                id = it.id,
                                note_name = it.name,
                                group_address = scenIt.deviceGroups[0].address.toInt(),
                                unicastAddress = it.address,
                                boundAppKeyIndexes = index,
                                isSelect = false,
                                isIsPowserOn = it.isIsPowserOn,
                                lightness = it.lightness,
                                effect = it.effect,
                                preset = it.preset,
                                deltaUV = it.deltaUV,
                                temperature = it.temperature,
                                hue = it.hue,
                                saturation = it.saturation,
                                deviceType = it.deviceType,
                                currentDeviceType = it.currentDeviceType,
                                childNode = mutableListOf()
                            )
                            call_back.add(bean)

                        }
                    }
                }
            }
        }

        return call_back
    }


    private fun getBoundAppKeyIndexes(Pmnode : ProvisionedMeshNode,
                                      address : Int) : Int {

        val e01 = Pmnode.elements
        if (e01.isNotEmpty()){
            val element = e01[e01.keys.hashCode()]
            element?.meshModels?.values?.let { meshModels ->

                val onOff = meshModels.filter { it.modelName == "Generic On Off Server" }

                if (onOff.isNotEmpty()){

                    val datass = onOff[0]

                    datass.subscribedAddresses.forEachIndexed { index, i ->

                        if (i == address){
                            return datass.boundAppKeyIndexes[index]
                        }

                    }

                }

            }
        }

        return 0
    }


    override fun isStatusBarEnabled(): Boolean {
        return !super.isStatusBarEnabled()
    }

    override fun onDestroy() {
        BusUtils.unregister(this)
        super.onDestroy()
    }

    @BusUtils.Bus(tag = "move_controller",sticky = true,threadMode = ThreadMode.MAIN)
    fun onEvent(index : Int){

        postDelayed({
            tab_top_layout.UpDataPos(index)
            mHeadClick = false
            changeFragment(index)
        },300)

    }

    @BusUtils.Bus(tag = "gone_hsi",sticky = true,threadMode = ThreadMode.MAIN)
    fun onEvent(isGone : Boolean){
        tab_top_layout.setGoneText(1,isGone,"HSI")
    }

}