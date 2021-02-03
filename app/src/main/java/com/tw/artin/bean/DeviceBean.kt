package com.tw.artin.bean

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.util.*

data class DevicesBean(
    var type : Int = 0,

    //搜索标题
    var isSearch : Boolean? = false,

    //搜索内容
    var scan_datas : ScanResult? = null,
    var isSelect : Boolean? = false,
    var is_scan : Boolean = false,

    //分组头部
    var group_name : String? = "",
    var meshUuid : String? = "",
    var address : Int? = 0,
    var isNotGroup : Boolean? = false,

    //分组节点
    var note_name : String? = "",
    var note_name_new : String? = "",
    var group_child_name : String? = "",
    var deviceuuid : UUID? = null,
    var group_address : Int? = 0,
    var boundAppKeyIndexes :Int? = 0,
    var unicastAddress :Int? = 0,
    var isonOff : Boolean? = false,
    var isonline : Boolean? = false
)

data class LinkSelect(
    var scan_datas : ScanResult,
    var isLink : Boolean? = false
)

data class ControllerGruoupBean(
    var group_name : String? = "",
    var meshUuid : String? = "",
    var address : Int? = 0,
    var boundAppKeyIndexes :Int = 0,
    var isSelect : Boolean = false,
    var color_cur : String = "",
    var brighteness_cur : String = "0",
    var isonOff: Boolean = false,
    var effect: Int? = 0,
    override val childNode: MutableList<BaseNode>?
) : BaseExpandNode()

data class ControllerNoGroupBean(
    var note_name : String? = "",
    var deviceuuid : UUID? = null,
    var group_address : Int? = 0,
    var boundAppKeyIndexes :Int? = 0,
    var unicastAddress :Int? = 0,
    var isSelect : Boolean = false,
    var color_cur : String = "",
    var brighteness_cur : String = "0",
    var isonOff: Boolean = false,
    var effect: Int? = 0,
    override val childNode: MutableList<BaseNode>?
) : BaseNode()

data class ControllerItemBean(
    var note_name : String? = "",
    var isSelect : Boolean = false,
    override val childNode: MutableList<BaseNode>
): BaseNode()


data class EffectBean(
    var name01 : String = "",
    var name02 : String = "",
    var effect : Int = 0,
    var preset : Int = 0,
    var isSelect : Boolean = false
)

data class GroupID(
    var gAddress : String = "",
    var gId : Int = 0,
    var devId : MutableList<DevID> = mutableListOf()
)

data class DevID(
    var dAddress : Int = 0,
    var dId : Int = 0,
    var isIsPowserOn : Boolean = false,
    var lightness : Int = 0,
    var effect : Int = 0,
    var preset : Int = 0,
    var deltaUV : Int = 0,
    var temperature : Int = 0,
    var hue : Int = 0,
    var saturation : Int = 0,
    var deviceType : Int = 0,
    var currentDeviceType : Int = 0
)

data class DelNoteBean(
    var node_address : Int = 0,
    var group_address : Int = 0
)


data class NControllerGruoupBean(

    var group_name : String? = "",
    var address : Int? = 0,
    var isSelect : Boolean = false,
    var isIsPowserOn : Boolean? = false,
    var lightness : Int? = 0,
    var effect : Int? = 0,
    var preset : Int? = 0,
    var deltaUV : Int? = 0,
    var temperature : Int? = 0,
    var hue : Int? = 0,
    var saturation : Int? = 0,
    var deviceType : Int? = 0,
    var currentDeviceType : Int? = 1,

    override var childNode: MutableList<BaseNode>?
) : BaseExpandNode()

data class NControllerNoGroupBean(

    var id : Int? = 0,
    var note_name : String? = "",
    var group_address : Int? = 0,
    var unicastAddress : Int? = 0,
    var boundAppKeyIndexes : Int? = 0,
    var isSelect : Boolean = false,
    var isIsPowserOn : Boolean? = false,
    var lightness : Int? = 0,
    var effect : Int? = 0,
    var preset : Int? = 0,
    var deltaUV : Int? = 0,
    var temperature : Int? = 0,
    var hue : Int? = 0,
    var saturation : Int? = 0,
    var deviceType : Int? = 0,
    var currentDeviceType : Int? = 1,

    override val childNode: MutableList<BaseNode>?
) : BaseNode()

data class NControllerItemBean(
    var note_name : String? = "",
    var unicastAddress : Int? = 0,
    var isSelect : Boolean = false,
    override val childNode: MutableList<BaseNode>
): BaseNode()


data class HeartBean(
    var address : Int,
    var electricQuantity : Int,
    var remainingTime : Int,
    var mTem : Int
)

data class CheckOnLine(
    var address : Int,
    var time : Long
)





