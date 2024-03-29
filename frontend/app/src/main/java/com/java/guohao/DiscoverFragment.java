package com.java.guohao;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DiscoverFragment extends Fragment {

    private final String[] menu_text = {"实体链接", "专项测试", "知识梳理", "试题推荐"};
    private final int[] icon_id = {
            R.drawable.ic_baseline_link_24,
            R.drawable.ic_baseline_map_24,
            R.drawable.ic_baseline_timeline_24,
            R.drawable.ic_baseline_apps_24
    };
    private final Class[] next_page = {EntityLinkActivity.class, HistoryActivity.class, SettingsActivity.class, LoginActivity.class};

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mText;
        private final ImageView mImage;
        private final View mView;

        public ViewHolder(View view) {
            super(view);
            mText = view.findViewById(R.id.menu_item_text);
            mImage = view.findViewById(R.id.item_icon);
            mView = view;
        }

        public TextView getText() {
            return mText;
        }

        public ImageView getImage() {
            return mImage;
        }

        public View getView() {
            return mView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        RecyclerView mView = view.findViewById(R.id.discover_view);
        mView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mView.setAdapter(new RecyclerView.Adapter<MeFragment.ViewHolder>() {
            @NonNull
            @Override
            public MeFragment.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
                return new MeFragment.ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull MeFragment.ViewHolder holder, int position) {
                holder.getText().setText(menu_text[position]);
                holder.getImage().setImageResource(icon_id[position]);
                holder.getView().setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), next_page[position]);
                    startActivityForResult(intent, position);
                });
            }

            @Override
            public int getItemCount() {
                return menu_text.length;
            }
        });
        return view;
    }
}