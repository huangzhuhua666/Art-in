package com.tw.artin.view

import android.content.Context
import com.blankj.utilcode.util.ToastUtils
import com.lxj.xpopup.core.CenterPopupView
import com.tw.artin.R
import com.tw.artin.ui2.fragment.NDeviceFragment
import kotlinx.android.synthetic.main.dialog_group_message.view.*

class CreatGroupPop2(context: Context, val mFragment : NDeviceFragment, val name_txt : String?, val titles : String?) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_group_message
    }


    override fun onCreate() {
        super.onCreate()

        titles?.let {
            tv_title.text = it
        }

        name_txt?.let {
            et_g_name.setText(it)
        }

        tv_g_cancel.setOnClickListener {
            dismiss()
        }

        tv_g_confirm.setOnClickListener {

            val name = et_g_name.text.toString()
            if (name.isEmpty()){
                ToastUtils.showShort(R.string.device_text07)
                return@setOnClickListener
            }

            if (name_txt == null){

                val has = mFragment.mAdapter.hasNameGroup(name)

                if (has){
                    ToastUtils.showShort(R.string.device_text08)
                    return@setOnClickListener
                }

            }

            mListener?.onInputData(name)

            dismiss()
        }

    }

    private var mListener: InputListener? = null

    fun setOnSelectListener(selectListener: InputListener): InputListener? {
        mListener = selectListener
        return mListener
    }

    interface InputListener {
        fun onInputData(txt : String)
    }

}