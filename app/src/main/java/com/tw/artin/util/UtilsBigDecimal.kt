package com.tw.artin.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * 加、减、乘、除 高精度计算工具类
 * */
object UtilsBigDecimal {

    // 需要精确至小数点后几位
    const val DECIMAL_POINT_NUMBER:Int = 2

    // 加法运算
    @JvmStatic
    fun add(d1:Double,d2:Double):Double = BigDecimal(d1).add(BigDecimal(d2)).setScale(
        DECIMAL_POINT_NUMBER,
        RoundingMode.HALF_UP).toDouble()

    // 减法运算
    @JvmStatic
    fun sub(d1:Double,d2: Double):Double = BigDecimal(d1).subtract(BigDecimal(d2)).setScale(
        DECIMAL_POINT_NUMBER,
        RoundingMode.HALF_UP).toDouble()

    // 乘法运算
    @JvmStatic
    fun mul(d1:Double,d2: Double):Double = BigDecimal(d1).multiply(BigDecimal(d2)).setScale(
        DECIMAL_POINT_NUMBER,
        RoundingMode.HALF_UP).toDouble()

    // 除法运算
    @JvmStatic
    fun div(d1:Double,d2: Double):Double = BigDecimal(d1).divide(
        BigDecimal(d2),
        DECIMAL_POINT_NUMBER,
        BigDecimal.ROUND_HALF_UP).toDouble()//向上取整数

    //double转Float
    @JvmStatic
    fun GetFInt(value : Double) : Float{
        DecimalFormat("0").run {
            setRoundingMode(RoundingMode.HALF_UP)
            return format(value).toFloat()
        }
    }

    //double转Float
    @JvmStatic
    fun GetFInt2(value : Double) : Float{
        DecimalFormat("0").run {
            setRoundingMode(RoundingMode.HALF_DOWN)
            return format(value).toFloat()
        }
    }

    //double转String 保留整数
    @JvmStatic
    fun GetIntString(value : Double) : String{
        DecimalFormat("0").run {
            setRoundingMode(RoundingMode.HALF_UP)
            return format(value)
        }
    }

}
