package com.java.guohao;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chivorn.smartmaterialspinner.SmartMaterialSpinner;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public class EntityLinkSearchFragment extends Fragment {

    private class ClickText extends ClickableSpan {
        private String mLabel;
        private String mUri;

        public ClickText(String label, String uri) {
            mLabel = label;
            mUri = uri;
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(requireContext().getColor(R.color.searchBarTextHighlightColor));
        }

        @Override
        public void onClick(@NonNull View view) {
            Intent intent = new Intent(requireActivity(), EntityInfoActivity.class);
            intent.putExtra(getString(R.string.label), mLabel);
            intent.putExtra(getString(R.string.course), GlobVar.KEYWORD_OF_SUBJECT.get(mCourse.getSelectedItem()));
            intent.putExtra(getString(R.string.uri), mUri);
            startActivity(intent);
        }
    }

    private static class SafeHandler extends Handler {
        private final WeakReference<Fragment> mParent;
        public SafeHandler(Fragment parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            EntityLinkSearchFragment parent = (EntityLinkSearchFragment) mParent.get();
            if (msg.what == GlobVar.SUCCESS_FROM_INTERNET) {
                String data = msg.obj.toString();
                parent.parseData(data);
                parent.updateData();
            }
        }
    }

    SafeHandler mHandler = new SafeHandler(this);

    class EntityLinkItem {
        public String entityType;
        public String uri;
        public String label;
        public Integer startIndex;
        public Integer endIndex;
        EntityLinkItem(String entityType, String uri, String entity, Integer startIndex, Integer endIndex) {
            this.entityType = entityType;
            this.label = entity;
            this.uri = uri;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    ArrayList<EntityLinkItem> mLocalDataset;

    ExtendedFloatingActionButton mSearchButton;
    TextInputEditText mResult;
    TextInputEditText mSearchText;
    SmartMaterialSpinner<String> mCourse;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_entity_link_search, container, false);
        mLocalDataset = new ArrayList<>();
        mSearchText = v.findViewById(R.id.entity_link_search_text);
        mResult = v.findViewById(R.id.entity_link_result);
        mCourse = v.findViewById(R.id.entity_link_course);
        mCourse.setItem(Arrays.asList(GlobVar.SUBJECTS));
        mSearchButton = v.findViewById(R.id.entity_link_search);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String course = mCourse.getSelectedItem();
                if (course == null) {
                    Snackbar.make(v, "请选择学科", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                HttpUtils.getLink(GlobVar.KEYWORD_OF_SUBJECT.get(course), mSearchText.getText().toString(), mHandler);
            }
        });
        return v;
    }

    private void updateData() {
        SpannableString str = new SpannableString(mSearchText.getText().toString());
        for (EntityLinkItem item : mLocalDataset) {
            str.setSpan(new ClickText(item.label, item.uri), item.startIndex, item.endIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mResult.setText(str);
        mResult.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void parseData(String raw_data) {
        try {
            JSONObject data = new JSONObject(raw_data).getJSONObject("data");
            JSONArray results = data.getJSONArray("results");
            mLocalDataset.clear();
            for (int i = 0; i < results.length(); ++i) {
                JSONObject item = results.getJSONObject(i);
                mLocalDataset.add(new EntityLinkItem(
                        item.getString("entity_type"), item.getString("entity_url"),
                        item.getString("entity"), item.getInt("start_index"), item.getInt("end_index")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}