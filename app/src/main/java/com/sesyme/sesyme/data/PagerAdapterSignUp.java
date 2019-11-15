package com.sesyme.sesyme.data;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sesyme.sesyme.Fragments.EditProfileFragment;
import com.sesyme.sesyme.Fragments.InterestFragment;
import com.sesyme.sesyme.Fragments.SignUpFragment;

public class PagerAdapterSignUp extends FragmentPagerAdapter {

    public PagerAdapterSignUp(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new SignUpFragment();
            case 1:
                return new EditProfileFragment();
            default:
                return new InterestFragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int nPosition = position + 1;
        return "Step " + nPosition;
    }
}
