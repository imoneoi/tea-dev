package com.java.guohao;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MeFragment extends Fragment {

    final String[] menu_text = {"我的收藏", "浏览历史", "设置", "退出登录"};
    final int[] icon_id = {
            R.drawable.star_border,
            R.drawable.ic_baseline_history_24,
            R.drawable.ic_baseline_settings_24,
            R.drawable.baseline_logout_24
    };

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
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        RecyclerView mView = view.findViewById(R.id.me_view);
        mView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.getText().setText(menu_text[position]);
                holder.getImage().setImageResource(icon_id[position]);
                holder.getView().setOnClickListener(v -> {
                    Class next_page = null;
                    switch (position) {
                        case 3:
                            HttpUtils.user.session = "";
                            ((MainActivity) requireActivity()).saveSession();
                            next_page = Login.class;
                            Toast.makeText(v.getContext(), "成功登出", Toast.LENGTH_LONG).show();
                            break;
                    }
                    Intent intent = new Intent(v.getContext(), next_page);
                    startActivity(intent);
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