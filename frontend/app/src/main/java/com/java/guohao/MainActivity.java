package com.java.guohao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private ViewPager2 mViewPager;
    private BottomNavigationView mBottomNav;

    private final int[] mIdOfPos = { R.id.nav_home, R.id.nav_qa, R.id.nav_discover, R.id.nav_me };
    private HashMap<Integer, Integer> mPosOfId; // can't be final

    private static class SafeHandler extends Handler {
        private final WeakReference<MainActivity> mParent;
        public SafeHandler(MainActivity parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                super.handleMessage(msg);
                MainActivity parent = mParent.get();
                JSONObject obj = new JSONObject(msg.obj.toString());
                System.out.println(obj.getInt("ok"));
                if (obj.getInt("ok") == 0) {
                    parent.jump2login();
                } else {
                    parent.saveUserData();
                    HttpUtils.user.getUserData();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    SafeHandler mHandler = new SafeHandler(this);
    String mSessionStorageKey = Storage.getKey(getClass().getSimpleName(), "user");
    String mUsernameStorageKey = Storage.getKey(getClass().getSimpleName(), "username");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPosOfId = new HashMap<>();
        for (int i = 0; i < mIdOfPos.length; ++i) {
            mPosOfId.put(mIdOfPos[i], i);
        }

        HttpUtils.user.setContext(this);
        if (Storage.contains(this, mSessionStorageKey)) {
            HttpUtils.user.session = Storage.load(this, mSessionStorageKey);
            HttpUtils.user.getUserData();
            HttpUtils.user.username = Storage.load(this, mUsernameStorageKey);
            HttpUtils.user.loadLocalData();
        }

        mViewPager = findViewById(R.id.main_vp);
        mViewPager.setUserInputEnabled(false);
        mViewPager.setOffscreenPageLimit(1);
        mBottomNav = findViewById(R.id.bottom_nav);

        mBottomNav.setOnItemSelectedListener(this);

        MainPagerAdapter mViewAdapter = new MainPagerAdapter(this);
        mViewPager.setAdapter(mViewAdapter);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mBottomNav.getMenu().findItem(mIdOfPos[position]).setChecked(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        HttpUtils.User.checkSession(HttpUtils.user.session, mHandler);
    }

    public boolean onNavigationItemSelected (@NonNull MenuItem menuItem) {
        mViewPager.setCurrentItem(mPosOfId.get(menuItem.getItemId()));
        return true;
    }

    void jump2login() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void saveUserData() {
        Storage.save(this, mSessionStorageKey, HttpUtils.user.session);
        Storage.save(this, mUsernameStorageKey, HttpUtils.user.username);
    }

}