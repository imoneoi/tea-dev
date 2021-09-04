package com.java.guohao;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.ref.WeakReference;

public class SearchFragment extends BasicListFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static class SafeHandler extends Handler {
        private final WeakReference<BasicListFragment> mParent;
        public SafeHandler(BasicListFragment parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == GlobVar.SUCCESS_FROM_INTERNET) {
                SearchFragment parent = (SearchFragment)mParent.get();
                String data = msg.obj.toString();
                parent.parseData(data);
                Storage.save(parent.requireContext(), parent.mStorageKey, data);
                parent.mRefresh.setRefreshing(false);
            }
        }
    }
    protected String mSearchKeyword;

    private SafeHandler mHandler = new SafeHandler(this);

    public SearchFragment() {
        this(null, "", "");
    }

    public SearchFragment(String course, String searchKeyword) {
        this(null, course, searchKeyword);
    }

    public SearchFragment(SearchActivity parent, String course, String searchKeyword) {
        super(parent, course, searchKeyword);
        this.mSearchKeyword = searchKeyword;
    }

    public void setSearchInfoAndInit(String course, String searchKeyword) {
        mCourse = course;
        mSearchKeyword = searchKeyword;
        updateStorageKey(searchKeyword);
        initData();
    }

    @Override
    public void onRefresh() {
        HttpUtils.searchEntity(mCourse, mSearchKeyword, mHandler);
    }
}
