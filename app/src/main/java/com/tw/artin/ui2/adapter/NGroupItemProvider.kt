package com.tw.artin.ui2.adapter

import android.content.Intent
import android.view.View
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tw.artin.R
import com.tw.artin.bean.DevicesBean
import com.tw.artin.ui.activity.LedInfoActivity
import com.tw.artin.ui2.fragment.NDeviceFragment

class NGroupItemProvider (val mFragment : NDeviceFragment) : BaseItemProvider<DevicesBean>(){

    override val itemViewType: Int = 3

    override val layoutId: Int = R.layout.not_group_item

    init {
        addChildClickViewIds(R.id.iv_not_i01,R.id.iv_not_i03)
    }

    override fun convert(helper: BaseViewHolder, item: DevicesBean) {

        if (item.note_name_new == ""){
            helper.setText(R.id.tv_not_name,item.note_name)
        }else{
            helper.setText(R.id.tv_not_name,item.note_name_new)
        }

        if (item.isonline!!){
            helper.setVisible(R.id.iv_not_i02,true)
        }else{
            helper.setVisible(R.id.iv_not_i02,false)
        }
    }


    override fun onChildClick(helper: BaseViewHolder, view: View, data: DevicesBean, position: Int) {


        when(view.id){

            R.id.iv_not_i01 ->{

                /*if (!data.isonline!!){
                    return
                }*/

                val onoffStr = "${data.note_name},${!data.isonOff!!}"

                (getAdapter() as NDeviceListAdapter).setOnOffData(onoffStr)

                mFragment.getAttachActivity().shearControl.opneLight(
                    data.unicastAddress!!,data.boundAppKeyIndexes!!
                )

            }

            R.id.iv_not_i03 -> {

                mFragment.getAttachActivity().run {

                    startActivityForResult(
                        Intent(this,LedInfoActivity::class.java).apply {
                            putExtra("name_note",data.note_name)
                            putExtra("name_note_new",data.note_name_new)
                            putExtra("group_address",data.group_address)
                            putExtra("unicastAddress",data.unicastAddress)
                            putExtra("boundAppKeyIndexes",data.boundAppKeyIndexes)
                            putExtra("isonline",data.isonline)
                        },
                        LED_INFO
                    )

                }
            }
        }

    }

}