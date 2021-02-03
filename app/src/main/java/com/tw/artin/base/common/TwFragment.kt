package com.tw.artin.base.common

import android.view.ViewGroup
import com.blankj.utilcode.util.ToastUtils
import com.gyf.immersionbar.ImmersionBar
import com.hjq.bar.TitleBar
import com.hjq.http.listener.OnHttpListener
import com.tw.artin.base.BaseFragment
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.action.TitleBarAction
import okhttp3.Call

abstract class TwFragment<A : TwActivity> : BaseFragment<A>(), TitleBarAction, OnHttpListener<Any> {

    /** 标题栏对象  */
    private var mTitleBar: TitleBar? = null

    /** 状态栏沉浸  */
    private lateinit var mImmersionBar: ImmersionBar

    /** p层(控制层)  */
    abstract fun <T : BaseInfVp.BasePresenter>getPresenter() : T?

    override fun initFragment() {

        getTitleBar()?.run {
            this.setOnTitleBarListener(this@TwFragment)
        }

        initImmersion()

        super.initFragment()

    }


    /**
     * 初始化沉浸式
     */
    protected open fun initImmersion() { // 初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            statusBarConfig().init()
            // 设置标题栏沉浸
            mTitleBar?.run {
                ImmersionBar.setTitleBar(this@TwFragment,this)
            }
        }
    }

    /**
     * 是否在Fragment使用沉浸式
     */
    open fun isStatusBarEnabled(): Boolean {
        return false
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    protected open fun getStatusBarConfig(): ImmersionBar? {
        return mImmersionBar
    }

    /**
     * 初始化沉浸式
     */
    open fun statusBarConfig(): ImmersionBar {
        mImmersionBar = ImmersionBar.with(this)
            // 默认状态栏字体颜色为黑色
            .statusBarDarkFont(statusBarDarkFont())
            .keyboardEnable(false)
        return mImmersionBar
    }

    /**
     * 获取状态栏字体颜色
     */
    protected open fun statusBarDarkFont(): Boolean { // 返回真表示黑色字体
        return false
    }

    override fun getTitleBar(): TitleBar? {

        if (mTitleBar == null){
            view?.run {
                mTitleBar = findTitleBar(this as ViewGroup)
            }
        }
        return mTitleBar
    }

    /**
     * 显示加载对话框
     */
    open fun showDialog() {
        getAttachActivity().showDialog()
    }

    /**
     * 隐藏加载对话框
     */
    open fun hideDialog() {
        getAttachActivity().hideDialog()
    }

    override fun onResume() {
        super.onResume()
        // 重新初始化状态栏
        statusBarConfig().init()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            // 重新初始化状态栏
            statusBarConfig().init()
        }
    }

    override fun onStart(call: Call?) {
        showDialog()
    }

    override fun onFail(e: Exception?) {
        ToastUtils.showShort(e?.message)
    }

    override fun onEnd(call: Call?) {
        hideDialog()
    }

    override fun onDestroy() {
        hideDialog()
        getPresenter<BaseInfVp.BasePresenter>()?.onDestroy()
        super.onDestroy()
    }

    override fun onSucceed(result: Any?) {

    }

}