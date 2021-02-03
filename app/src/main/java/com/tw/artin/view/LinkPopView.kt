package com.tw.artin.view

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lxj.xpopup.core.CenterPopupView
import com.tw.artin.MainTabActivity2
import com.tw.artin.R
import kotlinx.android.synthetic.main.link_pop_view.view.*


class LinkPopView(context: Context,
                  val mActivity: MainTabActivity2,
                  val pro : MutableLiveData<Int>) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.link_pop_view
    }

    override fun onCreate() {
        super.onCreate()

        pro.observe(mActivity,
            {
                link_pbar.progress = it
            })

        tv_link_stop.setOnClickListener {

            mListener?.onResult()

            dismiss()
        }

    }

    private var mListener: StopListener? = null

    fun setOnStopListener(selectListener: StopListener): StopListener? {
        mListener = selectListener
        return mListener
    }

    interface StopListener {
        fun onResult()
    }



}