package com.java.guohao;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;

public abstract class BasicListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final int LABEL_ORDER = 0;
    public static final int CATEGORY_ORDER = 1;
    public static final int FAVOURITE_ORDER = 2;

    protected int mCurOrder = LABEL_ORDER;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected final TextView mPrimary;
        protected final TextView mSecondary;
        protected final ImageView mFavourite;
        public String uri;

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
                if ((mLabel.isEmpty() || mLabel.containsKey(result.label)) && (mCategory.isEmpty() || mCategory.containsKey(result.category))) {
                    ret.add(result);
                }
            }
            return ret;
        }
    }

    protected ArrayList<EntitySearchResult> mOriginalDataset;
    protected ArrayList<EntitySearchResult> mLocalDataset;
    protected EntitySearchResultFilter mFilter;
    protected String mCourse;
    protected String mStorageKey;
    protected RecyclerView mView;
    protected RecyclerView.Adapter<ViewHolder> mAdapter;
    protected SwipeRefreshLayout mRefresh;
    protected SearchActivity mParent;

    public BasicListFragment() {
        this(null, "");
    }

    public BasicListFragment(String course) {
        this(null, course);
    }

    public BasicListFragment(SearchActivity parent, String course, String... args) {
        mParent = parent;
        mCourse = course;
        updateStorageKey(args);
        mLocalDataset = new ArrayList<>();
        mOriginalDataset = new ArrayList<>();
        mFilter = new EntitySearchResultFilter();
    }

    void updateStorageKey(String... args) {
        String otherArg = Storage.getKey(args);
        mStorageKey = Storage.getKey(this.getClass().getSimpleName(), mCourse, otherArg);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mRefresh = view.findViewById(R.id.basic_list_refresh);
        mRefresh.setOnRefreshListener(this); // onRefresh listens it

        mView = view.findViewById(R.id.basic_list_view);
        mView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mAdapter = new RecyclerView.Adapter<BasicListFragment.ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.two_line_list, parent, false);
                ViewHolder h = new ViewHolder(view);

                // star animation
                view.findViewById(R.id.two_line_star).setOnClickListener(v -> {
                    HttpUtils.CourseLabel courseLabel = new HttpUtils.CourseLabel(mCourse, h.getPrimary().getText().toString());
                    if (HttpUtils.user.isFavourite(courseLabel)) {
                        Helper.ImageViewAnimatedChange(v.getContext(), v.findViewById(R.id.two_line_star), R.drawable.star_border, R.color.default_grey, 100);
                    } else {
                        Helper.ImageViewAnimatedChange(v.getContext(), v.findViewById(R.id.two_line_star), R.drawable.star, R.color.orange, 100);
                    }
                    HttpUtils.user.reverseFavourite(courseLabel);
                });

                view.setOnClickListener(v -> {
                    // history
                    h.getPrimary().setTextColor(getResources().getColor(R.color.default_grey, requireActivity().getTheme()));

                    Intent intent = new Intent(v.getContext(), EntityInfoActivity.class);
                    intent.putExtra(getString(R.string.label), h.getPrimary().getText().toString());
                    intent.putExtra(getString(R.string.course), BasicListFragment.this.mCourse);
                    intent.putExtra(getString(R.string.uri), h.uri);
                    startActivity(intent);
                });
                return h;
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.getPrimary().setText(mLocalDataset.get(position).label);
                holder.getSecondary().setText(mLocalDataset.get(position).category);
                holder.uri = mLocalDataset.get(position).uri;

                /* favourite */
                HttpUtils.CourseLabel cl = new HttpUtils.CourseLabel(mCourse, mLocalDataset.get(position).label);
                if (HttpUtils.user.isFavourite(cl)) {
                    holder.getFavourite().setImageResource(R.drawable.star);
                    holder.getFavourite().setColorFilter(getResources().getColor(R.color.orange, requireActivity().getTheme()));
                } else {
                    holder.getFavourite().setImageResource(R.drawable.star_border);
                    holder.getFavourite().setColorFilter(getResources().getColor(R.color.default_grey, requireActivity().getTheme()));
                }

                /* history */
                if (HttpUtils.user.isHistory(cl)) {
                    holder.getPrimary().setTextColor(getResources().getColor(R.color.default_grey, requireActivity().getTheme()));
                }
            }

            @Override
            public int getItemCount() {
                return mLocalDataset.size();
            }
        };
        mView.setAdapter(mAdapter);
        initData();
        return view;
    }

    protected void parseData(String raw_data) {
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
            if (mParent != null) mParent.onBasicListFragmentUpdate();
            filterData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* need to implement: get data and save it to mLocalDataset */
    public abstract void onRefresh();

    public EntitySearchResultFilter getFilter() {
        return mFilter;
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

    @SuppressLint("NotifyDataSetChanged")
    public void filterData() {
        mLocalDataset = mFilter.filter(mOriginalDataset);
        sortData(mCurOrder); // and notify
    }

    public void sortData(int order) {
        mCurOrder = order;
        switch (order) {
            case LABEL_ORDER : {
                mLocalDataset.sort((o1, o2) -> o1.label.compareTo(o2.label));
                break;
            } case CATEGORY_ORDER : {
                mLocalDataset.sort((o1, o2) -> o1.category.compareTo(o2.category));
                break;
            } case FAVOURITE_ORDER : {
                mLocalDataset.sort((o1, o2) -> HttpUtils.user.isFavourite(new HttpUtils.CourseLabel(mCourse, o1.label)) ? -1 :
                        ((HttpUtils.user.isFavourite(new HttpUtils.CourseLabel(mCourse, o2.label))) ? 1 : o1.label.compareTo(o2.label)));
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }
}
