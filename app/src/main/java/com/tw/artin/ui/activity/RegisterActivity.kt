package com.tw.artin.ui.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity
import com.tw.artin.http.api.RegisterApi
import com.tw.artin.http.api.SendMsgApi
import com.tw.artin.http.model.HttpData
import kotlinx.android.synthetic.main.register_activity.*
import org.json.JSONObject

class RegisterActivity : TwActivity() {

    var mType = 2  //1 手机   2邮箱

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.register_activity
    }

    override fun initView() {

        setOnClickListener(R.id.cd_view,R.id.tv_change,R.id.reg_btn)
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v?.id){

            R.id.cd_view -> {

                val account = et_email_phone.text.toString()

                if (account.isEmpty()){
                    if (mType == 1){
                        ToastUtils.showShort(R.string.register_toa_t07)
                    }else{
                        ToastUtils.showShort(R.string.register_toa_t01)
                    }
                    return
                }

                EasyHttp.post(this)
                    .api(
                        SendMsgApi(
                            account,
                            mType
                        )
                    ).request(object : HttpCallback<HttpData<JSONObject>>(this){

                        override fun onSucceed(result: HttpData<JSONObject>?) {
                            super.onSucceed(result)

                            cd_view.start()
                            ToastUtils.showShort(R.string.common_code_send_hint)
                        }

                    })

            }

            R.id.tv_change -> {

                if (mType == 1){
                    mType = 2
                    tv_change.setText(R.string.register_t05)

                    tv_reg01.setText(R.string.register_t01)
                    et_email_phone.setHint(R.string.register_toa_t01)
                }else{
                    mType = 1
                    tv_change.setText(R.string.register_t06)

                    tv_reg01.setText(R.string.register_t07)
                    et_email_phone.setHint(R.string.register_toa_t07)
                }

                et_email_phone.setText("")
            }

            R.id.reg_btn -> sendRegist()
        }
    }

    override fun initData() {
    }

    fun sendRegist(){

        val account = et_email_phone.text.toString()
        if (account.isEmpty()){
            if (mType == 1){
                ToastUtils.showShort(R.string.register_toa_t07)
            }else{
                ToastUtils.showShort(R.string.register_toa_t01)
            }
            return
        }

        val code = et_yzm.text.toString()
        if (code.isEmpty()){
            ToastUtils.showShort(R.string.register_toa_t02)
            return
        }

        val pwd = et_pwd.text.toString()
        if (pwd.isEmpty()){
            ToastUtils.showShort(R.string.register_toa_t03)
            return
        }

        val pwd2 = et_pwd2.text.toString()
        if (pwd2.isEmpty()){
            ToastUtils.showShort(R.string.register_toa_t04)
            return
        }

        if (pwd != pwd2){
            ToastUtils.showShort(R.string.register_t08)
            return
        }

        val api : RegisterApi

        api = if (mType == 1){
            RegisterApi(
                code = code,
                password = pwd,
                username = account,
                mobile = account
            )
        }else{
            RegisterApi(
                code = code,
                password = pwd,
                username = account,
                email = account
            )
        }

        EasyHttp.post(this)
            .api(api).request(object : HttpCallback<HttpData<JSONObject>>(this){

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)

                    ToastUtils.showShort(R.string.register_t09)

                    setResult(Activity.RESULT_OK,
                        Intent().apply {
                            putExtra("account",account)
                            putExtra("type",mType)
                            putExtra("password",pwd)
                        }
                    )

                    finish()
                }
            })

    }

}