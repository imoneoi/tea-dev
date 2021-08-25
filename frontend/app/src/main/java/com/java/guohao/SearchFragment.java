package com.java.guohao;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPrimary;
        private final TextView mSecondary;
        private final ImageView mFavourite;

        public ViewHolder(View view) {
            super(view);
            mPrimary = view.findViewById(R.id.two_line_primary_text);
            mSecondary = view.findViewById(R.id.two_line_secondary_text);
            mFavourite = view.findViewById(R.id.two_line_star);
        }

        public TextView getPrimary() {
            return mPrimary;
        }

        public TextView getSecondary() {
            return mSecondary;
        }

        public ImageView getFavourite() {
            return mFavourite;
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

    public class EntitySearchResultFilter {
        public ArrayMap<String, Integer> mLabel;
        public ArrayMap<String, Integer> mCategory;

        public ArrayMap<String, Integer> mLabelSet;
        public ArrayMap<String, Integer> mCategorySet;

        EntitySearchResultFilter() {
            mLabel = new ArrayMap<>();
            mCategory = new ArrayMap<>();
            mLabelSet = new ArrayMap<>();
            mCategorySet = new ArrayMap<>();
        }

        void reset() {
            mLabel.clear();
            mCategory.clear();
            mLabelSet.clear();
            mCategorySet.clear();
            for (EntitySearchResult result : mOriginalDataset) {
                mLabelSet.put(result.label, 1);
                mCategorySet.put(result.category, 1);
            }
        }

        public ArrayList<EntitySearchResult> filter(ArrayList<EntitySearchResult> in) {
            ArrayList<EntitySearchResult> ret = new ArrayList<>();
            for (EntitySearchResult result : in) {
                boolean put = false;
                if (!mLabel.isEmpty()) {
                    for (String s : mLabel.keySet()) {
                        if (result.label.contains(s)) {
                            put = true;
                            break;
                        }
                    }
                } else {
                    put = true;
                }
                if (!mCategory.isEmpty()) {
                    for (String s : mCategory.keySet()) {
                        if (result.category.contains(s)) {
                            put = true;
                            break;
                        }
                    }
                } else {
                    put = true;
                }
                if (put) ret.add(result);
            }
            return ret;
        }
    }

    private ArrayList<EntitySearchResult> mOriginalDataset;
    private ArrayList<EntitySearchResult> mLocalDataset;
    private EntitySearchResultFilter mFilter;
    private SafeHandler mHandler = new SafeHandler(this);
    private String mCourse;
    private String mSearchKeyword;
    private String mStorageKey;
    private RecyclerView mView;
    private RecyclerView.Adapter<ViewHolder> mAdapter;
    private SwipeRefreshLayout mRefresh;
    private SearchActivity mParent;

    public SearchFragment(String course, String searchKeyword) {
        this(course, searchKeyword, null);
    }

    public SearchFragment(String course, String searchKeyword, SearchActivity parent) {
        mParent = parent;
        mCourse = course;
        mSearchKeyword = searchKeyword;
        mStorageKey = Storage.getKey(this.getClass().getSimpleName(), mCourse, mSearchKeyword);
        mLocalDataset = new ArrayList<>();
        mOriginalDataset = new ArrayList<>();
        mFilter = new EntitySearchResultFilter();
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

                // star animation
                view.findViewById(R.id.two_line_star).setOnClickListener(v -> {
                    String label = h.getPrimary().getText().toString();
                    if (HttpUtils.user.isFavourite(label)) {
                        Helper.ImageViewAnimatedChange(v.getContext(), v.findViewById(R.id.two_line_star), R.drawable.star_border, R.color.default_grey, 100);
                    } else {
                        Helper.ImageViewAnimatedChange(v.getContext(), v.findViewById(R.id.two_line_star), R.drawable.star, R.color.orange, 100);
                    }
                    HttpUtils.user.reverseFavourite(label);
                });

                view.setOnClickListener(v -> {
                    // history
                    h.getPrimary().setTextColor(getResources().getColor(R.color.default_grey, requireActivity().getTheme()));

                    Intent intent = new Intent(v.getContext(), EntityInfoActivity.class);
                    intent.putExtra(getString(R.string.label), h.getPrimary().getText().toString());
                    intent.putExtra(getString(R.string.course), SearchFragment.this.mCourse);
                    startActivity(intent);
                });
                return h;
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.getPrimary().setText(mLocalDataset.get(position).label);
                holder.getSecondary().setText(mLocalDataset.get(position).category);

                /* favourite */
                if (HttpUtils.user.isFavourite(mLocalDataset.get(position).label)) {
                    holder.getFavourite().setImageResource(R.drawable.star);
                    holder.getFavourite().setColorFilter(getResources().getColor(R.color.orange, requireActivity().getTheme()));
                } else {
                    holder.getFavourite().setImageResource(R.drawable.star_border);
                    holder.getFavourite().setColorFilter(getResources().getColor(R.color.default_grey, requireActivity().getTheme()));
                }

                /* history */
                if (HttpUtils.user.isHistory(mLocalDataset.get(position).label)) {
                    holder.getPrimary().setTextColor(getResources().getColor(R.color.default_grey, requireActivity().getTheme()));
                }
            }

            @Override
            public int getItemCount() {
                return mLocalDataset.size();
            }
        };
        mView.setAdapter(mAdapter);
        if (!mCourse.equals("")) {
            initData();
        }
        return view;
    }

    private void parseData(String raw_data) {
        try {
            JSONObject obj = new JSONObject(raw_data);
            JSONArray data = obj.getJSONArray("data");
            mOriginalDataset.clear();
            for (int i = 0; i < data.length(); ++i) {
                JSONObject single = data.getJSONObject(i);
                String label = single.getString("label");
                String category = single.getString("category");
                String uri = single.getString("uri");
                mOriginalDataset.add(new EntitySearchResult(label, category, uri));
            }
            mFilter.reset();
            if (mParent != null) mParent.onSearchFragmentUpdate();
            filterData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSearchInfoAndInit(String course, String searchKeyword) {
        mCourse = course;
        mSearchKeyword = searchKeyword;
        mStorageKey = Storage.getKey(this.getClass().getSimpleName(), mCourse, mSearchKeyword);
        initData();
    }

    public void initData() {
        mRefresh.setRefreshing(true);
        if (Storage.contains(this.requireContext(), mStorageKey)) {
            parseData(Storage.load(this.requireContext(), mStorageKey));
            mRefresh.setRefreshing(false);
        } else {
            onRefresh();
        }
    }

    public void onRefresh() {
        HttpUtils.searchEntity(mCourse, mSearchKeyword, mHandler);
    }

    public EntitySearchResultFilter getFilter() {
        return mFilter;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterData() {
        mLocalDataset = mFilter.filter(mOriginalDataset);
        mAdapter.notifyDataSetChanged();
    }
}
