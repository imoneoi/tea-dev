package com.java.guohao;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

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
            SearchFragment sf = new SearchFragment(mCourse, String.valueOf(text));
            getSupportFragmentManager().beginTransaction().replace(R.id.search_activity_view, sf).commit();
        }

        @Override
        public void onButtonClicked(int buttonCode) {}
    }

    private ArrayList<String> mSearchHistory = new ArrayList<>();
    private MaterialSearchBar mSearchBar;
    private SearchListener mListener = new SearchListener();
    private FragmentContainerView mView;
    private String mChineseCourse;
    private String mCourse;

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
    }
}