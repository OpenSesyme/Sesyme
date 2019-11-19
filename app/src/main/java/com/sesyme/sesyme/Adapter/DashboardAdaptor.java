package com.sesyme.sesyme.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sesyme.sesyme.Fragments.ClustersFragment;
import com.sesyme.sesyme.Fragments.HomeFragment;
import com.sesyme.sesyme.Fragments.InfoPortalFragment;
import com.sesyme.sesyme.Fragments.NotificationFragment;

import org.jetbrains.annotations.NotNull;

public class DashboardAdaptor extends FragmentPagerAdapter {

    public DashboardAdaptor(FragmentManager fm) {
        super(fm);
    }


    @NotNull
    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return new HomeFragment();
            case 1:
                return new InfoPortalFragment();
//            case 2:
//                return new BooksFragment();
            case 2:
                return new ClustersFragment();
            default:
                return new NotificationFragment();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
