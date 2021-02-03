package com.tw.artin.ui.adapter

import com.chad.library.adapter.base.binder.QuickItemBinder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tw.artin.R
import no.nordicsemi.android.support.v18.scanner.ScanResult

class SearchBinder : QuickItemBinder<ScanResult>(){

    override fun convert(holder: BaseViewHolder, data: ScanResult) {
        holder.setText(R.id.tv_ble_name,data.scanRecord!!.deviceName)
    }

    override fun getLayoutId(): Int {
        return R.layout.search_ble_item
    }
}