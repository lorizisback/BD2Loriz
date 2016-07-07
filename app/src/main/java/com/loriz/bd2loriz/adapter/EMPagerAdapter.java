package com.loriz.bd2loriz.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.loriz.bd2loriz.fragments.MapFragment;
import com.loriz.bd2loriz.fragments.QueryFragment;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Marco on 06/07/2016.
 */


public class EMPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> fragmentList;


    public EMPagerAdapter(FragmentManager fm) {
        super(fm);
        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(new MapFragment());
        fragmentList.add(new QueryFragment());
        }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return fragmentList.get(0);
        } else {
            return fragmentList.get(1);
        }
    }

    public Fragment getFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
