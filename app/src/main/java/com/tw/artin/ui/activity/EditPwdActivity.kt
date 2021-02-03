package com.tw.artin.ui.activity

import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity
import com.tw.artin.http.api.EditPwdApi
import com.tw.artin.http.model.HttpData
import kotlinx.android.synthetic.main.edit_pwd_activity.*
import org.json.JSONObject

class EditPwdActivity : TwActivity() {

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.edit_pwd_activity
    }

    override fun initView() {

        tv_edit.setOnClickListener {

            val pwd01 = et_now_pwd.text.toString()
            if (pwd01.isEmpty()){
                ToastUtils.showShort(R.string.edit_toa_pwd01)
                return@setOnClickListener
            }

            if (pwd01 != SPUtils.getInstance().getString("password")){
                ToastUtils.showShort(R.string.edit_toa1_pwd01)
                return@setOnClickListener
            }

            val pwd = et_new_pwd.text.toString()
            if (pwd.isEmpty()){
                ToastUtils.showShort(R.string.edit_toa_pwd02)
                return@setOnClickListener
            }

            EditPwd(
                EditPwdApi(pwd)
            )

        }

    }

    override fun initData() {
    }

    fun EditPwd(api : EditPwdApi){

        EasyHttp.post(this)
            .api(api)
            .request(object : HttpCallback<HttpData<JSONObject>>(this){

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)

                    SPUtils.getInstance().put("password",api.password)

                    ToastUtils.showShort(R.string.edit_pwd03)
                    finish()
                }
            })

    }

}