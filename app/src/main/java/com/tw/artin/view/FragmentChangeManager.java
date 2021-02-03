package com.tw.artin.view;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class FragmentChangeManager {
    private FragmentManager mFragmentManager;
    private int mContainerViewId;
    /** Fragment切换数组 */
    private ArrayList<Fragment> mFragments;
    /** 当前选中的Tab */
    private int mCurrentTab;
    private ArrayList<Boolean> isInit;
    private boolean isFirst;

    public FragmentChangeManager(FragmentManager fm, int containerViewId, ArrayList<Fragment> fragments) {
        this.mFragmentManager = fm;
        this.mContainerViewId = containerViewId;
        this.mFragments = fragments;
        initFragments();
    }

    /** 初始化fragments */
    private void initFragments() {

        isInit = new ArrayList<>();
        isFirst = true;

        for (Fragment fragment : mFragments) {
            isInit.add(false);
            //mFragmentManager.beginTransaction().add(mContainerViewId, fragment).hide(fragment).commit();
        }

        setFragments(0);
    }

    /** 界面切换控制 */
    public void setFragments(int index) {

        for (int i = 0; i < mFragments.size(); i++) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            Fragment fragment = mFragments.get(i);

            if (i == index) {
                if (!isInit.get(i)){
                    isInit.set(i,true);
                    ft.add(mContainerViewId,fragment).show(fragment);
                }else{
                    ft.show(fragment);
                }

            } else {

                if (isFirst){
                    break;
                }

                if (!isInit.get(i)){
                    isInit.set(i,true);
                    ft.add(mContainerViewId,fragment).hide(fragment);
                }else{
                    ft.hide(fragment);
                }

            }
            ft.commit();
        }
        isFirst = false;
        mCurrentTab = index;
    }

    public int getCurrentTab() {
        return mCurrentTab;
    }

    public Fragment getCurrentFragment() {
        return mFragments.get(mCurrentTab);
    }
}
