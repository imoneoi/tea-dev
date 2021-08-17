package com.java.guohao;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    public Fragment createFragment(int pos) {
        switch (pos) {
            case 0 : return new MainFragment();
            case 1 : return new QAFragment();
            case 2 : return new DiscoverFragment();
            case 3 : return new MeFragment();
        }
        return null;
    }

    public int getItemCount() {
        return GlobVar.BOTTOM_NAV_LENGTH;
    }
}
