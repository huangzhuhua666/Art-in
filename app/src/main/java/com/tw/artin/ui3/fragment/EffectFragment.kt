package com.tw.artin.ui3.fragment

import android.graphics.Color
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.ConvertUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.tw.artin.MainTabActivity2
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwFragment
import com.tw.artin.bean.EffectBean
import com.tw.artin.ui2.adapter.NEffectBinder
import com.tw.artin.view.UniversalItemDecoration
import com.tw.artin.vp.ControllerContract
import kotlinx.android.synthetic.main.fragment_effect.*

/**
 * Create by hzh on 2021/02/04.
 */
class EffectFragment : TwFragment<MainTabActivity2>(), ControllerContract.View {

    val mAdapter01 by lazy {
        BaseBinderAdapter().apply {

            addItemBinder(NEffectBinder())

            setOnItemClickListener { adapter, view, position ->

                val info = adapter.getItem(position) as EffectBean

                if (!info.isSelect) {

                    data.forEach {
                        it as EffectBean
                        it.isSelect = it.name01 == info.name01
                    }

                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_effect

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? = null

    override fun initView() {
        BusUtils.register(this)

        with(effect_recycle01) {
            layoutManager = GridLayoutManager(getAttachActivity(), 3)

            addItemDecoration(object : UniversalItemDecoration() {
                override fun getItemOffsets(position: Int): Decoration {

                    val decoration = ColorDecoration()
                    decoration.decorationColor = Color.BLACK

                    if (position % 3 == 0) {
                        decoration.right = ConvertUtils.dp2px(4f)
                    } else if (position % 3 == 1) {
                        decoration.left = ConvertUtils.dp2px(4f)
                        decoration.right = ConvertUtils.dp2px(4f)
                    } else {
                        decoration.left = ConvertUtils.dp2px(4f)
                    }

                    return decoration
                }
            })

            adapter = mAdapter01
        }
    }

    override fun initData() {

    }

    override fun onDestroy() {
        BusUtils.unregister(this)
        super.onDestroy()
    }
}