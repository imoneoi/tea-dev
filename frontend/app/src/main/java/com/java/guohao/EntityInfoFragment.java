package com.java.guohao;

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

public class EntityInfoFragment extends Fragment {

    private static class SafeHandler extends Handler {
        private final WeakReference<Fragment> mParent;
        public SafeHandler(Fragment parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            EntityInfoFragment parent = (EntityInfoFragment) mParent.get();
            if (msg.what == GlobVar.SUCCESS_FROM_INTERNET) {
                String data = msg.obj.toString();
                parent.parseData(data);
                Storage.save(parent.requireContext(), parent.mStorageKey, data);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mLabel;
        private final TextView mObject;

        public ViewHolder(View view) {
            super(view);
            mLabel = view.findViewById(R.id.entity_info_predicate_label);
            mObject = view.findViewById(R.id.entity_info_object);
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

        @Override
        public String toString() {
            return predicateLabel + ": " + object;
        }
    }

    private SafeHandler mHandler = new SafeHandler(this);
    private RecyclerView.Adapter<EntityInfoFragment.ViewHolder> mInfoAdapter;
    private ArrayList<Property> mLocalDataset;
    private String mStorageKey;
    private String mLabel;
    private String mCourse;
    private String mUri;
    RecyclerView mView;
    CircularProgressIndicator mLoading;

    public EntityInfoFragment() {
        this("", "", "");
    }

    public EntityInfoFragment(String label, String course, String uri) {
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
        View view = inflater.inflate(R.layout.fragment_entity_info, container, false);
        mLoading = view.findViewById(R.id.entity_info_loading);
        mView = view.findViewById(R.id.entity_info_view);
        mView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mInfoAdapter = new RecyclerView.Adapter<EntityInfoFragment.ViewHolder>() {
            @NonNull
            @Override
            public EntityInfoFragment.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_entity_info, parent, false);
                EntityInfoFragment.ViewHolder h = new EntityInfoFragment.ViewHolder(view);

                view.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), EntityInfoActivity.class);
                    intent.putExtra(getString(R.string.label), h.getObject().getText().toString());
                    intent.putExtra(getString(R.string.course), mCourse);
                    // intent.putExtra(getString(R.string.uri), mUri);
                    startActivity(intent);
                });
                return h;
            }

            @Override
            public void onBindViewHolder(@NonNull EntityInfoFragment.ViewHolder holder, int position) {
                holder.getPredicateLabel().setText(mLocalDataset.get(position).predicateLabel);
                holder.getObject().setText(mLocalDataset.get(position).object);
            }

            @Override
            public int getItemCount() {
                return mLocalDataset.size();
            }
        };
        mView.setAdapter(mInfoAdapter);
        initData();
        return view;
    }

    private void parseData(String raw_data) {
        Log.i("EntityInfoFragment", raw_data);
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
            mLocalDataset.sort((o1, o2) -> o1.predicateLabel.compareTo(o2.predicateLabel));
            mInfoAdapter.notifyDataSetChanged();
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
        HttpUtils.getEntityInfo(mCourse, mLabel, mHandler);
    }

    public String getAbstract() {
        ArrayList<String> arr = new ArrayList<>();
        mLocalDataset.forEach(r -> arr.add(r.toString()));
        return TextUtils.join("\n", arr);
    }
}