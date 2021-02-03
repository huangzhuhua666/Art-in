package com.tw.artin.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tw.artin.App
import com.tw.artin.R
import com.tw.artin.bean.CheckOnLine
import com.tw.artin.bean.DelNoteBean
import com.tw.artin.bean.EffectBean
import com.tw.artin.bean.HeartBean
import com.tw.artin.http.api.ScenesListApi

object BaseInfoData {

    var company_info_id: String = "1"

    var user_name: String = ""

    var Sys_language: String = ""

    //当前场景
    var scenes_cur: ScenesListApi.Content? = null
    //切换场景时候，先取消节点与组的订阅
    var scenes_node_del_change: MutableList<DelNoteBean> = mutableListOf()
    //切换场景，完成时候重新订阅
    var scenes_node_add_change: MutableList<DelNoteBean> = mutableListOf()
    //设备详情使用
    val device_info = mutableListOf<HeartBean>()


    val onlines = mutableListOf<CheckOnLine>()

    var selfIncresas = 0

    fun getNextTid() : Int{

        if (selfIncresas == 255){
            selfIncresas = 0
        }else{
            ++selfIncresas
        }

        return selfIncresas
    }

    var group_head = mutableListOf<Int>().apply {
        for (index in 49153..49407){
            add(index)
        }
    }

    //获取分组 A-Z头部下标
    fun getHeadIndex(address : Int) : Int{
        group_head.forEachIndexed { index, i ->
            if (i == address){
                return index
            }
        }

        return 0
    }


    //MA-01和MA-03特效
    fun getEffectList01() : MutableList<EffectBean>{

        val effectList = mutableListOf<EffectBean>()

        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name01),
                "Candle",
                0,
                0,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name02),
                "Fireplace",
                0,
                1,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name03),
                "Bonfire",
                0,
                2,
                false
            )
        )


        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name04),
                "Intermittent Lightning",
                1,
                0,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name05),
                "Frequent Lightning",
                1,
                1,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name06),
                "Continuous Lightning",
                1,
                2,
                false
            )
        )


        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name07),
                "Short Fireworks",
                2,
                0,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name08),
                "Continuous Fireworks",
                2,
                1,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name09),
                "Fireworks Show",
                2,
                2,
                false
            )
        )


        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name10),
                "Police Car",
                3,
                0,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name11),
                "Fire Truck",
                3,
                1,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name12),
                "Ambulance",
                3,
                2,
                false
            )
        )


        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name13),
                "Slow",
                4,
                0,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name14),
                "Medium",
                4,
                1,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name15),
                "Fast",
                4,
                2,
                false
            )
        )


        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name16),
                "Birthday Party",
                5,
                0,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name17),
                "Home Party",
                5,
                1,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name18),
                "Club",
                5,
                2,
                false
            )
        )


        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name19),
                "Product Launch",
                6,
                0,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name20),
                "Press Conference",
                6,
                1,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name21),
                "Paparazzi",
                6,
                2,
                false
            )
        )

        return effectList
    }


    //MA-01和MA-03特效
    fun getEffectList02() : MutableList<EffectBean>{

        val effectList = mutableListOf<EffectBean>()

        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name01),
                "Candle",
                0,
                0,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name02),
                "Fireplace",
                0,
                1,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name03),
                "Bonfire",
                0,
                2,
                false
            )
        )


        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name04),
                "Intermittent Lightning",
                1,
                0,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name05),
                "Frequent Lightning",
                1,
                1,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name06),
                "Continuous Lightning",
                1,
                2,
                false
            )
        )


        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name19),
                "Product Launch",
                6,
                0,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name20),
                "Press Conference",
                6,
                1,
                false
            )
        )
        effectList.add(
            EffectBean(
                App.instance.getString(R.string.effect_name21),
                "Paparazzi",
                6,
                2,
                false
            )
        )

        return effectList
    }


}