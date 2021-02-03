package com.tw.artin.ui.activity

import android.content.Intent
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import com.blankj.utilcode.util.*
import com.hjq.http.EasyConfig
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwActivity
import com.tw.artin.http.api.ForGetPwdApi
import com.tw.artin.http.api.LoginApi
import com.tw.artin.http.api.SendMsgApi
import com.tw.artin.http.model.HttpData
import com.tw.artin.MainTabActivity2
import kotlinx.android.synthetic.main.login_activity.*
import org.json.JSONObject

class LoginActivity : TwActivity(){

    var mType = 1 //1密码  2验证码

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.login_activity
    }

    override fun initView() {

        val account = intent.getStringExtra("account")
        val password = intent.getStringExtra("password")

        et_account.setText(account)
        et_pwd_code.setText(password)

        setOnClickListener(R.id.cd_view,R.id.login_btn,R.id.tv_pwd_change)
    }

    override fun initData() {

        val isAutoLogin = intent.getBooleanExtra("isAutoLogin",false)

        if (isAutoLogin){
            loginData()
        }

    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v?.id){

            R.id.cd_view -> sendMsg()

            R.id.login_btn -> {
                if (mType == 1){
                    loginData()
                }else{
                    CodeLogin()
                }
            }

            R.id.tv_pwd_change -> changeType()

        }
    }

    private fun loginData(){

        val account = et_account.text.toString()
        if (account.isEmpty()){
            ToastUtils.showShort(R.string.login_toa_t01)
            return
        }

        val isEmail = RegexUtils.isEmail(account)

        val type = if (isEmail){
            2
        }else{
            1
        }

        val code = et_pwd_code.text.toString()
        if (code.isEmpty()){
            if (mType == 1){
                ToastUtils.showShort(R.string.login_toa_t02)
            }else{
                ToastUtils.showShort(R.string.login_toa_t03)
            }
            return
        }

        val language : String
        val lu = LanguageUtils.getSystemLanguage()

        if (lu.language == "zh"){

            if (lu.country == "HK" || lu.country == "TW" || lu.country == "MO"){
                language = "zh-Hant" //繁体
            }else{
                language = "zh-Hans" //简体
            }

        }else{
            language = "en" //英文
        }


        val api : LoginApi

        if (type == 1){
            api = LoginApi(
                language = language,
                username = account,
                password = code,
                mobile = account
            )
        }else{
            api = LoginApi(
                language = language,
                username = account,
                password = code,
                email = account
            )
        }

        EasyHttp.post(this)
            .api(
                api
            ).request(object : HttpCallback<HttpData<LoginApi.LoginBean>>(this){

                override fun onSucceed(result: HttpData<LoginApi.LoginBean>?) {
                    super.onSucceed(result)

                    result?.objx?.let {

                        EasyConfig.getInstance()
                            .addHeader("Authorization", it.token)

                        SPUtils.getInstance().put("account",et_account.text.toString())
                        SPUtils.getInstance().put("password",et_pwd_code.text.toString())

                        BaseInfoData.Sys_language = it.language ?: ""

                        BusUtils.post("cover_finish")

                        startActivity(
                            Intent(this@LoginActivity, MainTabActivity2::class.java)
                            //Intent(this@LoginActivity, MainTabActivity::class.java)
                        )
                        finish()
                    }
                }
            })

    }

    private fun CodeLogin(){

        val account = et_account.text.toString()
        if (account.isEmpty()){
            ToastUtils.showShort(R.string.login_toa_t01)
            return
        }

        val isEmail = RegexUtils.isEmail(account)

        val type = if (isEmail){
            2
        }else{
            1
        }

        val code = et_pwd_code.text.toString()
        if (code.isEmpty()){
            ToastUtils.showShort(R.string.login_toa_t03)
            return
        }

        EasyHttp.post(this)
            .api(
                ForGetPwdApi(
                    account,
                    type,
                    code
                )
            ).request(object : HttpCallback<HttpData<ForGetPwdApi.ForgetBean>>(this){

                override fun onSucceed(result: HttpData<ForGetPwdApi.ForgetBean>?) {
                    super.onSucceed(result)

                    result?.objx?.let {

                        EasyConfig.getInstance()
                            .addHeader("Authorization", it.token)

                        SPUtils.getInstance().put("account",et_account.text.toString())
                        SPUtils.getInstance().put("password","")

                        BusUtils.post("cover_finish")

                        startActivity(
                            Intent(this@LoginActivity, MainTabActivity2::class.java)
                            //Intent(this@LoginActivity, MainTabActivity::class.java)
                        )
                        finish()
                    }

                }
            })

    }

    private fun sendMsg(){

        val account = et_account.text.toString()
        if (account.isEmpty()){
            ToastUtils.showShort(R.string.login_toa_t01)
            return
        }

        val isEmail = RegexUtils.isEmail(account)

        val type = if (isEmail){
            2
        }else{
            1
        }

        EasyHttp.post(this)
            .api(
                SendMsgApi(
                    account,
                    type
                )
            ).request(object : HttpCallback<HttpData<JSONObject>>(this){

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)

                    cd_view.start()
                    ToastUtils.showShort(R.string.common_code_send_hint)
                }

            })

    }

    private fun changeType(){

        if (mType == 1){

            mType = 2

            tv_pwd_change.setText(R.string.login_t05)

            tv_pwdtype.setText(R.string.login_t03)
            et_pwd_code.setHint(R.string.login_toa_t03)
            et_pwd_code.transformationMethod = HideReturnsTransformationMethod.getInstance()

            cd_view.visibility = View.VISIBLE

        }else{

            mType = 1

            tv_pwd_change.setText(R.string.login_t04)

            tv_pwdtype.setText(R.string.login_t02)
            et_pwd_code.setHint(R.string.login_toa_t02)
            et_pwd_code.transformationMethod = PasswordTransformationMethod.getInstance()

            cd_view.visibility = View.INVISIBLE
        }

        et_pwd_code.setText("")

    }

}