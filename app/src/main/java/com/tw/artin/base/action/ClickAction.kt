package com.tw.artin.base.action

import android.view.View
import androidx.annotation.IdRes

interface ClickAction : View.OnClickListener {

    fun <V : View?> findViewById(@IdRes id: Int): V

    override fun onClick(v: View?) {
        // 默认不实现，让子类实现
    }

    fun setOnClickListener(@IdRes vararg ids: Int) {
        for (id in ids) {
            findViewById<View>(id).setOnClickListener(this)
        }
    }

    fun setOnClickListener(vararg views: View) {
        for (view in views) {
            view.setOnClickListener(this)
        }
    }
}