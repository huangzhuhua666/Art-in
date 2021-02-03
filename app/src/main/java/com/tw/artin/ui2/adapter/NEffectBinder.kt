package com.tw.artin.ui2.adapter

import com.chad.library.adapter.base.binder.QuickItemBinder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tw.artin.R
import com.tw.artin.bean.EffectBean

class NEffectBinder : QuickItemBinder<EffectBean>() {

    override fun convert(holder: BaseViewHolder, data: EffectBean) {

        when(data.effect){

            0 -> holder.setImageResource(R.id.ef_iv, R.mipmap.icon_e4)

            1 -> holder.setImageResource(R.id.ef_iv, R.mipmap.icon_e3)

            2 -> holder.setImageResource(R.id.ef_iv, R.mipmap.icon_e1)

            3 -> holder.setImageResource(R.id.ef_iv, R.mipmap.icon_e2)

            4 -> holder.setImageResource(R.id.ef_iv, R.mipmap.icon_e7)

            5 -> holder.setImageResource(R.id.ef_iv, R.mipmap.icon_e6)

            6 -> holder.setImageResource(R.id.ef_iv, R.mipmap.icon_e5)
        }

        if (data.isSelect){
            holder.setBackgroundResource(R.id.layout_effect, R.drawable.effect_shape02)
        }else{
            holder.setBackgroundResource(R.id.layout_effect, R.drawable.effect_shape01)
        }

        holder.setText(R.id.tv_ef_name01,data.name01)

        holder.setText(R.id.tv_ef_name02,data.name02)

    }

    override fun getLayoutId(): Int {
        return R.layout.binder_effect
    }

}