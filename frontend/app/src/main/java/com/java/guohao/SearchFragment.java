package com.java.guohao;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
                SearchFragment parent = (SearchFragment)mParent.get();
                parent.parseData(msg.obj.toString());
                parent.mRefresh.setRefreshing(false);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPrimary;
        private final TextView mSecondary;
        public ViewHolder(View view) {
            super(view);
            mPrimary = view.findViewById(R.id.two_line_primary_text);
            mSecondary = view.findViewById(R.id.two_line_secondary_text);
        }

        public TextView getPrimary() {
            return mPrimary;
        }

        public TextView getSecondary() {
            return mSecondary;
        }
    }

    public static class EntitySearchResult {
        public String label;
        public String category;
        public String uri;
        EntitySearchResult(String label, String category, String uri) {
            this.label = label;
            this.category = category;
            this.uri = uri;
        }
    }

    private ArrayList<EntitySearchResult> mLocalDataset;
    private SafeHandler mHandler = new SafeHandler(this);
    private String mSubject;
    private String mSearchKeyword;
    private RecyclerView mView;
    private RecyclerView.Adapter<ViewHolder> mAdapter;
    private SwipeRefreshLayout mRefresh;

    public SearchFragment(String subject, String searchKeyword) {
        mSubject = subject;
        mSearchKeyword = searchKeyword;
        mLocalDataset = new ArrayList<>();
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

        mView = view.findViewById(R.id.search_view);
        mView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mAdapter = new RecyclerView.Adapter<SearchFragment.ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.two_line_list, parent, false);
                ViewHolder h = new ViewHolder(view);
                view.findViewById(R.id.two_line_star).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String label = h.getPrimary().getText().toString();
                        if (HttpUtils.user.isFavourite(label)) {
                            Helper.ImageViewAnimatedChange(v.getContext(), v.findViewById(R.id.two_line_star), R.drawable.star_border, R.color.black, 100);
                        } else {
                            Helper.ImageViewAnimatedChange(v.getContext(), v.findViewById(R.id.two_line_star), R.drawable.star, R.color.orange, 100);
                        }
                        HttpUtils.user.reverseFavourite(label);
                    }
                });
                return h;
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.getPrimary().setText(mLocalDataset.get(position).label);
                holder.getSecondary().setText(mLocalDataset.get(position).category);
            }

            @Override
            public int getItemCount() {
                return mLocalDataset.size();
            }
        };
        mView.setAdapter(mAdapter);
        return view;
    }

    private void parseData(String raw_data) {
        try {
            JSONObject obj = new JSONObject(raw_data);
            JSONArray data = obj.getJSONArray("data");
            mLocalDataset.clear();
            for (int i = 0; i < data.length(); ++i) {
                JSONObject single = data.getJSONObject(i);
                String label = single.getString("label");
                String category = single.getString("category");
                String uri = single.getString("uri");
                mLocalDataset.add(new EntitySearchResult(label, category, uri));
            }
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRefresh() {
        HttpUtils.searchEntity(mSubject, mSearchKeyword, mHandler);
    }
}
