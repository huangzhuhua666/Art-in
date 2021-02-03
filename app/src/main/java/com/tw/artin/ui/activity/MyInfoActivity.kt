package com.tw.artin.ui.activity

import android.content.Intent
import android.view.View
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.LogUtils
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.lxj.xpopup.XPopup
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity
import com.tw.artin.http.api.MyInfoApi
import com.tw.artin.http.model.HttpData
import kotlinx.android.synthetic.main.my_info_activity.*
import org.json.JSONObject

class MyInfoActivity : TwActivity() {

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.my_info_activity
    }

    override fun initView() {
        setOnClickListener(R.id.tv_exit,R.id.tv_edit_pwd)
    }

    override fun initData() {
        getMyInfoData()
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v?.id){

            R.id.tv_exit -> {

                XPopup.Builder(this)
                    .hasShadowBg(false)
                    .asConfirm(resources.getString(R.string.exit_app_t01),
                        resources.getString(R.string.exit_app_t03)
                    )
                    {
                        finish()
                        BusUtils.post("logout")
                    }
                    .bindLayout(R.layout.dialog_message)
                    .show()

            }

            R.id.tv_edit_pwd -> startActivity(
                Intent(this,EditPwdActivity::class.java)
            )
        }

    }

    private fun getMyInfoData(){

        EasyHttp.post(this)
            .api(
                MyInfoApi()
            )
            .request(object : HttpCallback<HttpData<MyInfoApi.MyInfoBean>>(this){

                override fun onSucceed(result: HttpData<MyInfoApi.MyInfoBean>?) {
                    super.onSucceed(result)

                    result?.objx?.let {

                        tv_nickname.text = it.username ?: ""

                        tv_moblie.text = it.mobile ?: ""

                        tv_email.text = it.email ?: ""

                    }
                }
            })


    }

}