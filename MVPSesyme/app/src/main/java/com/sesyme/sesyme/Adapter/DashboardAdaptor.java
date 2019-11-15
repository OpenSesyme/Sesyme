package com.sesyme.sesyme.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sesyme.sesyme.Fragments.HomeFragment;
import com.sesyme.sesyme.Fragments.NotificationFragment;
import com.sesyme.sesyme.Fragments.ProfileFragment;
import com.sesyme.sesyme.Fragments.SampleFragment;

public class DashboardAdaptor extends FragmentPagerAdapter {

    public DashboardAdaptor(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return new HomeFragment();
            case 1:
                return new SampleFragment();
            case 2:
                return new NotificationFragment();
            default:
                return new ProfileFragment();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
