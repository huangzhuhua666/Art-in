package com.tw.artin.ui.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.tw.artin.MainTabActivity2
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwFragment
import com.tw.artin.http.api.*
import com.tw.artin.http.model.HttpData
import com.tw.artin.ui.adapter.NAddScenesBinder
import com.tw.artin.ui.adapter.NScenesBinder
import kotlinx.android.synthetic.main.scenes_fragment.*
import okhttp3.Call
import org.json.JSONObject
import java.util.*


class NScenesFragment : TwFragment<MainTabActivity2>(){

    private val mAdapter by lazy {
        BaseBinderAdapter().apply {
            addItemBinder(NScenesBinder(this@NScenesFragment))
            addItemBinder(NAddScenesBinder(this@NScenesFragment))
        }
    }

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
       return R.layout.scenes_fragment
    }

    override fun initView() {

        BusUtils.register(this)

        with(scenes_srlayout){
            setEnableLoadMore(false)
            setOnRefreshListener {
               getList()
            }
        }

        with(scenes_recycle){
            layoutManager = LinearLayoutManager(getAttachActivity())
            adapter = mAdapter
        }

    }

    override fun initData() {
    }

    fun setCurData(api: CurrentApi){

        EasyHttp.post(this)
            .api(api)
            .request(object : HttpCallback<HttpData<JSONObject>>(this) {

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)

                    scenes_srlayout.autoRefresh()
                }
            })

    }

    fun getList(){

        EasyHttp.post(this)
            .api(ScenesListApi())
            .request(object : HttpCallback<HttpData<ScenesListApi.ScenesBean>>(this) {

                override fun onSucceed(result: HttpData<ScenesListApi.ScenesBean>?) {
                    super.onSucceed(result)
                    scenes_srlayout.finishRefresh()

                    result?.objx?.let {

                        val datas = mutableListOf<Any>()

                        //先设置为空
                        BaseInfoData.scenes_cur = null

                        if (it.content.isNotEmpty()) {

                            it.content.forEach {

                                datas.add(it)

                                if (it.isCurrent) {

                                    BaseInfoData.scenes_cur = it
                                }
                            }
                        }

                        datas.add("")

                        mAdapter.setList(datas)

                    } ?: setNotCur()
                }

                override fun onStart(call: Call?) {
                }

                override fun onFail(e: Exception?) {
                    super.onFail(e)
                    mAdapter.setList(mutableListOf(""))
                }
            })
    }

    fun setNotCur(){
        BaseInfoData.scenes_cur = null
        mAdapter.setList(mutableListOf(""))
    }

    fun byteArrayToHexStr(bytes: ByteArray): String? {
        var strHex: String
        val sb = StringBuilder()
        for (aByte in bytes) {
            strHex = Integer.toHexString(aByte.toInt() and 0xFF)
            sb.append("").append(if (strHex.length == 1) "0" else "")
                .append(strHex) // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().toUpperCase(Locale.ROOT)
    }

    fun creatScenes(name: String, isCur: Boolean){

        getAttachActivity().shearControl.mMeshManagerApi.let {

            it.meshNetwork?.let { mwork ->
                val appkey = mwork.createAppKey()
                //绑定network
                appkey.boundNetKeyIndex = 0

                val has_success = mwork.addAppKey(appkey)

                if (has_success){

                    EasyHttp.post(this)
                        .api(
                            AddScenesApi2(
                                name,
                                isCur,
                                byteArrayToHexStr(appkey.key)!!,
                                getAttachActivity().shearControl.getNotGroupDeviceGroups()
                            )
                        )
                        .request(object : HttpCallback<HttpData<JSONObject>>(this){

                            override fun onSucceed(result: HttpData<JSONObject>?) {
                                super.onSucceed(result)
                                ToastUtils.showShort(R.string.led_info_t15)

                                scenes_srlayout.autoRefresh()
                            }
                        })

                }else{
                    ToastUtils.showShort(R.string.scene_t03)
                }

            }


        }

    }

    fun upDataScenes(data: ScenesListApi.Content,appkey : String){

        EasyHttp.post(this)
            .api(
                UpDataNScenesApi(
                    id = data.id,
                    scenesName = data.scenesName,
                    isCurrent = data.isCurrent,
                    applicationKey = appkey,
                    deviceGroups = BaseInfoData.scenes_cur?.deviceGroups
                )
            )
            .request(object : HttpCallback<HttpData<JSONObject>>(this) {

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)
                    ToastUtils.showShort(R.string.led_info_t23)

                    scenes_srlayout.autoRefresh()
                }
            })

    }

    fun delScenes(data: ScenesListApi.Content){

        EasyHttp.post(this)
            .api(
                DelScenesApi(
                    id = data.id
                )
            )
            .request(object : HttpCallback<HttpData<JSONObject>>(this) {

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)
                    ToastUtils.showShort(R.string.led_info_t24)

                    scenes_srlayout.autoRefresh()
                }
            })

    }

    override fun onDestroy() {
        BusUtils.unregister(this)
        super.onDestroy()
    }

    @BusUtils.Bus(tag = "Change_Current", sticky = true)
    fun onEvent(){

        /*val info01 = mAdapter.getItem(Select_pos) as ScenesListApi.Content
        setCurData(CurrentApi(info01.id))*/
    }

    override fun isStatusBarEnabled(): Boolean {
        return !super.isStatusBarEnabled()
    }

    override fun onResume() {
        super.onResume()
        getList()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            getList()
        }
    }

}