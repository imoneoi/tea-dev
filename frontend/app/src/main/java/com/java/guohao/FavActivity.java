package com.java.guohao;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;

public class FavActivity extends AppCompatActivity {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPrimaryText, mSecondaryText;
        private final View mView;
        private final ImageView mFavourite;

        public ViewHolder(View view) {
            super(view);
            mPrimaryText = view.findViewById(R.id.two_line_primary_text);
            mSecondaryText = view.findViewById(R.id.two_line_secondary_text);
            mFavourite = view.findViewById(R.id.two_line_star);
            mView = view;
        }

        public TextView getPrimaryText() {
            return mPrimaryText;
        }

        public TextView getSecondaryText() {
            return mSecondaryText;
        }

        public View getView() {
            return mView;
        }

        public ImageView getFavourite() {
            return mFavourite;
        }
    }

    private RecyclerView.Adapter<ViewHolder> mAdapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav);

        MaterialToolbar mTopBar = findViewById(R.id.fav_top_bar);
        mTopBar.setTitle("我的收藏");
        mTopBar.setNavigationOnClickListener(v -> FavActivity.this.finish());

        SwipeRefreshLayout mRefresh = findViewById(R.id.fav_refresh);
        mRefresh.setOnRefreshListener(() -> {
            mAdapter.notifyDataSetChanged();
            mRefresh.setRefreshing(false);
        });

        RecyclerView mView = findViewById(R.id.fav_view);
        mView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecyclerView.Adapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.two_line_list, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                HttpUtils.CourseLabel key = HttpUtils.user.favorite.keyAt(HttpUtils.user.favorite.size() - position - 1);
                holder.getPrimaryText().setText(key.label);
                holder.getSecondaryText().setText(GlobVar.SUBJECT_OF_KEYWORD.get(key.course));

                holder.getFavourite().setImageResource(R.drawable.star);
                holder.getFavourite().setColorFilter(getResources().getColor(R.color.orange, getTheme()));
                holder.getFavourite().setOnClickListener(v -> {
                    if (HttpUtils.user.isFavourite(key)) {
                        Helper.ImageViewAnimatedChange(v.getContext(), v.findViewById(R.id.two_line_star), R.drawable.star_border, R.color.default_grey, 100);
                    } else {
                        Helper.ImageViewAnimatedChange(v.getContext(), v.findViewById(R.id.two_line_star), R.drawable.star, R.color.orange, 100);
                    }
                    HttpUtils.user.reverseFavourite(key);
                });

                holder.getView().setOnClickListener(v -> {
                    Intent intent = new Intent(FavActivity.this, EntityInfoActivity.class);
                    intent.putExtra(getString(R.string.course), key.course);
                    intent.putExtra(getString(R.string.label), key.label);
                    startActivity(intent);
                });
            }

            @Override
            public int getItemCount() {
                return HttpUtils.user.favorite.size();
            }
        };
        mView.setAdapter(mAdapter);
    }

}