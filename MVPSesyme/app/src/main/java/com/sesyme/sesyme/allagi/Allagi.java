package com.sesyme.sesyme.allagi;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;

public class Allagi {

    static ArrayList<Fragment> fragments;
    private Activity activity;
    private ArrayList<String> list;
    private ArrayList<Integer> imagesList;
    static long transitionDuration = 1000;

    private Allagi(@NonNull Activity activity, ArrayList<String> list,
                   ArrayList<Integer> imagesList, ArrayList<Fragment> fragmentsList) {
        this.activity = activity;
        this.list = list;
        this.imagesList = imagesList;
        fragments = fragmentsList;
    }

    public static Allagi initialize(@NonNull Activity activity, ArrayList<String> list,
                                    ArrayList<Integer> imagesList, ArrayList<Fragment> fragmentsList) {
        return new Allagi(activity, list, imagesList, fragmentsList);
    }

    public static ArrayList<Fragment> getFragments() {
        return fragments;
    }

    public void start() {
        activity.finish();
        MenuListActivity.startActivity(activity, list, imagesList);
        activity.overridePendingTransition(0,0);

    }

    public static long getTransitionDuration() {
        return transitionDuration;
    }

    public void setTransitionDuration(long milliSeconds) {
        transitionDuration = milliSeconds;
    }

}
