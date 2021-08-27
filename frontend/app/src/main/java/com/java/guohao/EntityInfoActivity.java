package com.java.guohao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class EntityInfoActivity extends AppCompatActivity {

    private MaterialToolbar mTopBar;
    private ViewPager2 mViewPager;
    private TabLayout mTabBar;
    private TabLayoutMediator mMediator;
    private TextView mTitle;
    private Chip mCategory;
    private String mLabel;
    private String mCourse;
    private String mUri;
    private final String[] mTitles = { "实体详情", "知识链接", "相关试题" };

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_info);
        Intent intent = getIntent();
        mLabel = intent.getStringExtra(getString(R.string.label));
        mCourse = intent.getStringExtra(getString(R.string.course));
        // mUri = intent.getStringExtra(getString(R.string.uri));

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

        mViewPager = findViewById(R.id.entity_info_viewpager);
        mViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                System.out.println(position);
                switch (position) {
                    case 0 : {
                        return new EntityInfoFragment(mLabel, mCourse, mUri);
                    } case 1 : {
                        return new EntityRelationshipFragment(mLabel, mCourse, mUri);
                    } case 2 : {
                        return new EntityQuestionFragment(mLabel, mCourse, mUri);
                    }
                }
                return null;
            }

            @Override
            public int getItemCount() {
                return mTitles.length;
            }
        });
        mTabBar = findViewById(R.id.entity_info_tab);
        mMediator = new TabLayoutMediator(mTabBar, mViewPager, (tab, position) -> {
            tab.setText(mTitles[position]);
        });
        mMediator.attach();

        // add history
        HttpUtils.user.addHistory(new HttpUtils.CourseLabel(mCourse, mLabel));
    }

    private void share() {
        // TODO: share function
    }
}