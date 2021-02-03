package com.tw.artin.ui.activity

import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import com.just.agentweb.AgentWeb
import com.just.agentweb.AgentWebConfig
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity
import kotlinx.android.synthetic.main.web_activity.*

/*浏览器*/
class BrowserActivity : TwActivity() {

    //是否动态获取网页标题
    var getWebTitle = true

    lateinit var mAgentWeb: AgentWeb

    override fun getLayoutId(): Int = R.layout.web_activity

    override fun initView() {


        intent.getStringExtra("web_title")?.run {
            getWebTitle = false
            setTitle(this)
        }?: setWebTitles()

        AgentWebConfig.removeAllCookies()

        val urls = getUrl()

        mAgentWeb = AgentWeb.with(this)
            .setAgentWebParent(counter_layout,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            .useDefaultIndicator()
            .createAgentWeb()
            .ready()
            .go(urls)


        // AgentWeb 没有把WebView的功能全面覆盖 ，所以某些设置 AgentWeb 没有提供 ， 请从WebView方面入手设置。
        //mAgentWeb.webCreator.webView.overScrollMode = WebView.OVER_SCROLL_NEVER

    }

    fun getUrl() : String{
        return intent.getStringExtra("web_url") ?: ""
    }


    override fun initData() {

    }

    fun setWebTitles(){
        getWebTitle = false
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            true
        } else super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        mAgentWeb.webLifeCycle.onPause()
        super.onPause()
    }

    override fun onResume() {
        mAgentWeb.webLifeCycle.onResume()
        super.onResume()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        Log.i("Info", "onResult:$requestCode onResult:$resultCode")
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        mAgentWeb.webLifeCycle.onDestroy()
    }

}