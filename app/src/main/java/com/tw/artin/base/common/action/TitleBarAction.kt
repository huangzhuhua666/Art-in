package com.tw.artin.base.common.action

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar

interface TitleBarAction : OnTitleBarListener {

    fun getTitleBar(): TitleBar?

    /**
     * 左项被点击
     *
     * @param v     被点击的左项View
     */
    override fun onLeftClick(v: View?) {}

    /**
     * 标题被点击
     *
     * @param v     被点击的标题View
     */
    override fun onTitleClick(v: View?) {}

    /**
     * 右项被点击
     *
     * @param v     被点击的右项View
     */
    override fun onRightClick(v: View?) {}

    /**
     * 设置标题栏的标题
     */
    fun setTitle(@StringRes id: Int) {
        getTitleBar()?.run {
            setTitle(this.resources.getString(id))
        }
    }

    /**
     * 设置标题栏的标题
     */
    fun setTitle(title: CharSequence?) {
        getTitleBar()?.run {
            setTitle(title)
        }
    }

    /**
     * 设置标题栏的左标题
     */
    fun setLeftTitle(id: Int) {
        getTitleBar()?.run {
            setLeftTitle(id)
        }
    }

    fun setLeftTitle(text: CharSequence?) {
        getTitleBar()?.run {
            setLeftTitle(text)
        }
    }

    fun getLeftTitle(): CharSequence? {

        return getTitleBar()?.run {
            getLeftTitle()
        } ?: ""
    }

    /**
     * 设置标题栏的右标题
     */
    fun setRightTitle(id: Int) {
        getTitleBar()?.run {
            setRightTitle(id)
        }
    }

    fun setRightTitle(text: CharSequence?) {
        getTitleBar()?.run {
            setRightTitle(text)
        }
    }

    fun getRightTitle(): CharSequence? {

        return getTitleBar()?.run {
            getRightTitle()
        } ?: ""
    }

    /**
     * 设置标题栏的左图标
     */
    fun setLeftIcon(id: Int) {
        getTitleBar()?.run {
            setLeftIcon(id)
        }
    }

    fun setLeftIcon(drawable: Drawable?) {
        getTitleBar()?.run {
            setLeftIcon(drawable)
        }
    }

    fun getLeftIcon(): Drawable? {

        return getTitleBar()?.run {
            getLeftIcon()
        }
    }

    /**
     * 设置标题栏的右图标
     */
    fun setRightIcon(id: Int) {
        getTitleBar()?.run {
            setRightIcon(id)
        }
    }

    fun setRightIcon(drawable: Drawable?) {
        if (getTitleBar() != null) {
            getTitleBar()?.run {
                setRightIcon(drawable)
            }
        }
    }

    fun getRightIcon(): Drawable? {

        return getTitleBar()?.run {
            getLeftIcon()
        }
    }

    /**
     * 递归获取 ViewGroup 中的 TitleBar 对象
     */
    fun findTitleBar(group: ViewGroup): TitleBar? {
        for (i in 0 until group.childCount) {
            val view = group.getChildAt(i)
            if (view is TitleBar) {
                return view
            } else if (view is ViewGroup) {
                val titleBar = findTitleBar(view)
                if (titleBar != null) {
                    return titleBar
                }
            }
        }
        return null
    }

}