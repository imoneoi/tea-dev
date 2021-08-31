package com.java.guohao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.common.UiError;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.sina.weibo.sdk.openapi.WBAPIFactory;
import com.sina.weibo.sdk.share.WbShareCallback;

import java.io.ByteArrayOutputStream;

public class EntityInfoActivity extends AppCompatActivity implements WbShareCallback, WbShareInterface {

    private MaterialToolbar mTopBar;
    private ViewPager2 mViewPager;
    private TabLayout mTabBar;
    private TabLayoutMediator mMediator;
    private TextView mTitle;
    private Chip mCategory;
    private String mLabel;
    private String mCourse;
    private String mChineseCourse;
    private String mUri;
    private final String[] mTitles = { "实体详情", "知识链接", "相关试题" };
    private IWBAPI mWeiboAPI;

    private EntityInfoFragment mInfoFragment;
    private EntityRelationshipFragment mRelationshipFragment;
    private EntityQuestionFragment mQuestionFragment;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_info);
        Intent intent = getIntent();
        mLabel = intent.getStringExtra(getString(R.string.label));
        mCourse = intent.getStringExtra(getString(R.string.course));
        mChineseCourse = GlobVar.SUBJECT_OF_KEYWORD.get(mCourse);
        // mUri = intent.getStringExtra(getString(R.string.uri));

        // init weibo
        AuthInfo authInfo = new AuthInfo(this, GlobVar.APP_KY, GlobVar.REDIRECT_URL, GlobVar.SCOPE);
        mWeiboAPI = WBAPIFactory.createWBAPI(this);
        mWeiboAPI.registerApp(this, authInfo);
        mWeiboAPI.setLoggerEnable(true);

        mTitle = findViewById(R.id.entity_info_label);
        mTitle.setText(mLabel);
        mCategory = findViewById(R.id.entity_info_chip);
        mCategory.setText(mChineseCourse);

        mTopBar = findViewById(R.id.entity_info_top_bar);
        mTopBar.setTitle(R.string.entity_info);
        mTopBar.setNavigationOnClickListener(v -> EntityInfoActivity.this.finish());
        mTopBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.entity_info_share : {
                    share("我学习了一个新的知识，快来看看吧！",
                            mLabel + " - " + mChineseCourse,
                            mInfoFragment.getAbstract());
                    break;
                }
            }
            return false;
        });

        mInfoFragment = new EntityInfoFragment(mLabel, mCourse, mUri);
        mRelationshipFragment = new EntityRelationshipFragment(mLabel, mCourse, mUri);
        mQuestionFragment = new EntityQuestionFragment(mLabel, mCourse, mUri, this::share);

        mViewPager = findViewById(R.id.entity_info_viewpager);
        mViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                System.out.println(position);
                switch (position) {
                    case 0 : {
                        return mInfoFragment;
                    } case 1 : {
                        return mRelationshipFragment;
                    } case 2 : {
                        return mQuestionFragment;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("EntityInfoActivity", String.format("request %d, result %d, data %s", requestCode, resultCode, data));
        mWeiboAPI.doResultIntent(data, this);
    }

    public void share(String wbContent, String title, String content) {
        WeiboMultiMessage msg = new WeiboMultiMessage();
        TextObject text = new TextObject();
        text.text = wbContent;
        msg.textObject = text;
        WebpageObject obj = new WebpageObject();
        obj.title = title;
        obj.description = content;
        obj.actionUrl = GlobVar.APP_URL;
        /*
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.book);
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, os);
            obj.thumbData = os.toByteArray();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
        msg.mediaObject = obj;
        mWeiboAPI.shareMessage(msg, false /* client or H5 */);
    }

    @Override
    public void onComplete() {
        Snackbar.make(mViewPager, "分享成功", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onError(UiError uiError) {
        Snackbar.make(mViewPager, "分享失败", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCancel() {
        Snackbar.make(mViewPager, "分享取消", Snackbar.LENGTH_LONG).show();
    }
}