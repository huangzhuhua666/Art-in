package com.tw.artin.ui2.fragment

import android.content.Intent
import android.util.TypedValue
import android.view.View
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LanguageUtils
import com.blankj.utilcode.util.ToastUtils
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.lxj.xpopup.XPopup
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwFragment
import com.tw.artin.http.api.SetLanguageApi
import com.tw.artin.http.model.HttpData
import com.tw.artin.MainTabActivity2
import com.tw.artin.ui.activity.*
import kotlinx.android.synthetic.main.me_fragment.*


class MeFragment : TwFragment<MainTabActivity2>() {

    var language = ""

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.me_fragment
    }

    override fun initView() {

        getTitleBar()?.run {
            leftTitle = AppUtils.getAppVersionName()
            setLeftSize(TypedValue.COMPLEX_UNIT_SP,18f)
            setLeftColor(android.R.color.white)
        }

        setOnClickListener(R.id.tv_my01,R.id.tv_my02,
            R.id.tv_my03,R.id.tv_my04,R.id.tv_my05,R.id.tv_my06,R.id.tv_language)
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v?.id){

            //账号
            R.id.tv_my01 -> startActivity(
                Intent(getAttachActivity(),MyInfoActivity::class.java)
            )

            //绑定设备
            R.id.tv_my02 -> {}

            //固件升级
            R.id.tv_my03 -> startActivity(
                Intent(getAttachActivity(),DfuActivity::class.java)
            )

            //主页
            R.id.tv_my04 -> startActivity(
                Intent(getAttachActivity(),BrowserActivity::class.java).apply {
                    putExtra("web_title",resources.getString(R.string.my_t05))
                    putExtra("web_url","https://www.art-in.info")
                }
            )

            //消息
            R.id.tv_my05 -> startActivity(
                Intent(getAttachActivity(),MessageActivity::class.java)
            )

            //意见反馈
            R.id.tv_my06 -> startActivity(
                Intent(getAttachActivity(),FeedBackActivity::class.java)
            )

            R.id.tv_language -> {

                XPopup.Builder(context)
                    .hasShadowBg(false)
                    .asBottomList(
                        getAttachActivity().getString(R.string.my_t12),
                        arrayOf(
                            getAttachActivity().getString(R.string.my_t09),
                            getAttachActivity().getString(R.string.my_t10),
                            getAttachActivity().getString(R.string.my_t11)
                        )
                    ) { position, text ->

                        val text = if (position == 0){
                            "zh-Hans"
                        }else if(position == 1){
                            "zh-Hant"
                        }else{
                            "en"
                        }

                        language = text

                        setLanguage(text,false)
                    }
                    .show()

            }
        }
    }

    override fun initData() {

        if (BaseInfoData.Sys_language == ""){


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

            setLanguage(language,true)

        }else{

            language = BaseInfoData.Sys_language
            setLanguage()
        }

    }

    override fun isStatusBarEnabled(): Boolean {
        return !super.isStatusBarEnabled()
    }

    private fun setLanguage(text : String, isNotData : Boolean){

        EasyHttp.post(this)
            .api(
                SetLanguageApi(text)
            ).request(object : HttpCallback<HttpData<String>>(this){

                override fun onSucceed(result: HttpData<String>?) {
                    super.onSucceed(result)

                    if (!isNotData){
                        ToastUtils.showShort(R.string.my_t08)
                    }

                    setLanguage()

                }
            })

    }

    fun setLanguage(){

        when(language){
            "zh-Hans" -> tv_language.setText(R.string.my_t09)
            "zh-Hant" -> tv_language.setText(R.string.my_t10)
            "en" -> tv_language.setText(R.string.my_t11)
        }
    }

}