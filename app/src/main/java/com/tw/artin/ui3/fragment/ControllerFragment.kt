package com.tw.artin.ui3.fragment

import com.blankj.utilcode.util.BusUtils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.tw.artin.MainTabActivity2
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwFragment
import com.tw.artin.bean.NControllerGruoupBean
import com.tw.artin.bean.NControllerItemBean
import com.tw.artin.bean.NControllerNoGroupBean
import com.tw.artin.ui3.adapter.CctAdapter
import com.tw.artin.util.FragmentUtils
import com.tw.artin.view.OnTabSelectListener
import kotlinx.android.synthetic.main.fragment_controller.*

/**
 * Create by hzh on 2021/02/04.
 */
class ControllerFragment : TwFragment<MainTabActivity2>() {

    private var cct_fragment: CctFragment? = null
    private var hsi_fragment: HsiFragment? = null
    private var effect_fragment: EffectFragment? = null

    private var main_pos = 0

    //默认选中的组
    var mCurrentIndex = -1
    var mHeadClick = false

    val mAdapter by lazy { CctAdapter(this) }

    override fun getLayoutId(): Int = R.layout.fragment_controller

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? = null

    override fun initView() {
        BusUtils.register(this)

        getStatusBarConfig()?.run { statusBarView(v_view).init() }

        tab_top_layout.setTabsNotViewPager(arrayListOf("CCT", "HSI", "EFFECT"))

        changeFragment(0)

        rv_data?.adapter = mAdapter

        initListener()
    }

    override fun initData() {

    }

    override fun onResume() {
        super.onResume()
        genData()
    }

    private fun initListener() {
        tab_top_layout.setOnTabSelectListener(object : OnTabSelectListener {

            override fun onTabSelect(position: Int) {
                if (position == main_pos) return

                mHeadClick = true
                changeFragment(position)
            }

            override fun onTabReselect(position: Int) {

            }
        })
    }

    private fun changeFragment(pos: Int) {
        main_pos = pos
        when (main_pos) {
            0 -> {
                cct_fragment = childFragmentManager.findFragmentByTag(
                    CctFragment::class.java.name
                ) as CctFragment?

                if (cct_fragment == null) {
                    cct_fragment = CctFragment()
                    FragmentUtils.addFragmentWithTag(
                        childFragmentManager,
                        cct_fragment,
                        R.id.container_layout,
                        CctFragment::class.java.name
                    )
                } else FragmentUtils.showFragment(childFragmentManager, cct_fragment)
            }
            1 -> {
                hsi_fragment = childFragmentManager.findFragmentByTag(
                    HsiFragment::class.java.name
                ) as HsiFragment?

                if (hsi_fragment == null) {
                    hsi_fragment = HsiFragment()
                    FragmentUtils.addFragmentWithTag(
                        childFragmentManager,
                        hsi_fragment,
                        R.id.container_layout,
                        HsiFragment::class.java.name
                    )
                } else FragmentUtils.showFragment(childFragmentManager, hsi_fragment)
            }
            2 -> {
                effect_fragment = childFragmentManager.findFragmentByTag(
                    EffectFragment::class.java.name
                ) as EffectFragment?

                if (effect_fragment == null) {
                    effect_fragment = EffectFragment()
                    FragmentUtils.addFragmentWithTag(
                        childFragmentManager,
                        effect_fragment,
                        R.id.container_layout,
                        EffectFragment::class.java.name
                    )
                } else FragmentUtils.showFragment(childFragmentManager, effect_fragment)
            }
        }
    }

    private fun genData() {
        BaseInfoData.scenes_cur?.let { data ->
            val part = data.deviceGroups.partition { !it.isUngrouped }
            val dataList = mutableListOf<BaseNode>()
            dataList.addAll(part.first.filter { it.devices.isNotEmpty() }.map { group ->
                NControllerGruoupBean(
                    group_name = group.dgName,
                    address = group.address.toInt(),
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
                ).apply {
                    childNode = group.devices.mapIndexed { index, device ->
                        if (index == 0) {
                            isIsPowserOn = device.isIsPowserOn
                            lightness = device.lightness
                            effect = device.effect
                            deltaUV = device.deltaUV
                            temperature = device.temperature
                            hue = device.hue
                            saturation = device.saturation
                            deviceType = device.deviceType
                            currentDeviceType = device.currentDeviceType
                        }

                        NControllerItemBean(
                            device.name,
                            device.address,
                            false,
                            mutableListOf()
                        )
                    }.toMutableList()
                }
            })

            if (part.second.isEmpty()) {
                mAdapter.setList(dataList)

                if (mCurrentIndex == -1) mCurrentIndex = 0
                if (mCurrentIndex < dataList.size) mAdapter.selectItem(mCurrentIndex, true)

                return
            }

            dataList.addAll(part.second[0].devices.map {
                NControllerNoGroupBean(
                    id = it.id,
                    note_name = it.name,
                    group_address = data.deviceGroups[0].address.toInt(),
                    unicastAddress = it.address,
                    boundAppKeyIndexes = 0,
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
            })

            mAdapter.setList(dataList)

            if (mCurrentIndex == -1) mCurrentIndex = 0
            if (mCurrentIndex < dataList.size) mAdapter.selectItem(mCurrentIndex, true)
        }
    }

    override fun isStatusBarEnabled(): Boolean {
        return !super.isStatusBarEnabled()
    }

    override fun onDestroy() {
        BusUtils.unregister(this)
        super.onDestroy()
    }

    @BusUtils.Bus(tag = "gone_hsi",sticky = true,threadMode = BusUtils.ThreadMode.MAIN)
    fun onEvent(isGone : Boolean){
        tab_top_layout.setGoneText(1,isGone,"HSI")
    }
}