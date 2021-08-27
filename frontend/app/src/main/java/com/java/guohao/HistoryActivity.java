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

import java.sql.Timestamp;

public class HistoryActivity extends AppCompatActivity {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPrimaryText, mSecondaryText, mTimeText;
        private final View mView;

        public ViewHolder(View view) {
            super(view);
            mPrimaryText = view.findViewById(R.id.history_item_primary_text);
            mSecondaryText = view.findViewById(R.id.history_item_secondary_text);
            mTimeText = view.findViewById(R.id.history_time);
            mView = view;
        }

        public TextView getPrimaryText() {
            return mPrimaryText;
        }

        public TextView getSecondaryText() {
            return mSecondaryText;
        }

        public TextView getTimeText() {
            return mTimeText;
        }

        public View getView() {
            return mView;
        }
    }

    String deltaTime2text(long delta) {
        double t = delta / 1000.0;
        if (t < 60) {
            return (int) t + "秒前";
        }
        t /= 60;
        if (t < 120) {
            return (int) t + "分钟前";
        }
        t /= 60;
        if (t < 24) {
            return (int) t + "小时前";
        }
        t /= 24;
        return (int) t + "天前";
    }

    private RecyclerView.Adapter<ViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        MaterialToolbar mTopBar = findViewById(R.id.history_top_bar);
        mTopBar.setTitle("浏览历史");
        mTopBar.setNavigationOnClickListener(v -> HistoryActivity.this.finish());

        RecyclerView mView = findViewById(R.id.history_view);
        mView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecyclerView.Adapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                HttpUtils.CourseLabel key = HttpUtils.user.history.keyAt(HttpUtils.user.history.size() - position - 1);
                holder.getPrimaryText().setText(key.label);
                holder.getSecondaryText().setText(GlobVar.SUBJECT_OF_KEYWORD.get(key.course));
                long delta = new Timestamp(System.currentTimeMillis()).getTime() - HttpUtils.user.history.get(key);
                holder.getTimeText().setText(deltaTime2text(delta));
                holder.getView().setOnClickListener(v -> {
                    Intent intent = new Intent(HistoryActivity.this, EntityInfoActivity.class);
                    intent.putExtra(getString(R.string.course), key.course);
                    intent.putExtra(getString(R.string.label), key.label);
                    startActivity(intent);
                });
            }

            @Override
            public int getItemCount() {
                return HttpUtils.user.history.size();
            }
        };
        mView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyItemRangeChanged(0, HttpUtils.user.history.size());
    }
}