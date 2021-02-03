package com.tw.artin.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tw.artin.base.common.TwFragment

class ViewPager2Adapter(factivity: FragmentActivity, private val fragmentNames : ArrayList<Class<*>>) :
    FragmentStateAdapter(factivity) {

    override fun getItemCount(): Int {
        return fragmentNames.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentNames[position].newInstance() as TwFragment<*>
    }
}