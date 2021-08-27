package com.java.guohao;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

public class FavActivity extends AppCompatActivity {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPrimaryText, mSecondaryText;
        private final View mView;

        public ViewHolder(View view) {
            super(view);
            mPrimaryText = view.findViewById(R.id.two_line_primary_text);
            mSecondaryText = view.findViewById(R.id.two_line_secondary_text);
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
    }

    private RecyclerView.Adapter<ViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav);

        MaterialToolbar mTopBar = findViewById(R.id.fav_top_bar);
        mTopBar.setTitle("我的收藏");
        mTopBar.setNavigationOnClickListener(v -> FavActivity.this.finish());

        RecyclerView mView = findViewById(R.id.fav_view);
        mView.setLayoutManager(new LinearLayoutManager(this));
        mView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
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
        });
    }

}