package com.java.guohao;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Arrays;

public class SettingsActivity extends AppCompatActivity {

    public static final int SETTINGS_CHANGED = 1;

    private final String[] settings_text = {"跟随系统", "深色模式"};
    private final boolean[] checked = new boolean[settings_text.length];
    private final boolean[] enabled = new boolean[settings_text.length];

    public static class ViewHolder extends RecyclerView.ViewHolder {

        SwitchMaterial mSwitch;

        public ViewHolder(View view) {
            super(view);
            mSwitch = view.findViewById(R.id.settings_item);
        }

        public SwitchMaterial getSwitch() {
            return mSwitch;
        }

    }

    private int newNightMode;
    private boolean[] newChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar mTopBar = findViewById(R.id.settings_top_bar);
        mTopBar.setTitle("主题");
        mTopBar.setNavigationOnClickListener(v -> SettingsActivity.this.finish());
        mTopBar.setOnMenuItemClickListener(item -> {
            if (!Arrays.equals(newChecked, checked)) {
                (new MaterialAlertDialogBuilder(SettingsActivity.this))
                        .setTitle("提示")
                        .setMessage("需要重启APP以使设置生效。")
                        .setPositiveButton("确定", (dialog, which) -> {
                            Storage.save(getApplicationContext(), GlobVar.NIGHT_MODE_KEY, String.valueOf(newNightMode));
                            this.setResult(SETTINGS_CHANGED);
                            this.finish();
                        })
                        .show();
            } else {
                finish();
            }
            return false;
        });
        checked[0] = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        checked[1] = ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
        newChecked = checked.clone();
        enabled[0] = true;
        enabled[1] = !checked[0];

        RecyclerView mView = findViewById(R.id.settings_view);
        mView.setLayoutManager(new LinearLayoutManager(this));
        mView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_item, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.getSwitch().setText(settings_text[position]);
                holder.getSwitch().setEnabled(enabled[position]);
                holder.getSwitch().setChecked(checked[position]);
                holder.getSwitch().setOnCheckedChangeListener((buttonView, isChecked) -> {
                    newNightMode = AppCompatDelegate.getDefaultNightMode();
                    boolean dark = ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
                    if (position == 0) {
                        enabled[1] = !isChecked;
                        notifyItemChanged(1);
                        if (isChecked) {
                            newNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        } else {
                            if (dark) {
                                newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
                            } else {
                                newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
                            }
                        }
                    } else { // position == 1
                        if (isChecked) {
                            newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
                        } else {
                            newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
                        }
                    }
                    newChecked[position] = isChecked;
                });
            }

            @Override
            public int getItemCount() {
                return settings_text.length;
            }
        });
    }

}