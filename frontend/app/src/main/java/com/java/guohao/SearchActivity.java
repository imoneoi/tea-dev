package com.java.guohao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.google.android.material.chip.Chip;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.Arrays;

public class SearchActivity extends AppCompatActivity {
    private class SearchListener implements MaterialSearchBar.OnSearchActionListener {
        int historySize;
        SearchListener() {
            super();
            historySize = mSearchHistory.size();
        }

        @Override
        public void onSearchStateChanged(boolean enabled) {
            if (mSearchHistory.size() != historySize) {
                Storage.save(SearchActivity.this, getString(R.string.storage_search_key), Helper.array2Str(mSearchHistory.toArray(new String[0])));
                historySize = mSearchHistory.size();
            }
            if (!enabled) {
                SearchActivity.this.finishAfterTransition();
            }
        }

        @Override
        public void onSearchConfirmed(CharSequence text) {
            Log.i("MainFragment", "Search: " + text); // TODO: search
            mSearchHistory.add(0, text.toString());
            System.out.println(mSearchHistory.toString());
            Storage.save(SearchActivity.this, getString(R.string.storage_search_key), Helper.array2Str(mSearchHistory.toArray(new String[0])));

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            mSearchFragment.setSearchInfoAndInit(mCourse, text.toString());
         }

        @Override
        public void onButtonClicked(int buttonCode) {}
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Chip mChip;

        public ViewHolder(View view) {
            super(view);
            mChip = view.findViewById(R.id.search_filter_chip);
        }

        Chip getChip() {
            return mChip;
        }
    }

    public class FilterAdapter extends RecyclerView.Adapter<SearchActivity.ViewHolder> {
        ArrayMap<String, Integer> mLocalDataset;
        ArrayMap<String, Integer> mAllData;

        public FilterAdapter(ArrayMap<String, Integer> dataset, ArrayMap<String, Integer> allData) {
            update(dataset, allData);
        }

        public void update(ArrayMap<String, Integer> dataset, ArrayMap<String, Integer> allData) {
            mLocalDataset = dataset;
            mAllData = allData;
        }

        @NonNull
        @Override
        public SearchActivity.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chip_search_filter, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchActivity.ViewHolder holder, int position) {
            String val = mAllData.keyAt(position);
            Chip chip = holder.getChip();
            chip.setText(val);
            if (mLocalDataset.containsKey(val)) {
                chip.setChecked(true);
            }
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chip.isChecked()) mLocalDataset.remove(val);
                    else mLocalDataset.put(val, 1);
                    chip.setChecked(!chip.isChecked());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAllData.size();
        }
    }

    private ArrayList<String> mSearchHistory = new ArrayList<>();
    private MaterialSearchBar mSearchBar;
    private SearchListener mListener = new SearchListener();
    private FragmentContainerView mView;
    private String mChineseCourse;
    private String mCourse;
    private SearchFragment mSearchFragment;
    private FilterAdapter mLabelAdapter;
    private FilterAdapter mCategoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();
        mChineseCourse = intent.getStringExtra(getString(R.string.course));
        mCourse = GlobVar.KEYWORD_OF_SUBJECT.get(mChineseCourse);
        mSearchBar = findViewById(R.id.search_activity_search);
        mSearchBar.setOnSearchActionListener(mListener);
        mSearchBar.setHint("在 " + mChineseCourse + " 学科搜索");
        if (Storage.contains(this, getString(R.string.storage_search_key))) {
            mSearchHistory = new ArrayList<>(Arrays.asList(Helper.str2Array(
                    Storage.load(this, getString(R.string.storage_search_key)))));
        }
        mSearchBar.setLastSuggestions(mSearchHistory);
        mSearchBar.callOnClick();

        mView = findViewById(R.id.search_activity_view);

        DrawerLayout layout = findViewById(R.id.search_activity_drawer);
        layout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mSearchFragment.filterData();
            }
        });

        mSearchFragment = new SearchFragment("", "", this);
        getSupportFragmentManager().beginTransaction().replace(R.id.search_activity_view, mSearchFragment).commit();

        RecyclerView mCategoryView = layout.findViewById(R.id.search_filter_category_view);
        mCategoryView.setLayoutManager(new GridLayoutManager(this, 3));
        mCategoryAdapter = new FilterAdapter(
                mSearchFragment.getFilter().mCategory, mSearchFragment.getFilter().mCategorySet);
        mCategoryView.setAdapter(mCategoryAdapter);

        RecyclerView mLabelView = layout.findViewById(R.id.search_filter_label_view);
        mLabelView.setLayoutManager(new GridLayoutManager(this, 3));
        mLabelAdapter = new FilterAdapter(
                mSearchFragment.getFilter().mLabel, mSearchFragment.getFilter().mLabelSet);
        mLabelView.setAdapter(mLabelAdapter);

        ImageButton filterButton = findViewById(R.id.search_activity_filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.open();
            }
        });
    }

    public void onSearchFragmentUpdate() {
        mLabelAdapter.notifyDataSetChanged();
        mCategoryAdapter.notifyDataSetChanged();
    }
}