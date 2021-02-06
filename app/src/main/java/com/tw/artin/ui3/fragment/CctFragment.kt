package com.tw.artin.ui3.fragment

import android.view.View
import android.widget.FrameLayout
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.gcssloop.widget.ArcSeekBar
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.tw.artin.MainTabActivity2
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwFragment
import com.tw.artin.vp.ControllerContract
import kotlinx.android.synthetic.main.cct_fragment.*
import kotlinx.android.synthetic.main.fragment_cct.*
import kotlinx.android.synthetic.main.fragment_cct.arc_bar
import kotlinx.android.synthetic.main.fragment_cct.rsb_bar
import kotlinx.android.synthetic.main.fragment_cct.tv_k01
import kotlinx.android.synthetic.main.fragment_cct.tv_k02
import kotlinx.android.synthetic.main.fragment_controller.*

/**
 * Create by hzh on 2021/02/04.
 */
@Suppress("SetTextI18n")
class CctFragment : TwFragment<MainTabActivity2>(), ControllerContract.View {

    override fun getLayoutId(): Int = R.layout.fragment_cct

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? = null

    override fun initView() {
        // G/M
        with(rsb_bar) {
            setOnRangeChangedListener(object : OnRangeChangedListener {

                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

                }

                override fun onRangeChanged(
                    view: RangeSeekBar?,
                    leftValue: Float,
                    rightValue: Float,
                    isFromUser: Boolean
                ) {
                    if (leftValue < -50) view?.setProgress(-50f)

                    if (leftValue > 50) view?.setProgress(50f)
                }

                override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

                }
            })

            setProgress(0f)
        }

        //半圆
        with(arc_bar) {
            val mWidth = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(60f)

            layoutParams.let {
                it.width = mWidth
                it.height = mWidth
            }

            (tv_k01.layoutParams as FrameLayout.LayoutParams).bottomMargin =
                SizeUtils.dp2px(mWidth / 10f)

            (tv_k02.layoutParams as FrameLayout.LayoutParams).leftMargin =
                SizeUtils.dp2px(mWidth / 6.6f)

            setOnProgressChangeListener(object : ArcSeekBar.OnProgressChangeListener {

                override fun onProgressChanged(
                    seekBar: ArcSeekBar?,
                    progress: Int,
                    isUser: Boolean
                ) {
                    if (!isUser) (parentFragment as ControllerFragment?)?.scroll_view?.requestDisallowInterceptTouchEvent(false)
                }

                override fun onStartTrackingTouch(seekBar: ArcSeekBar?) {
                    (parentFragment as ControllerFragment?)?.scroll_view?.requestDisallowInterceptTouchEvent(true)
                }

                override fun onStopTrackingTouch(seekBar: ArcSeekBar?) {
                    (parentFragment as ControllerFragment?)?.scroll_view?.requestDisallowInterceptTouchEvent(false)
                }
            })

            progress = 2000
        }

        setOnClickListener(R.id.tv_zero, R.id.tv_k01, R.id.tv_k02)
    }

    override fun initData() {

    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.tv_zero -> {
                //点击
                rsb_bar.setProgress(0f)
            }
            R.id.tv_k01 -> {
                //点击
                arc_bar.progress = 3200
            }
            R.id.tv_k02 -> {
                //点击
                arc_bar.progress = 5600
            }
        }
    }

    override fun onDestroyView() {
        BusUtils.unregister(this)
        super.onDestroyView()
    }
}