package com.tw.artin.ui3.fragment

import com.blankj.utilcode.util.BusUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.tw.artin.MainTabActivity2
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwFragment
import com.tw.artin.ui3.adapter.TextBinder
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

    private val mAdapter by lazy { BaseBinderAdapter().addItemBinder(TextBinder()) }

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
        mAdapter.setNewInstance(
            mutableListOf<String>().apply {
                for (i in 1..20) {
                    add("text $i")
                }
            }.toMutableList()
        )
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

    override fun isStatusBarEnabled(): Boolean {
        return !super.isStatusBarEnabled()
    }

    override fun onDestroy() {
        BusUtils.unregister(this)
        super.onDestroy()
    }
}