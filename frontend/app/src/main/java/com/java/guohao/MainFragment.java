package com.java.guohao;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xuexiang.xui.widget.tabbar.EasyIndicator;

import java.util.HashMap;

public class MainFragment extends Fragment {

    private SearchView mSearchView;
    private EasyIndicator mTabBar;
    private String[] mTitles = {"语文", "数学", "英语"};

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mSearchView = view.findViewById(R.id.main_search);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i("MainFragment", "Search: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mTabBar = view.findViewById(R.id.main_tab);
        mTabBar.setTabTitles(mTitles);
        mTabBar.setViewPager(new FragmentPagerAdapter() {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return new SearchFragment(GlobVar.KEYWORD_OF_SUBJECT.get(mTitles[position]));
            }

            @Override
            public int getCount() {
                return mTitles.length;
            }
        });

        return view;
    }
}