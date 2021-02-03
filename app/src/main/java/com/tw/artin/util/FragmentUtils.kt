package com.tw.artin.util

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainer
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.blankj.utilcode.util.FragmentUtils

/**
 * @author thp
 * time 2020/10/19
 * desc:
 */
object FragmentUtils {
    fun addFragmentWithTag(
        fragmentManager: FragmentManager,
        fragment: Fragment?,
        @IdRes containerViewId: Int,
        tag: String
    ) {
        checkNotNull(fragment)
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(containerViewId, fragment, tag)
        fragmentManager.fragments.forEach {
            fragmentTransaction.hide(it)
        }
        fragmentTransaction.commit()
    }

    fun showFragment(
        fragmentManager: FragmentManager,
        fragment: Fragment?
    ) {
        checkNotNull(fragment)
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

        fragmentManager.fragments.forEach {
            if (it == fragment) {
                fragmentTransaction.show(fragment)
            } else {
                fragmentTransaction.hide(it)
            }
        }
        fragmentTransaction.commit()
    }

    fun hideFragment(
        fragmentManager: FragmentManager,
        fragment: Fragment
    ) {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.hide(fragment)
        fragmentTransaction.commit()
    }
    fun removeFragment(
        fragmentManager: FragmentManager,
        fragment: Fragment
    ) {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(fragment)
        fragmentTransaction.commit()
    }
}