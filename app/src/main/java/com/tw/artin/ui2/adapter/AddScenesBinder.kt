package com.tw.artin.ui2.adapter

import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.binder.QuickItemBinder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnInputConfirmListener
import com.tw.artin.R
import com.tw.artin.ui2.fragment.ScenesFragment

class AddScenesBinder(val mFragment : ScenesFragment) : QuickItemBinder<String>() {

    override fun convert(holder: BaseViewHolder, data: String) {
    }

    override fun getLayoutId(): Int {
        return R.layout.add_scenes_item
    }

    override fun onClick(holder: BaseViewHolder, view: View, data: String, position: Int) {
        super.onClick(holder, view, data, position)

        XPopup.Builder(context)
            .hasShadowBg(false)
            .autoOpenSoftInput(true)
            .asInputConfirm(
                context.getString(R.string.led_info_t13),"",
                context.getString(R.string.led_info_t14),
                object : OnInputConfirmListener{
                    override fun onConfirm(text: String?) {

                        text?.let {

                            if (it.isEmpty()){
                                ToastUtils.showShort(R.string.led_info_t14)
                                return
                            }

                            mFragment.creatScenes(it,false)

                        }
                    }
                }
            )
            .bindLayout(R.layout.dialog_input_message)
            .show()

    }
}