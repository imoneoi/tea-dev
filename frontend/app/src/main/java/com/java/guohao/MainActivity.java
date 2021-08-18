package com.java.guohao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private ViewPager2 mViewPager;
    private MainPagerAdapter mViewAdapter;
    private BottomNavigationView mBottomNav;

    private final int mIdOfPos[] = { R.id.nav_home, R.id.nav_qa, R.id.nav_discover, R.id.nav_me };
    private HashMap<Integer, Integer> mPosOfId; // can't be final

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPosOfId = new HashMap<>();
        for (int i = 0; i < mIdOfPos.length; ++i) {
            mPosOfId.put(mIdOfPos[i], i);
        }

        mViewPager = findViewById(R.id.main_vp);
        mViewPager.setUserInputEnabled(false);
        mBottomNav = findViewById(R.id.bottom_nav);

        mBottomNav.setOnItemSelectedListener(this);

        mViewAdapter = new MainPagerAdapter(this);
        mViewPager.setAdapter(mViewAdapter);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mBottomNav.getMenu().findItem(mIdOfPos[position]).setChecked(true);
            }
        });
    }

    public boolean onNavigationItemSelected (@NonNull MenuItem menuItem) {
        mViewPager.setCurrentItem(mPosOfId.get(menuItem.getItemId()));
        return true;
    }
}