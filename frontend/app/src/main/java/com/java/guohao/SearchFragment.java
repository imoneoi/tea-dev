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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
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
                    ((SearchFragment)mParent.get()).mRefresh.setRefreshing(false);
                    JSONObject obj = new JSONObject(msg.obj.toString());
                    JSONArray data = obj.getJSONArray("data");
                    for (int i = 0; i < data.length(); ++i) {
                        JSONObject single = data.getJSONObject(i);
                        String label = single.getString("label");
                        String category = single.getString("category");
                        String uri = single.getString("uri");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String[] localDataset;
    private SafeHandler mHandler = new SafeHandler(this);
    private String mSubject;
    private String mSearchKeyword;
    private RecyclerView mView;
    private SwipeRefreshLayout mRefresh;

    public SearchFragment(String subject, String searchKeyword) {
        mSubject = subject;
        mSearchKeyword = searchKeyword;
        localDataset = new String[0];
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
        /*
        mView = view.findViewById(R.id.search_refresh);
        // TODO: finish viewholder https://developer.android.com/guide/topics/ui/layout/recyclerview?hl=zh-cn
        mView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.two_line_list, parent, false);
                return new RecyclerView.ViewHolder(view) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
        */
        return view;
    }

    public void onRefresh() {
        HttpUtils.searchEntity(mSubject, mSearchKeyword, mHandler);
    }
}
