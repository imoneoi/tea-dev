package com.java.guohao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class EntityInfoActivity extends AppCompatActivity {

    private static class SafeHandler extends Handler {
        private final WeakReference<AppCompatActivity> mParent;
        public SafeHandler(AppCompatActivity parent) {
            mParent = new WeakReference<>(parent);
        }
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == GlobVar.SUCCESS_FROM_INTERNET) {
                EntityInfoActivity parent = (EntityInfoActivity) mParent.get();
                String data = msg.obj.toString();
                parent.parseData(data);
                Storage.save(parent, parent.mStorageKey, data);
                parent.mAdapter.notifyDataSetChanged();
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mLabel;
        private final TextView mObject;

        public ViewHolder(View view) {
            super(view);
            mLabel = view.findViewById(R.id.entity_relationship_predicate_label);
            mObject = view.findViewById(R.id.entity_relationship_object);
        }

        public TextView getPredicateLabel() {
            return mLabel;
        }

        public TextView getObject() {
            return mObject;
        }
    }

    private static class Property {
        String predicate;
        String predicateLabel;
        String object;

        Property(String predicate, String predicateLabel, String object) {
            this.predicate = predicate;
            this.predicateLabel = predicateLabel;
            this.object = object;
        }
    }

    private SafeHandler mHandler = new SafeHandler(this);
    private MaterialToolbar mTopBar;
    private RecyclerView mView;
    private TextView mTitle;
    private Chip mCategory;
    private RecyclerView.Adapter<EntityInfoActivity.ViewHolder> mAdapter;
    private String mLabel;
    private String mCourse;
    private String mStorageKey;
    private ArrayList<Property> mLocalDataset;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_info);
        mLocalDataset = new ArrayList<>();
        Intent intent = getIntent();
        mLabel = intent.getStringExtra(getString(R.string.label));
        mCourse = intent.getStringExtra(getString(R.string.course));
        mStorageKey = Storage.getKey(this.getClass().getSimpleName(), mLabel, mCourse);

        mTitle = findViewById(R.id.entity_info_label);
        mTitle.setText(mLabel);
        mCategory = findViewById(R.id.entity_info_chip);
        mCategory.setText(GlobVar.SUBJECT_OF_KEYWORD.get(mCourse));

        mTopBar = findViewById(R.id.entity_info_top_bar);
        mTopBar.setTitle(R.string.entity_info);
        mTopBar.setNavigationOnClickListener(v -> EntityInfoActivity.this.finish());
        mTopBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.entity_info_share : {
                    share();
                    break;
                }
            }
            return false;
        });

        mView = findViewById(R.id.entity_info_view);
        mView.setLayoutManager(new LinearLayoutManager(EntityInfoActivity.this));
        mAdapter = new RecyclerView.Adapter<EntityInfoActivity.ViewHolder>() {
            @NonNull
            @Override
            public EntityInfoActivity.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_entity_relationship, parent, false);
                EntityInfoActivity.ViewHolder h = new ViewHolder(view);

                view.setOnClickListener(v -> {
                    Intent intent1 = new Intent(v.getContext(), EntityInfoActivity.class);
                    intent1.putExtra(getString(R.string.label), h.getObject().getText().toString());
                    intent1.putExtra(getString(R.string.course), EntityInfoActivity.this.mCourse);
                    startActivity(intent1);
                });
                return h;
            }

            @Override
            public void onBindViewHolder(@NonNull EntityInfoActivity.ViewHolder holder, int position) {
                holder.getPredicateLabel().setText(mLocalDataset.get(position).predicateLabel);
                holder.getObject().setText(mLocalDataset.get(position).object);
            }

            @Override
            public int getItemCount() {
                return mLocalDataset.size();
            }
        };
        mView.setAdapter(mAdapter);
        initData();
    }

    private void share() {
        // TODO: share function
    }

    private void initData() {
        HttpUtils.user.addHistory(mLabel);
        if (Storage.contains(this, mStorageKey)) {
            parseData(Storage.load(this, mStorageKey));
        } else {
            onRefresh();
        }
    }

    // get data from Internet
    private void onRefresh() {
        HttpUtils.getEntityInfo(mCourse, mLabel, mHandler);
    }

    private void parseData(String raw_data) {
        Log.i("EntityInfoActivity", raw_data);
        try {
            JSONObject obj = new JSONObject(raw_data);
            JSONArray property = obj.getJSONObject("data").getJSONArray("property");
            mLocalDataset.clear();
            for (int i = 0; i < property.length(); ++i) {
                JSONObject item = property.getJSONObject(i);
                mLocalDataset.add(new Property(
                        item.getString("predicate"), item.getString("predicateLabel"),
                        item.getString("object")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}