package com.tw.artin.ui2.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.BaseInfoData
import com.tw.artin.base.common.TwFragment
import com.tw.artin.bean.DelNoteBean
import com.tw.artin.http.api.*
import com.tw.artin.http.model.HttpData
import com.tw.artin.MainTabActivity2
import com.tw.artin.ui2.adapter.AddScenesBinder
import com.tw.artin.ui2.adapter.ScenesBinder
import kotlinx.android.synthetic.main.scenes_fragment.*
import okhttp3.Call
import org.json.JSONObject
import java.lang.Exception

class ScenesFragment : TwFragment<MainTabActivity2>() {

    var Select_pos = -1

    private val mAdapter by lazy {
        BaseBinderAdapter().apply {
            addItemBinder(ScenesBinder(this@ScenesFragment))
            addItemBinder(AddScenesBinder(this@ScenesFragment))
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
        getList()
    }

    fun getList(){

        EasyHttp.post(this)
            .api(ScenesListApi())
            .request(object : HttpCallback<HttpData<ScenesListApi.ScenesBean>>(this){

                override fun onSucceed(result: HttpData<ScenesListApi.ScenesBean>?) {
                    super.onSucceed(result)
                    scenes_srlayout.finishRefresh()

                    result?.objx?.let {

                        val datas = mutableListOf<Any>()

                        //先设置为空
                        BaseInfoData.scenes_cur = null

                        if (it.content.isNotEmpty()){

                            it.content.forEach {

                                datas.add(it)

                                if (it.isCurrent){

                                    BaseInfoData.scenes_cur = it
                                }
                            }
                        }

                        datas.add("")

                        mAdapter.setList(datas)

                        if (getAttachActivity().shearControl.operating_mesh == 7){

                            BaseInfoData.scenes_cur?.let {

                                it.deviceGroups.forEach {it01 ->

                                    if (it01.devices.isNotEmpty() && !it01.isUngrouped){

                                        it01.devices.forEach {it02 ->

                                            BaseInfoData.scenes_node_add_change.add(
                                                DelNoteBean(it02.address,it01.address.toInt())
                                            )
                                        }
                                    }
                                }

                                //删除没节点数据
                                val node_it = BaseInfoData.scenes_node_add_change.iterator()
                                while (node_it.hasNext()){
                                    val info = node_it.next()
                                    if (info.node_address == 0){
                                        node_it.remove()
                                    }
                                }


                                //没订阅任何组，直接刷新列表
                                if (BaseInfoData.scenes_node_add_change.isEmpty()){

                                    getAttachActivity().shearControl.operating_mesh = 0

                                    getAttachActivity().shearControl.let {
                                        it.mMeshManagerApi.meshNetwork?.let {workIt ->
                                            it.dealWithDatas(workIt)
                                        }
                                    }

                                }else{

                                    //新增订阅
                                    getAttachActivity().shearControl.AddGroupMoreNode()
                                }

                            }

                        }

                    }?: setNotCur()
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

    fun creatScenes(name : String, isCur : Boolean){

        EasyHttp.post(this)
            .api(
                AddScenesApi(
                    name,isCur,getAttachActivity().shearControl.getNotGroupDeviceGroups()
                )
            )
            .request(object : HttpCallback<HttpData<JSONObject>>(this){

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)
                    ToastUtils.showShort(R.string.led_info_t15)

                    scenes_srlayout.autoRefresh()
                }
            })

    }

    fun upDataScenes(data : ScenesListApi.Content){

        EasyHttp.post(this)
            .api(
                UpDataScenesApi(
                    id = data.id,
                    scenesName = data.scenesName,
                    isCurrent = data.isCurrent
                )
            )
            .request(object : HttpCallback<HttpData<JSONObject>>(this){

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)
                    ToastUtils.showShort(R.string.led_info_t23)

                    scenes_srlayout.autoRefresh()
                }
            })

    }

    fun delScenes(data : ScenesListApi.Content){

        EasyHttp.post(this)
            .api(
                DelScenesApi(
                    id = data.id
                )
            )
            .request(object : HttpCallback<HttpData<JSONObject>>(this){

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)
                    ToastUtils.showShort(R.string.led_info_t24)

                    scenes_srlayout.autoRefresh()
                }
            })

    }

    fun setCurData(api : CurrentApi){

        EasyHttp.post(this)
            .api(api)
            .request(object : HttpCallback<HttpData<JSONObject>>(this){

                override fun onSucceed(result: HttpData<JSONObject>?) {
                    super.onSucceed(result)

                    scenes_srlayout.autoRefresh()
                }
            })

    }

    @BusUtils.Bus(tag = "Change_Current",sticky = true)
    fun onEvent(){

        val info01 = mAdapter.getItem(Select_pos) as ScenesListApi.Content

        setCurData(CurrentApi(info01.id))
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

    override fun onDestroy() {
        BusUtils.unregister(this)
        super.onDestroy()
    }

}