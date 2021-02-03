package com.tw.artin.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.KeyboardUtils
import com.tw.artin.base.action.ClickAction
import com.tw.artin.base.action.HandlerAction

abstract class BaseActivity : AppCompatActivity(), HandlerAction, ClickAction {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivity()
    }

    protected open fun initActivity() {
        initLayout()
        initView()
        initData()
    }

    /**
     * 获取布局 ID
     */
    protected abstract fun getLayoutId():Int

    /**
     * 初始化控件
     */
    protected abstract fun initView()

    /**
     * 初始化数据
     */
    protected abstract fun initData()

    /**
     * 初始化布局
     */
    protected open fun initLayout() {
        if (getLayoutId() > 0) {
            setContentView(getLayoutId())
            initSoftKeyboard()
        }
    }

    /**
     * 初始化软键盘
     */
    protected open fun initSoftKeyboard() { // 点击外部隐藏软键盘，提升用户体验
        getContentView().setOnClickListener {
            KeyboardUtils.hideSoftInput(this)
        }
    }

    /**
     * 获取当前 Activity 对象
     */
    protected open fun getActivity(): BaseActivity? {
        return this
    }

    /**
     * 和 setContentView 对应的方法
     */
    fun getContentView(): ViewGroup {
        return findViewById(Window.ID_ANDROID_CONTENT)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 设置为当前的 Intent，避免 Activity 被杀死后重启 Intent 还是最原先的那个
        setIntent(intent)
    }

    override fun onDestroy() {
        removeCallbacks()
        super.onDestroy()
    }

    override fun finish() {
        KeyboardUtils.hideSoftInput(this)
        super.finish()
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        v?.let {
            KeyboardUtils.hideSoftInput(it)
        }

    }

}