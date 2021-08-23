package com.java.guohao;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    private MaterialSearchBar mSearchBar;
    private TabLayout mTabBar;
    private ViewPager2 mViewPager;
    private ArrayList<String> mInitialTitles = new ArrayList<>(); // initial titles by local storage or ...
    private TabLayoutMediator mMediator;
    private FragmentStateAdapter mPagerAdapter;
    private ImageButton mEditButton;

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
        mSearchBar = view.findViewById(R.id.main_search);
        mSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra(getString(R.string.course), mTabBar.getTabAt(mTabBar.getSelectedTabPosition()).getText());
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        mViewPager = view.findViewById(R.id.main_tab_vp);
        mPagerAdapter = new FragmentStateAdapter(this) {
            @Override
            public long getItemId(int position) {
                return MainFragment.this.mInitialTitles.get(position).hashCode(); // or removing current will be a huge bug
            }

            public Fragment createFragment(int pos) {
                if (0 <= pos && pos < getItemCount()) {
                    String query = String.valueOf(mTabBar.getTabAt(pos).getText());
                    return new SearchFragment(GlobVar.KEYWORD_OF_SUBJECT.get(query),
                                              GlobVar.EXAMPLE_SEARCH_KEY_OF_SUBJECT.get(query));
                }
                return null;
            }

            public int getItemCount() {
                return mInitialTitles.size();
            }
        };
        mViewPager.setAdapter(mPagerAdapter);

        mTabBar = view.findViewById(R.id.main_tab);
        mTabBar.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mSearchBar.setPlaceHolder(" 在 " + tab.getText() + " 学科搜索");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
        loadInitialTitles();
        mMediator = new TabLayoutMediator(mTabBar, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(mInitialTitles.get(position));
            }
        });
        mMediator.attach();

        mEditButton = view.findViewById(R.id.main_edit_btn);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TabEditDialogFragment(MainFragment.this).show(getParentFragmentManager(), null);
            }
        });

        return view;
    }

    private void loadInitialTitles() {
        // TODO: load titles from local file
        mInitialTitles.add("语文");
        mInitialTitles.add("数学");
        mInitialTitles.add("英语");
    }

    public ArrayList<String> getInitialTitles() {
        return mInitialTitles;
    }

    public void onDialogPositiveClick(DialogFragment dialog) {
        this.mInitialTitles = ((TabEditDialogFragment) dialog).getInTitles();
        this.mPagerAdapter.notifyDataSetChanged();
    }
}