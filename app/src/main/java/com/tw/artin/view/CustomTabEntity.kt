package com.tw.artin.view

import androidx.annotation.DrawableRes

interface CustomTabEntity {

    var tabTitle: String?

    @get:DrawableRes
    val tabSelectedIcon: Int

    @get:DrawableRes
    val tabUnselectedIcon: Int

}