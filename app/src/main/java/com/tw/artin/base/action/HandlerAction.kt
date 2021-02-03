package com.tw.artin.base.action

import android.os.Handler
import android.os.Looper
import android.os.SystemClock

interface HandlerAction {

    val HANDLER : Handler
        get() = Handler(Looper.getMainLooper())

    /**
     * 延迟执行
     */
    fun post(r: Runnable): Boolean {
        return postDelayed(r, 0)
    }

    /**
     * 延迟一段时间执行
     */
    fun postDelayed(r: Runnable, delayMillis: Long): Boolean {
        var times = delayMillis
        if (times < 0) {
            times = 0
        }
        return postAtTime(r, SystemClock.uptimeMillis() + times)
    }

    /**
     * 在指定的时间执行
     */
    fun postAtTime(
        r: Runnable,
        uptimeMillis: Long
    ): Boolean { // 发送和这个 Activity 相关的消息回调
        return HANDLER.postAtTime(r, this, uptimeMillis)
    }

    /**
     * 移除消息回调
     */
    fun removeCallbacks() {
        HANDLER.removeCallbacksAndMessages(this)
    }

}