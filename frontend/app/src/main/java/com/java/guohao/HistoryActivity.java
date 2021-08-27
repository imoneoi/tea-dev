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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

    static class HistoryItem {
        public String label, course;
        public long time;

        HistoryItem(HttpUtils.CourseLabel key, long value) {
            label = key.label;
            course = key.course;
            time = value;
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
    private List<HistoryItem> historyList;

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
                HistoryItem cur = historyList.get(historyList.size() - position - 1);
                holder.getPrimaryText().setText(cur.label);
                holder.getSecondaryText().setText(GlobVar.SUBJECT_OF_KEYWORD.get(cur.course));
                holder.getTimeText().setText(deltaTime2text(new Timestamp(System.currentTimeMillis()).getTime() - cur.time));
                holder.getView().setOnClickListener(v -> {
                    Intent intent = new Intent(HistoryActivity.this, EntityInfoActivity.class);
                    intent.putExtra(getString(R.string.course), cur.course);
                    intent.putExtra(getString(R.string.label), cur.label);
                    startActivity(intent);
                });
            }

            @Override
            public int getItemCount() {
                return historyList.size();
            }
        };
        mView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        historyList = new ArrayList<>();
        for (Map.Entry<HttpUtils.CourseLabel, Long> item : HttpUtils.user.history.entrySet()) {
            historyList.add(new HistoryItem(item.getKey(), item.getValue()));
        }
        historyList.sort(Comparator.comparingLong(o -> o.time));
        mAdapter.notifyItemRangeChanged(0, historyList.size());
    }
}