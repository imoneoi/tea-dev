package com.java.guohao;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.material.appbar.MaterialToolbar;

public class EntityLinkActivity extends AppCompatActivity {

    MaterialToolbar mTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_link);
        mTopBar = findViewById(R.id.entity_link_top_bar);
        mTopBar.setNavigationOnClickListener(v -> EntityLinkActivity.this.finish());
    }
}