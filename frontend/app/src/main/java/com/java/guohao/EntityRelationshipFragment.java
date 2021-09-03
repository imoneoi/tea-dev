package com.java.guohao;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;

public class EntityRelationshipFragment extends Fragment {

    private static class SafeHandler extends Handler {
        private final WeakReference<Fragment> mParent;
        public SafeHandler(Fragment parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            EntityRelationshipFragment parent = (EntityRelationshipFragment) mParent.get();
            if (msg.what == GlobVar.SUCCESS_FROM_INTERNET) {
                String data = msg.obj.toString();
                parent.parseData(data);
                Storage.save(parent.requireContext(), parent.mStorageKey, data);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPredicate;
        private final TextView mObject;

        public ViewHolder(View view) {
            super(view);
            mPredicate = view.findViewById(R.id.entity_relationship_predicate);
            mObject = view.findViewById(R.id.entity_relationship_value);
        }

        public TextView getPredicate() {
            return mPredicate;
        }

        public TextView getValue() {
            return mObject;
        }
    }

    private class Relationship {
        public String predicate;
        public String value;
        public Relationship(String predicate, String value) {
            this.predicate = predicate;
            this.value = value;
        }
    }

    private SafeHandler mHandler = new SafeHandler(this);
    private String mStorageKey;
    private String mLabel;
    private String mCourse;
    private String mUri;
    private ArrayList<Relationship> mLocalDataset;
    RecyclerView mView;
    RecyclerView.Adapter<ViewHolder> mAdapter;
    CircularProgressIndicator mLoading;

    public EntityRelationshipFragment() {
        this("", "", "");
    }

    public EntityRelationshipFragment(String label, String course, String uri) {
        mLabel = label;
        mCourse = course;
        mUri = uri;
        mLocalDataset = new ArrayList<>();
        mStorageKey = Storage.getKey(this.getClass().getSimpleName(), label, course);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entity_relationship, container, false);
        mLoading = view.findViewById(R.id.entity_relationship_loading);
        mView = view.findViewById(R.id.entity_relationship_view);
        mView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new RecyclerView.Adapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(requireContext()).inflate(R.layout.card_entity_relationship, parent, false);
                ViewHolder h = new ViewHolder(view);

                view.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), EntityInfoActivity.class);
                    intent.putExtra(getString(R.string.label), h.getValue().getText().toString());
                    intent.putExtra(getString(R.string.course), mCourse);
                    // intent.putExtra(getString(R.string.uri), mUri);
                    startActivity(intent);
                });
                return h;
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.getPredicate().setText(mLocalDataset.get(position).predicate);
                holder.getValue().setText(mLocalDataset.get(position).value);
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

    @SuppressLint("NotifyDataSetChanged")
    private void parseData(String raw_data) {
        Log.i("EntityRelationshipFragment", raw_data);
        try {
            JSONObject obj = new JSONObject(raw_data);
            JSONArray data = obj.getJSONArray("data");
            mLocalDataset.clear();
            for (int i = 0; i < data.length(); ++i) {
                JSONObject item = data.getJSONObject(i);
                String value = TextUtils.join("\n",
                        item.getString("value").split("<br>"))
                        .trim();
                mLocalDataset.add(new Relationship(
                        item.getString("predicate"), value));
            }
            mLocalDataset.sort(Comparator.comparing(o -> o.predicate));
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLoading.setVisibility(View.GONE);
    }

    private void initData() {
        if (Storage.contains(requireContext(), mStorageKey)) {
            parseData(Storage.load(requireContext(), mStorageKey));
        } else {
            onRefresh();
        }
    }

    // get data from Internet
    private void onRefresh() {
        HttpUtils.getEntityRelationship(mCourse, mLabel, mHandler);
    }
}