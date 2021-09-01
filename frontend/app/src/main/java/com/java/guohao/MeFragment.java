package com.java.guohao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MeFragment extends Fragment {

    private final String[] menu_text = {"我的收藏", "浏览历史", "深色主题", "退出登录"};
    private final int[] icon_id = {
        R.drawable.star_border,
        R.drawable.ic_baseline_history_24,
        R.drawable.ic_baseline_nights_stay_24,
        R.drawable.baseline_logout_24
    };
    private final Class[] next_page = {FavActivity.class, HistoryActivity.class, SettingsActivity.class, LoginActivity.class};

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
                    if (position == 3) {
                        HttpUtils.user.session = "";
                        ((MainActivity) requireActivity()).saveUserData();
                        Toast.makeText(v.getContext(), "成功登出", Toast.LENGTH_LONG).show();
                    }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == SettingsActivity.SETTINGS_CHANGED) {
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
            System.exit(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((TextView) requireView().findViewById(R.id.me_username)).setText(HttpUtils.user.username);
    }
}