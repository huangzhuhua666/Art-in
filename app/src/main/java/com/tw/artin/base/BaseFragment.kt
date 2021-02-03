package com.tw.artin.base

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.tw.artin.base.action.ClickAction
import com.tw.artin.base.action.HandlerAction

abstract class BaseFragment<A : BaseActivity> : Fragment(), HandlerAction, ClickAction {

    /** Activity 对象  */
    private var mActivity: A? = null

    /** 根布局  */
    private var mRootView: View? = null

    /** 当前是否加载过  */
    private var mLoading = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity() as A
    }

    override fun onDetach() {
        removeCallbacks()
        mActivity = null
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mLoading = false

        return if (getLayoutId() > 0) {
            inflater.inflate(getLayoutId(), null).also { mRootView = it }
        } else {
            null
        }

    }

    override fun onResume() {
        super.onResume()
        if (!mLoading) {
            mLoading = true
            initFragment()
        }
    }

    override fun onDestroyView() {
        mLoading = false
        mRootView = null
        super.onDestroyView()
    }


    /**
     * 获取绑定的 Activity，防止出现 getActivity 为空
     */
    open fun getAttachActivity(): A {
        return mActivity!!
    }

    protected open fun initFragment() {
        initView()
        initData()
    }

    /**
     * 获取布局 ID
     */
    protected abstract fun getLayoutId(): Int

    /**
     * 初始化控件
     */
    protected abstract fun initView()

    /**
     * 初始化数据
     */
    protected abstract fun initData()

    /**
     * Fragment 返回键被按下时回调
     */
    open fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean { // 默认不拦截按键事件，回传给 Activity
        return false
    }

    override fun <V : View?> findViewById(@IdRes id: Int): V {
        return  mRootView!!.findViewById<V>(id)
    }

}