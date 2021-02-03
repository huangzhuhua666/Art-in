package com.tw.artin.ui2.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseProviderMultiAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.dragswipe.DragAndSwipeCallback
import com.chad.library.adapter.base.module.BaseDraggableModule
import com.chad.library.adapter.base.module.DraggableModule
import com.tw.artin.R
import com.tw.artin.base.BaseInfoData
import com.tw.artin.bean.DevicesBean
import com.tw.artin.ui2.fragment.NDeviceFragment
import no.nordicsemi.android.mesh.Group
import no.nordicsemi.android.mesh.MeshNetwork
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode
import java.util.*

class NDeviceListAdapter(val mFragment : NDeviceFragment) :  BaseProviderMultiAdapter<DevicesBean>(),
    DraggableModule {

    val ledOnOff = mutableListOf<String>()

    init {
        addItemProvider(NSearchTopProvider(mFragment))
        addItemProvider(NSearchItemProvider())

        addItemProvider(NGroupProvider(mFragment))
        addItemProvider(NGroupItemProvider(mFragment))
    }


    override fun getItemType(data: List<DevicesBean>, position: Int): Int {
        return data[position].type
    }

    override fun addDraggableModule(baseQuickAdapter: BaseQuickAdapter<*, *>): BaseDraggableModule {

        return BaseDraggableModule(baseQuickAdapter).apply {

            itemTouchHelperCallback = object : DragAndSwipeCallback(this) {

                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {

                    if (viewHolder.itemViewType != 3){
                        return makeMovementFlags(0, 0)
                    }

                    return super.getMovementFlags(recyclerView, viewHolder)
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    source: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean{

                    //原来位置
                    val pos01 = source.adapterPosition
                    val info01 = data[pos01]
                    val size01 = getElementSize(info01.unicastAddress!!)

                    //移动后位置
                    val pos02 = target.adapterPosition
                    val info02 = data[pos02]

                    //不在线  不处理
                    /*if (!info01.isonline!!){
                        return false
                    }
*/
                    if (pos02 <= 0){
                        return false
                    }


                    val mType = info02.type

                    if (mType == 0 || mType == 1){

                        return false
                    }else{

                        //组位置
                        if (mType == 2){

                            val name01 = info02.group_name

                            if (name01 == mFragment.resources.getString(R.string.device_text05)){
                                return false
                                /*if (info02.isonline!!){
                                    return false
                                }else{
                                    return false
                                }*/
                            }else{

                                data.filter { it.group_child_name == name01}.run {

                                    if (isEmpty()){
                                        return true
                                    }

                                    val size02 = getElementSize(get(0).unicastAddress!!)
                                    return size01 == size02

                                }

                            }

                        }else if(mType == 2){

                            //节点位置

                            val name01 = info02.group_child_name

                            if (name01 == mFragment.resources.getString(R.string.device_text05)){
                                return true
                            }else{

                                data.filter { it.group_child_name == name01}.run {

                                    if (isEmpty()){
                                        return true
                                    }

                                    val size02 = getElementSize(get(0).unicastAddress!!)
                                    return size01 == size02

                                }
                            }

                        }else{
                            return false
                        }

                    }
                }
            }

            itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        }
    }

    fun getElementSize(mUnicastAddress : Int) : Int{

        mFragment.getAttachActivity().shearControl.mMeshManagerApi.meshNetwork?.let { meshwork ->

            //获取能用节点
            val nodes_list = mutableListOf<ProvisionedMeshNode>()
            //节点
            val mNodes = meshwork.nodes
            mNodes.forEach {

                meshwork.selectedProvisioner?.let { pro ->

                    if (it.uuid != pro.provisionerUuid){

                        nodes_list.add(it)
                    }
                }
            }

            if (nodes_list.isEmpty()){
                return 0
            }

            nodes_list.forEach {

                if (it.unicastAddress == mUnicastAddress){

                    val e01 = it.elements

                    if (e01.isNotEmpty()){

                        val element = e01[e01.keys.hashCode()]

                        val models = element?.meshModels

                        return models?.size ?: 0
                    }
                }
            }

        }

        return 0

    }


    fun setMoveGroup(pos : Int){

        //最新数据
        val item = data[pos]

        //所属那个组
        val datas = data[pos-1]

        //组
        if(datas.type == 2){

            //相同组  不处理
            if (item.group_child_name == datas.group_name){
                return
            }

            //未分组 删除原来的分组
            if (datas.group_name == mFragment.resources.getString(R.string.device_text05)){

                mFragment.getAttachActivity()
                    .shearControl.delGroupNode(item.group_address!!,item.deviceuuid.toString())

            }else{

                mFragment.getAttachActivity()
                    .shearControl.AddGroupNode(datas.address!!,item.deviceuuid.toString())

            }

        }else if(datas.type == 3){

            //相同组下节点  不处理
            if (item.group_child_name == datas.group_child_name){
                return
            }

            //未分组 删除原来的分组
            if (datas.group_child_name == mFragment.resources.getString(R.string.device_text05)){

                mFragment.getAttachActivity()
                    .shearControl.delGroupNode(item.group_address!!,item.deviceuuid.toString())

            }else{

                mFragment.getAttachActivity()
                    .shearControl.AddGroupNode(datas.group_address!!,item.deviceuuid.toString())

            }

        }
    }

    //名字是否有重复
    fun hasNameGroup(g_name : String) : Boolean{

        data.filter { it.type == 2 }?.run {
            if (this.isEmpty()){
                return false
            }else{
                return any { it.group_name == g_name }
            }
        }
    }


    @SuppressLint("RestrictedApi")
    fun DealWith(){

        mFragment.getAttachActivity().shearControl.mMeshManagerApi.meshNetwork?.let { meshwork ->

            //new
            val new_mGroups = mutableListOf<Group>()

            val y_data = meshwork.groups

            new_mGroups.addAll(y_data)

            //对本地组数据进行筛选
            BaseInfoData.scenes_cur?.let {

                val gIt = new_mGroups.iterator()

                while (gIt.hasNext()){

                    val gInfo = gIt.next()

                    val mAddress = gInfo.address

                    //非未分组进行筛选
                    if (mAddress != 49152){
                        val has_address = it.deviceGroups.any { it.address == mAddress.toString() }
                        if (!has_address){
                            gIt.remove()
                        }
                    }
                }
            }

            //当前适配器数据
            val adapterData = data.filter { it.type == 2}

            //组适配器没有数据
            if (adapterData.isEmpty()){

                new_mGroups.forEach { nGroup ->

                    addData(
                        DevicesBean(
                            type = 2,
                            group_name = nGroup.name,
                            meshUuid = nGroup.meshUuid,
                            address = nGroup.address,
                            isNotGroup = nGroup.name == mFragment.getString(R.string.device_text05)
                        )
                    )

                }

            }else{

                val add_group = mutableListOf<Group>()

                val del_group = mutableListOf<DevicesBean>()

                new_mGroups.forEach { nGroup ->

                    val gName01 = nGroup.name

                    val has_group = adapterData.any { it.group_name == gName01 }

                    //新增组
                    if (!has_group){
                        add_group.add(nGroup)
                    }
                }

                adapterData.forEach { adapterIt ->

                    val gName01 = adapterIt.group_name

                    val has_group = new_mGroups.any { it.name == gName01 }

                    //删除
                    if (!has_group){
                        del_group.add(adapterIt)
                    }
                }

                //新增组有数据
                if (add_group.isNotEmpty()){

                    add_group.forEach {

                        addData(
                            DevicesBean(
                                type = 2,
                                group_name = it.name,
                                meshUuid = it.meshUuid,
                                address = it.address,
                                isNotGroup = it.name == mFragment.getString(R.string.device_text05)
                            )
                        )

                    }
                }
                //新增组有数据

                //删除组有数据
                if (del_group.isNotEmpty()){

                    del_group.forEach {

                        val name01 = it.group_name

                        val notes_del = data.filter { it.group_child_name == name01}

                        notes_del.forEach { notIt ->
                            //删除节点
                            remove(notIt)
                        }

                        //删除组
                        remove(it)

                    }
                }
                //删除组有数据

            }

            //获取能用节点
            val nodes_list = mutableListOf<ProvisionedMeshNode>()
            //节点
            val mNodes = meshwork.nodes
            mNodes.forEach {

                meshwork.selectedProvisioner?.let { pro ->

                    if (it.uuid != pro.provisionerUuid){

                        nodes_list.add(it)
                    }
                }
            }

            //清空所有节点
            data.filter { it.type == 3 }.run {
                forEach {
                    remove(it)
                }
            }


            //当前适配器数据(重新获取)
            val adapterData_cur = data.filter { it.type == 2}

            adapterData_cur.forEach {

                val pos = data.indexOf(it)

                addData(pos+1,
                    getNodeItemData(nodes_list,meshwork,it.address!!)
                )
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun getNodeItemData(nodes_list : MutableList<ProvisionedMeshNode>,
                                mMeshNetwork : MeshNetwork, address : Int) : MutableList<DevicesBean> {

        val d_list = mutableListOf<DevicesBean>()

        if (nodes_list.isNotEmpty()){

            nodes_list.forEach {

                val e01 = it.elements

                if (e01.isNotEmpty()){

                    val element = e01[e01.keys.hashCode()]

                    element?.meshModels?.values?.let { meshModels ->

                        val onOff = meshModels.filter { it.modelName == "Generic On Off Server" }

                        if (onOff.isNotEmpty()){

                            val datass = onOff[0]

                            //只有一个地址 未分组
                            if (datass.subscribedAddresses.size == 1){

                                if (datass.subscribedAddresses[0] == address){

                                    val db = DevicesBean(
                                        type = 3,
                                        note_name = it.nodeName,
                                        group_child_name = mMeshNetwork.getGroup(address).name,
                                        deviceuuid = UUID.fromString(it.uuid),
                                        group_address = datass.subscribedAddresses[0],
                                        boundAppKeyIndexes = datass.boundAppKeyIndexes[0],
                                        unicastAddress = it.unicastAddress,
                                        isonline = mFragment.getAttachActivity().shearControl.getDevOnLine(it.nodeName),
                                        isonOff = getOnOffData(it.nodeName)
                                    )

                                    BaseInfoData.scenes_cur?.let {
                                        it.deviceGroups.forEach { it01 ->
                                            it01.devices.filter { it.address == db.unicastAddress }.run {
                                                if (isNotEmpty()){
                                                    db.note_name_new = get(0).name
                                                }
                                            }
                                        }
                                    }

                                    d_list.add(db)

                                }

                            }else{

                                datass.subscribedAddresses.forEachIndexed { index, i ->

                                    if (i == address && index > 0){

                                        val db = DevicesBean(
                                            type = 3,
                                            note_name = it.nodeName,
                                            group_child_name = mMeshNetwork.getGroup(i).name,
                                            deviceuuid = UUID.fromString(it.uuid),
                                            group_address = i,
                                            boundAppKeyIndexes = datass.boundAppKeyIndexes[0],
                                            unicastAddress = it.unicastAddress,
                                            isonline = mFragment.getAttachActivity().shearControl.getDevOnLine(it.nodeName),
                                            isonOff = getOnOffData(it.nodeName)
                                        )

                                        BaseInfoData.scenes_cur?.let {
                                            it.deviceGroups.forEach { it01 ->
                                                it01.devices.filter { it.address == db.unicastAddress }.run {
                                                    if (isNotEmpty()){
                                                        db.note_name_new = get(0).name
                                                    }
                                                }
                                            }
                                        }

                                        d_list.add(db)

                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        return d_list
    }

    //设备onOff数据
    fun setOnOffData(txt : String){

        if (ledOnOff.isEmpty()){
            ledOnOff.add(txt)
        }else{

            val data01 = txt.split(",")

            var index_now = -1

            ledOnOff.forEachIndexed { index, s ->

                val data02 = s.split(",")

                if (data01[0] == data02[0]){
                    index_now = index
                }
            }

            if (index_now != -1){
                ledOnOff[index_now] = txt
            }else{
                ledOnOff.add(txt)
            }

        }
    }

    fun getOnOffData(txt : String) : Boolean{

        if (ledOnOff.isEmpty()){
            return false
        }else{

            ledOnOff.forEach {

                val data02 = it.split(",")

                if (data02[0] == txt){

                    return data02[1] == "true"

                }
            }
        }

        return false

    }

    //删除扫描对象
    fun moveScanData(name : String){

        val iter = data.iterator()

        while (iter.hasNext()){

            val info = iter.next()

            if (info.type == 1 && info.scan_datas?.device?.name == name){

                remove(info)
            }
        }
    }

}
