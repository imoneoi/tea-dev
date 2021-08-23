package com.java.guohao;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainFragment extends Fragment {

    private MaterialSearchBar mSearchBar;
    private TabLayout mTabBar;
    private ViewPager2 mViewPager;
    private ArrayList<String> mInitialTitles = new ArrayList<>(); // initial titles by local storage or ...
    private TabLayoutMediator mMediator;
    private FragmentStateAdapter mPagerAdapter;
    private ImageButton mAddButton;
    private ImageButton mRemoveButton;

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

        mAddButton = view.findViewById(R.id.main_add_btn);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(
                    MainFragment.this.getActivity(),
                    R.style.MaterialAlertDialog_MaterialComponents)
                    .setTitle("添加")
                    .setItems(GlobVar.SUBJECTS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mInitialTitles.add(GlobVar.SUBJECTS[which]);
                        mPagerAdapter.notifyItemInserted(mInitialTitles.size() - 1);
                    }
                }).show();
            }
        });

        mRemoveButton = view.findViewById(R.id.main_remove_btn);
        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                new MaterialAlertDialogBuilder(
                        MainFragment.this.getActivity(),
                        R.style.MaterialAlertDialog_MaterialComponents)
                        .setTitle("删除")
                        .setItems(mInitialTitles.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("info", String.valueOf(which));
                        mInitialTitles.remove(which);
                        mPagerAdapter.notifyItemRemoved(which);
                    }
                }).show();
                 */
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