package com.tw.artin.ui2.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.tw.artin.R
import com.tw.artin.bean.NControllerGruoupBean
import com.tw.artin.ui2.fragment.NCctFragment
import com.tw.artin.ui2.fragment.NControllerFragment
import com.tw.artin.util.UtilsBigDecimal

class NCctGProvider(val mFragment : NCctFragment) : BaseNodeProvider() {

    override val itemViewType: Int = 0

    override val layoutId: Int = R.layout.cct_g_provider

    init {
        addChildClickViewIds(R.id.iv_exp,R.id.iv_g_offon)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {

        if (item is NControllerGruoupBean){

            helper.getView<TextView>(R.id.tv_color).background =

                GradientDrawable().apply {

                    shape = GradientDrawable.OVAL

                    if (item.temperature == 0){
                        setColor(Color.GREEN)
                    }else{
                        (getAdapter() as NCctAdapter).setCctBg(item.temperature!!,this)
                    }

                }

            helper.setText(R.id.tv_color,"CCT")

            helper.setText(R.id.tv_cct_g_name,item.group_name)

            helper.getView<RangeSeekBar>(R.id.seek_bar_g).let {

                it.setOnRangeChangedListener(object : OnRangeChangedListener{

                    override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                        //1滑动
                        mFragment.changeList[2] = 1
                    }

                    override fun onRangeChanged(
                        view: RangeSeekBar?,
                        leftValue: Float,
                        rightValue: Float,
                        isFromUser: Boolean
                    ) {

                        if (isFromUser){
                            if (leftValue<1){
                                view?.setProgress(1f)
                            }
                        }
                    }

                    override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

                        if (isLeft && mFragment.changeList[2] == 1){

                            val leftValue = view?.leftSeekBar?.progress!!

                            val text = UtilsBigDecimal.GetIntString(leftValue.toDouble())

                            if (text == "-0"){
                                helper.setText(R.id.tv_ld_g,"0%")
                            }else{
                                helper.setText(R.id.tv_ld_g,"${text}%")
                            }

                            if (text == "0"){
                                return
                            }

                            (getAdapter() as NCctAdapter)?.let { adapter ->

                                if (item.isSelect){
                                    //没有下一步
                                    adapter.has_next = 0
                                    adapter.nextBeanString = ""

                                    mFragment.OpDatas(text)

                                    mFragment.changeList[2] = 0

                                }else{

                                    //先选中，然后操作
                                    mFragment.changeList[0] = 0
                                    mFragment.changeList[1] = 0
                                    mFragment.changeList[2] = 0
                                    mFragment.changeList[3] = 0

                                    mFragment.parentFragment?.let {
                                        it as NControllerFragment
                                        it.mCurrentIndex = helper.adapterPosition
                                    }

                                    adapter.selectItem(helper.adapterPosition,true)

                                    //下一步，操作亮度
                                    adapter.has_next = 2

                                    //装数据
                                    adapter.nextBeanString = ""
                                    adapter.nextBeanString = text
                                }

                            }

                        }

                    }
                })

                //显示状态
                mFragment.changeList[2] = 0
                it.setProgress(item.lightness!!.toFloat())

                val text = UtilsBigDecimal.GetIntString(item.lightness!!.toDouble())

                if (text == "-0"){
                    helper.setText(R.id.tv_ld_g,"0%")
                }else{
                    helper.setText(R.id.tv_ld_g,"${text}%")
                }

            }

            if (item.isExpanded){
                helper.setImageResource(R.id.iv_exp,R.mipmap.shebei_btn_xiala)
            }else{
                helper.setImageResource(R.id.iv_exp,R.mipmap.shebei_btn_next)
            }

            if (item.isIsPowserOn!!){
                helper.setImageResource(R.id.iv_g_offon,R.mipmap.shebei_btn_on)
            }else{
                helper.setImageResource(R.id.iv_g_offon,R.mipmap.shebei_btn_off)
            }

            if (item.isSelect){
                helper.setBackgroundColor(R.id.layout_cct,Color.parseColor("#585858"))
                helper.setGone(R.id.v_select,false)
            }else{
                helper.setBackgroundColor(R.id.layout_cct,Color.BLACK)
                helper.setGone(R.id.v_select,true)
            }

        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        super.onClick(helper, view, data, position)

        if (data is NControllerGruoupBean){

            if (!data.isSelect){

                mFragment.changeList[0] = 0
                mFragment.changeList[1] = 0
                mFragment.changeList[2] = 0
                mFragment.changeList[3] = 0

                mFragment.parentFragment?.let {
                    it as NControllerFragment
                    it.mCurrentIndex = position
                }

                (getAdapter() as NCctAdapter).selectItem(position,true)
            }

        }

    }

    override fun onChildClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        super.onChildClick(helper, view, data, position)

        if (data is NControllerGruoupBean){

            when(view.id){

                R.id.iv_exp ->{
                    getAdapter()?.expandOrCollapse(position)
                }

                R.id.iv_g_offon ->{

                    (getAdapter() as NCctAdapter)?.let { adapter ->

                        if (data.isSelect){

                            //没有下一步
                            adapter.has_next = 0
                            adapter.nextBean = null

                            adapter.setOnOffOp(0,
                                data.address!!,
                                true,
                                !data.isIsPowserOn!!)

                        }else{

                            //先选中，然后操作
                            mFragment.changeList[0] = 0
                            mFragment.changeList[1] = 0
                            mFragment.changeList[2] = 0
                            mFragment.changeList[3] = 0

                            mFragment.parentFragment?.let {
                                it as NControllerFragment
                                it.mCurrentIndex = position
                            }

                            adapter.selectItem(position,true)

                            //下一步，操作灯
                            adapter.has_next = 1

                            //装数据
                            adapter.nextBean = null
                            adapter.nextBean = data
                        }
                    }



                }
            }
        }

    }

}