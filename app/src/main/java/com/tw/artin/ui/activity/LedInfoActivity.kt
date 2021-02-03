package com.tw.artin.ui.activity

import android.content.Intent
import android.view.View
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.BusUtils.ThreadMode
import com.blankj.utilcode.util.ToastUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnInputConfirmListener
import com.lxj.xpopup.interfaces.OnSelectListener
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwActivity
import kotlinx.android.synthetic.main.led_info_activity.*

//设备详情
class LedInfoActivity : TwActivity() {

    var unicastAddress = -1
    var isonline = false
    var group_address = 0
    var boundAppKeyIndexes = 0

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.led_info_activity
    }

    override fun initView() {

        BusUtils.register(this)

        setOnClickListener(R.id.iv_edit)
    }

    override fun initData() {

        unicastAddress = intent.getIntExtra("unicastAddress",-1)

        if (intent.getStringExtra("name_note_new") == ""
            || intent.getStringExtra("name_note_new") == null){
            tv_note_name.text = intent.getStringExtra("name_note")
        }else{
            tv_note_name.text = intent.getStringExtra("name_note_new")
        }

        boundAppKeyIndexes = intent.getIntExtra("boundAppKeyIndexes",0)

        group_address = intent.getIntExtra("group_address",0)

        isonline = intent.getBooleanExtra("isonline",false)

        if (isonline){
            postDelayed({
                //BusUtils.post("getElementSize",unicastAddress)
                BusUtils.post("getElementSize","$unicastAddress,$boundAppKeyIndexes")
            },300)
        }else{
            tv_p01.text = "-"
            tv_p02.text = "-"
            tv_p03.text = "-"
        }


    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v?.id){

            R.id.iv_edit -> {

                XPopup.Builder(this)
                    .hasShadowBg(false)
                    .autoOpenSoftInput(true)
                    .asInputConfirm(
                        getString(R.string.exit_app_t01),
                        "",
                        tv_note_name.text.toString(),
                        getString(R.string.pop_text03),
                        object : OnInputConfirmListener {
                            override fun onConfirm(text: String?) {

                                text?.let {

                                    if (it.isEmpty()){
                                        ToastUtils.showShort(R.string.pop_text03)
                                        return
                                    }

                                    BaseInfoData.scenes_cur?.let { curIt ->

                                        curIt.deviceGroups.forEach { dgIt ->

                                            dgIt.devices.forEach { devIt ->
                                                if (devIt.address == unicastAddress){
                                                    devIt.name = it
                                                }
                                            }
                                        }
                                    }

                                    BusUtils.post("edit_note_name")

                                    tv_note_name.text = it
                                }
                            }
                        }
                    )
                    .bindLayout(R.layout.dialog_input_message)
                    .show()

            }

        }
    }

    override fun onRightClick(v: View?) {
        super.onRightClick(v)

        if (!isonline){
            return
        }

        val lists = if (group_address == 49152){
            arrayOf(getString(R.string.pop_text02))
        }else{
            arrayOf(getString(R.string.pop_text01),getString(R.string.pop_text02))
        }


        XPopup.Builder(this)
            .hasShadowBg(false)
            .asBottomList(
                getString(R.string.pick_image01),
                lists,
                object : OnSelectListener{
                    override fun onSelect(position: Int, text: String?) {

                        //场景中移除  取消订阅
                        if (text == getString(R.string.pop_text01)){

                            setResult(RESULT_OK, Intent().apply {
                                putExtra("isDel",false)
                                putExtra("unicastAddress",unicastAddress)
                                putExtra("group_address",group_address)
                            })

                            finish()

                        }else{

                            setResult(RESULT_OK, Intent().apply {
                                putExtra("isDel",true)
                                putExtra("unicastAddress",unicastAddress)
                            })

                            finish()
                        }
                    }
                }
            ).show()

    }

    override fun onDestroy() {
        BusUtils.unregister(this)
        super.onDestroy()
    }

    @BusUtils.Bus(tag = "typeElement",sticky = true,threadMode = ThreadMode.MAIN)
    fun onEvent(datas : String){
        tv_model.text = datas
        val size = datas.count { it.toString() == "/" }
        if (size == 3){
            tv_led_t01.setText(R.string.led_info_t08)
            tv_led_t02.setText(R.string.led_info_t26)
        }else{
            tv_led_t01.setText(R.string.led_info_t07)
            tv_led_t02.setText(R.string.led_info_t27)
        }
    }

    @BusUtils.Bus(tag = "getLedInfo",sticky = true,threadMode = ThreadMode.MAIN)
    fun onEvent01(datas : String){

        val info = datas.split(",")

        tv_p01.text = info.get(0)
        tv_p02.text = info.get(1)
        tv_p03.text = info.get(2)
    }

}