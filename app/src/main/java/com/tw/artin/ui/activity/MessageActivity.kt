package com.tw.artin.ui.activity

import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseBinderAdapter
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity
import com.tw.artin.http.api.MessageApi
import com.tw.artin.http.api.ReadMessageApi
import com.tw.artin.http.model.HttpData
import com.tw.artin.ui.adapter.MessageBinder
import kotlinx.android.synthetic.main.message_activity.*
import okhttp3.Call
import org.json.JSONObject

class MessageActivity : TwActivity(){

    private val mAdapter by lazy {
        BaseBinderAdapter().apply {
            addItemBinder(MessageBinder())
        }
    }

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.message_activity
    }

    override fun initView() {

        with(message_srlayout){
            setEnableLoadMore(false)
            setOnRefreshListener {
                getList()
            }
        }

        with(msg_recycle){
            layoutManager = LinearLayoutManager(this@MessageActivity)
            adapter = mAdapter
        }

    }

    override fun initData() {
        message_srlayout.autoRefresh()
    }

    fun getList(){

        EasyHttp.post(this)
            .api(MessageApi())
            .request(object : HttpCallback<HttpData<MessageApi.MsgBean>>(this){

                override fun onSucceed(result: HttpData<MessageApi.MsgBean>?) {

                    message_srlayout.finishRefresh()

                    result?.objx?.let {

                        mAdapter.setList(
                            it.content
                        )

                        postDelayed(Runnable {

                            val mIds : MutableList<Int> = mutableListOf()

                            it.content.forEach {itcontent ->
                                mIds.add(itcontent.id)
                            }

                            readData(mIds)

                        },1500)

                    }
                }

                override fun onStart(call: Call?) {
                }
            })

    }

    fun readData(ids : MutableList<Int>){

        EasyHttp.post(this)
            .api(ReadMessageApi(ids))
            .request(object : HttpCallback<HttpData<JSONObject>>(this){

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)


                }
            })

    }
}