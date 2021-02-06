package com.tw.artin.ui3.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.ScreenUtils
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.tw.artin.MainTabActivity2
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwFragment
import com.tw.artin.vp.ControllerContract
import kotlinx.android.synthetic.main.fragment_controller.*
import kotlinx.android.synthetic.main.fragment_hsi.*

/**
 * Create by hzh on 2021/02/04.
 */
class HsiFragment : TwFragment<MainTabActivity2>(), ControllerContract.View {

    //旋转角度
    private var angle = 0

    private var mHue: Float = 0f
    private var mSat: Float = 0f

    override fun getLayoutId(): Int = R.layout.fragment_hsi

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        BusUtils.register(this)

        with(hsi_color) {

            hsi_color.layoutParams.apply {
                width = ScreenUtils.getScreenWidth() / 2
                height = ScreenUtils.getScreenWidth() / 2
            }

            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> (parentFragment as ControllerFragment?)?.scroll_view?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP -> (parentFragment as ControllerFragment?)?.scroll_view?.requestDisallowInterceptTouchEvent(false)
                }
                return@setOnTouchListener false
            }

            setColorListener(object : ColorEnvelopeListener {

                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    if (fromUser) {
                        envelope?.let {
                            val hsv = floatArrayOf(0f, 0f, 0f)
                            Color.colorToHSV(it.color, hsv)

                            mSat = hsv[1]
                            mHue = hsv[0]
                        }
                    }
                }
            })
        }

        setOnClickListener(R.id.iv_xz)
    }

    override fun initData() {

    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.iv_xz -> setRotation()
        }
    }

    private fun setRotation() {
        angle = if (angle == 360) 0
        else angle + 90

        hsi_color.rotation = angle.toFloat()
    }

    override fun onDestroyView() {
        BusUtils.unregister(this)
        super.onDestroyView()
    }
}