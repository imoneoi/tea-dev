package com.java.guohao;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class SearchFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static class SafeHandler extends Handler {
        private final WeakReference<Fragment> mParent;

        public SafeHandler(Fragment parent) {
            mParent = new WeakReference<Fragment>(parent);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == GlobVar.SUCCESS) {
                try {
                    JSONObject obj = new JSONObject(msg.obj.toString());
                    ((SearchFragment)mParent.get()).mTextView.setText(obj.toString());
                    ((SearchFragment)mParent.get()).mRefresh.setRefreshing(false);
                    // TODO: handle obj
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    SafeHandler mHandler = new SafeHandler(this);
    String mSubject;
    String mSearchKeyword;
    TextView mTextView;
    SwipeRefreshLayout mRefresh;

    public SearchFragment(String subject, String searchKeyword) {
        mSubject = subject;
        mSearchKeyword = searchKeyword;
    }

    public void onCreate(Bundle savedInstanceState, String searchKeyword) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mRefresh = view.findViewById(R.id.search_refresh);
        mRefresh.setOnRefreshListener(this); // onRefresh listens it
        mTextView = view.findViewById(R.id.search_text);
        mTextView.setText(mSubject + " " + mSearchKeyword);
        return view;
    }

    public void onRefresh() {
        HttpUtils.searchEntity(mSubject, mSearchKeyword, mHandler);
    }
}
