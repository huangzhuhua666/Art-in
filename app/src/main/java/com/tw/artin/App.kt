package com.tw.artin

import android.app.Application
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDex
import com.hjq.bar.TitleBar
import com.hjq.bar.style.TitleBarLightStyle
import com.hjq.http.EasyConfig
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.tw.artin.http.ReleaseServer
import com.tw.artin.http.RequestHandler
import okhttp3.OkHttpClient

class App : Application() {

    companion object {
        lateinit var instance: Application
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        instance = this@App

        initView()

        EasyConfig.with(OkHttpClient())
            // 是否打印日志
            .setLogEnabled(BuildConfig.DEBUG)
            // 设置服务器配置
            .setServer(ReleaseServer())
            // 设置请求处理策略
            .setHandler(RequestHandler(instance))
            // 设置请求重试次数
            .setRetryCount(3)
            .into()

    }

    private fun initView() {
        // 标题栏全局样式
        TitleBar.initStyle(object : TitleBarLightStyle(this) {

            override fun getBackground(): Drawable {
                return ColorDrawable(ContextCompat.getColor(instance, android.R.color.black))
            }

            override fun getTitleColor(): Int {
                return ContextCompat.getColor(instance, android.R.color.white)
            }

            override fun getBackIcon(): Drawable {
                return getDrawable(R.drawable.bar_icon_back_white)
            }

            //隐藏下划线
            override fun isLineVisible(): Boolean {
                return !super.isLineVisible()
            }
        })


        // 设置全局的 Header 构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context: Context?, layout: RefreshLayout? ->
            ClassicsHeader(
                context
            ).setEnableLastTime(false)
        }
        // 设置全局的 Footer 构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context: Context?, layout: RefreshLayout? ->
            ClassicsFooter(
                context
            ).setDrawableSize(20f)
        }
    }

}