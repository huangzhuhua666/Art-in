package com.tw.artin.dfu

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tw.artin.ui.activity.UpDataDfuActivity


class NotificationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isTaskRoot){
            val intent = Intent(this, UpDataDfuActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtras(getIntent().extras!!)
            startActivity(intent)
        }
        finish()
    }

}