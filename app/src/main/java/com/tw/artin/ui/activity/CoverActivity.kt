package com.tw.artin.ui.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.LanguageUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity

class CoverActivity : TwActivity() {

    val GET_DATA = 1000

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.cover_activity
    }

    override fun initView() {
        BusUtils.register(this)
        setOnClickListener(R.id.register_tv,R.id.login_tv)
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v?.id){

            R.id.login_tv -> startActivity(
                Intent(this,LoginActivity::class.java).apply {
                    putExtra("account",SPUtils.getInstance().getString("account"))
                    putExtra("password",SPUtils.getInstance().getString("password"))
                }
            )

            R.id.register_tv -> startActivityForResult(
                Intent(this,RegisterActivity::class.java),
                GET_DATA
            )
        }
    }

    override fun initData() {

        val account = SPUtils.getInstance().getString("account")
        val password = SPUtils.getInstance().getString("password")

        if (account != "" && password != ""){

            val isLogout = intent.getBooleanExtra("isLogout",false)

            if (!isLogout){

                postDelayed(
                    Runnable {

                        startActivity(
                            Intent(this,LoginActivity::class.java).apply {
                                putExtra("account",account)
                                putExtra("password",password)
                                putExtra("isAutoLogin",true)
                            }
                        )

                    },400
                )
            }
        }
    }

    override fun onDestroy() {
        BusUtils.unregister(this)
        super.onDestroy()
    }

    @BusUtils.Bus(tag = "cover_finish")
    fun onEvent(){
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK){
            return
        }

        if (requestCode == GET_DATA){

            data?.let {

                val account = it.getStringExtra("account")
                val password = it.getStringExtra("password")

                SPUtils.getInstance().put("account",account)
                SPUtils.getInstance().put("password",password)

                startActivity(
                    Intent(this,LoginActivity::class.java).apply {
                        putExtra("account",account)
                        putExtra("password",password)
                        putExtra("isAutoLogin",true)
                    }
                )

            }

        }


    }

}